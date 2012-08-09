package org.jbpm.simulation.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil.FeatureEList;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.MetadataType;
import org.jboss.drools.MetaentryType;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.util.BPMN2Utils;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class BPMN2SimulationDataProvider implements SimulationDataProvider {

    private FlowElementsContainer container;
    private Map<String, FlowElement> localCache = new HashMap<String, FlowElement>();
    
    public BPMN2SimulationDataProvider(FlowElementsContainer container) {
        this.container = container;
    }
    
    public BPMN2SimulationDataProvider(String bpmn2xml) {

        this(new ByteArrayInputStream(getBytes(bpmn2xml)));

    }
    
    public BPMN2SimulationDataProvider(InputStream bpmn2Stream) {

        Definitions defs = BPMN2Utils.getDefinitions(bpmn2Stream);
        List<RootElement> rootElements = defs.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                // TODO add support for multiple processes in definition
                this.container = (Process) root;
                break;
            }
        }
    }
    public Map<String, Object> getSimulationDataForNode(Node node) {
        String nodeId = (String) node.getMetaData().get("UniqueId");
        
        return getSimulationDataForNode(nodeId);
    }
    public Map<String, Object> getSimulationDataForNode(
            String nodeId) {
        
        FlowElement foundElement = null;
        if (localCache.containsKey(nodeId)) {
            foundElement = localCache.get(nodeId);
        } else {
            foundElement = findElementInContainer(container, nodeId);
            localCache.put(nodeId, foundElement);
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        // extract required properties from flow element and put them into a map
        List<ExtensionAttributeValue> extensions = foundElement.getExtensionValues();
        for (ExtensionAttributeValue extAttrValue : extensions) {
            FeatureMap extensionElements = extAttrValue.getValue();
           
            
            @SuppressWarnings("unchecked")
            
            FeatureEList<MetadataType> metadataTypeExtensions = (FeatureEList<MetadataType>) extensionElements
                                                 .get(DroolsPackage.Literals.DOCUMENT_ROOT__METADATA, true);
            if(metadataTypeExtensions != null && metadataTypeExtensions.size() > 0) {
             MetadataType metaType = metadataTypeExtensions.get(0);
             for(Object metaEntryObj : metaType.getMetaentry()) {
                 MetaentryType entry = (MetaentryType) metaEntryObj;
                 if(entry.getName() != null && entry.getName().equals("staffavailability")) {
                     properties.put("staffavailability", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("workinghours")) {
                     properties.put("workinghours", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("costpertimeunit")) {
                     properties.put("costpertimeunit", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("duration")) {
                     properties.put("duration", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("timeunit")) {
                     properties.put("timeunit", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("range")) {
                     properties.put("range", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("standarddeviation")) {
                     properties.put("standarddeviation", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("distributiontype")) {
                     properties.put("distributiontype", entry.getValue());
                 }
                 if(entry.getName() != null && entry.getName().equals("probability")) {
                     properties.put("probability", entry.getValue());
                 }
             }
            }
        }
        return properties;
    }

    protected FlowElement findElementInContainer(FlowElementsContainer container, String id) {
        List<FlowElement> currentContainerElems = container.getFlowElements();
        
        for (FlowElement fElement : currentContainerElems) {
            if (fElement.getId().equals(id) ) {
                return fElement;
            } else if (fElement instanceof FlowElementsContainer) {
                FlowElement fe = findElementInContainer((FlowElementsContainer) fElement, id);
                if (fe != null) {
                    return fe;
                }
            }
        }
        
        return null;
    }
    
    public double calculatePathProbability(SimulationPath path) {
        double probability = 100;
        for (String sequenceFlowId : path.getSequenceFlowsIds()) {
            String probabilityFromElement = (String) getSimulationDataForNode(sequenceFlowId).get(SimulationConstants.PROBABILITY);
            if (probabilityFromElement != null) {
                double transitionProbability = SimulationUtils.asDouble(probabilityFromElement);
                if (transitionProbability > 0) {
                    probability = probability * (transitionProbability / 100);
                }
            }
        }
        double result = probability / 100;
        
        path.setProbability(result);
        
        return result;
    }
    
    private static byte[] getBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}

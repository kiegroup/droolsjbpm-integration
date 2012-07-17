package org.jbpm.simulation.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.ExtensionDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.jbpm.simulation.PathContextManager;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.util.BPMN2Utils;

public class BPMN2SimulationDataProvider implements SimulationDataProvider {

    private FlowElementsContainer container;
    private Map<String, FlowElement> localCache = new HashMap<String, FlowElement>();
    
    public BPMN2SimulationDataProvider(FlowElementsContainer container) {
        this.container = container;
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
    
    public Map<String, Object> getSimulationDataForNode(String processId,
            Node node) {
        String nodeId = (String) node.getMetaData().get("UniqueId");
        FlowElement foundElement = null;
        if (localCache.containsKey(nodeId)) {
            foundElement = localCache.get(nodeId);
        } else {
            foundElement = findElementInContainer(container, nodeId);
            localCache.put(nodeId, foundElement);
        }
        
        // extract required properties from flow element and put them into a map
        List<ExtensionAttributeValue> extensions = foundElement.getExtensionValues();
        for (ExtensionAttributeValue extAttrValue : extensions) {
            
        }
        return null;
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
}

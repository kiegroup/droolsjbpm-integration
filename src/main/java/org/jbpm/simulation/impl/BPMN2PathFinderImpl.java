package org.jbpm.simulation.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContextManager;
import org.jbpm.simulation.PathFinder;
import org.jbpm.simulation.PathFormatConverter;
import org.jbpm.simulation.handler.ElementHandler;
import org.jbpm.simulation.handler.HandlerRegistry;
import org.jbpm.simulation.util.JBPMBpmn2ResourceFactoryImpl;
import org.jbpm.simulation.util.JBPMBpmn2ResourceImpl;

public class BPMN2PathFinderImpl implements PathFinder {

    private PathContextManager manager;
    
    private Definitions definitions;
    
    private List<FlowElement> triggerElements = new ArrayList<FlowElement>();
    private Map<String, FlowElement> catchingEvents = new HashMap<String, FlowElement>();


    public BPMN2PathFinderImpl(String bpmn2xml) {
        this.manager = new PathContextManager();
        try {
            InputStream is = new ByteArrayInputStream(bpmn2xml.getBytes("UTF-8"));
            
            this.definitions = getDefinitions(is);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public BPMN2PathFinderImpl(File bpmn2File) {
        this.manager = new PathContextManager();
        try {
            InputStream is = new FileInputStream(bpmn2File);
            
            this.definitions = getDefinitions(is);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public BPMN2PathFinderImpl(InputStream bpmn2Stream) {
        this.manager = new PathContextManager();
        this.definitions = getDefinitions(bpmn2Stream);
    }

    public BPMN2PathFinderImpl(Definitions bpmn2Defs) {
        this.manager = new PathContextManager();
        this.definitions = bpmn2Defs;
    }

    public List<PathContext> findPaths() {
        List<RootElement> rootElements = definitions.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                Process process = (Process) root;
                // find flow elements and traverse it find path
                List<FlowElement> flowElements = process.getFlowElements();
                for (FlowElement fElement : flowElements) {
                    if (fElement instanceof StartEvent) {
                        triggerElements.add(0, fElement);
                    } else if((fElement instanceof Activity) && isAdHocProcess(process)) {
                        Activity act = (Activity) fElement;
                        if(act.getIncoming() == null || act.getIncoming().size() == 0) {
                            triggerElements.add(0, fElement);
                        }
                    } else if (fElement instanceof IntermediateCatchEvent) {
                        String key = null;
                        List<EventDefinition> eventDefinitions = ((IntermediateCatchEvent) fElement)
                                .getEventDefinitions();
                        if (eventDefinitions != null) {
                            for (EventDefinition edef : eventDefinitions) {
                                if (edef instanceof SignalEventDefinition) {
                                    key = ((SignalEventDefinition) edef)
                                            .getSignalRef();
                                } else if (edef instanceof MessageEventDefinition) {
                                    key = ((MessageEventDefinition) edef)
                                            .getMessageRef().getId();
                                } else if (edef instanceof LinkEventDefinition) {
                                    key = ((LinkEventDefinition) edef).getName();
                                } else if (edef instanceof CompensateEventDefinition) {
                                    key = ((CompensateEventDefinition) edef)
                                            .getActivityRef().getId();
                                }
                                if (key != null) {
                                    catchingEvents.put(key, fElement);
                                }
                            }
                        }
                    }
                }
            }
        }
        ElementHandler handler = HandlerRegistry.getMainHandler();
        // show what was found
        for (FlowElement fe : triggerElements) {
            if (fe instanceof StartEvent || fe instanceof Activity) {
                handler.handle(fe, manager);
            }
        }
        
        manager.complete();
        
        return manager.getCompletePaths();
    }

    protected Definitions getDefinitions(InputStream is) {
        try {
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet
                    .getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                            new JBPMBpmn2ResourceFactoryImpl());
            resourceSet.getPackageRegistry().put(
                    "http://www.omg.org/spec/BPMN/20100524/MODEL",
                    Bpmn2Package.eINSTANCE);
            JBPMBpmn2ResourceImpl resource = (JBPMBpmn2ResourceImpl) resourceSet
                    .createResource(URI
                            .createURI("inputStream://dummyUriWithValidSuffix.xml"));
            resource.getDefaultLoadOptions().put(
                    JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
            resource.setEncoding("UTF-8");
            Map<String, Object> options = new HashMap<String, Object>();
            options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
            
            resource.load(is, options);

            EList<Diagnostic> warnings = resource.getWarnings();

            if (warnings != null && !warnings.isEmpty()) {
                for (Diagnostic diagnostic : warnings) {
                    System.out.println("Warning: " + diagnostic.getMessage());
                }
            }

            EList<Diagnostic> errors = resource.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (Diagnostic diagnostic : errors) {
                    System.out.println("Error: " + diagnostic.getMessage());
                }
                throw new IllegalStateException(
                        "Error parsing process definition");
            }

            return ((DocumentRoot) resource.getContents().get(0))
                    .getDefinitions();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    protected static String streamToString(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    protected static boolean isAdHocProcess(Process process) {
        Iterator<FeatureMap.Entry> iter = process.getAnyAttribute().iterator();
        while(iter.hasNext()) {
            FeatureMap.Entry entry = iter.next();
            if(entry.getEStructuralFeature().getName().equals("adHoc")) {
                return Boolean.parseBoolean(((String)entry.getValue()).trim());
            }
        }
        return false;
    }

    public <E> E findPaths(PathFormatConverter<E> converter) {
        findPaths();
        
        return converter.convert(manager.getCompletePaths());
    }

}

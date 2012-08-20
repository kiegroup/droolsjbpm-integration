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
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContextManager;
import org.jbpm.simulation.PathFinder;
import org.jbpm.simulation.PathFormatConverter;
import org.jbpm.simulation.handler.ElementHandler;
import org.jbpm.simulation.handler.HandlerRegistry;
import org.jbpm.simulation.util.BPMN2Utils;

public class BPMN2PathFinderImpl implements PathFinder {

    private PathContextManager manager;
    
    private Definitions definitions;
    private FlowElementsContainer container;
    private List<FlowElement> triggerElements = new ArrayList<FlowElement>();
    


    public BPMN2PathFinderImpl(String bpmn2xml) {
        this.manager = new PathContextManager();
        try {
            InputStream is = new ByteArrayInputStream(bpmn2xml.getBytes("UTF-8"));
            
            this.definitions = BPMN2Utils.getDefinitions(is);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public BPMN2PathFinderImpl(File bpmn2File) {
        this.manager = new PathContextManager();
        try {
            InputStream is = new FileInputStream(bpmn2File);
            
            this.definitions = BPMN2Utils.getDefinitions(is);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public BPMN2PathFinderImpl(InputStream bpmn2Stream) {
        this.manager = new PathContextManager();
        this.definitions = BPMN2Utils.getDefinitions(bpmn2Stream);
    }

    public BPMN2PathFinderImpl(Definitions bpmn2Defs) {
        this.manager = new PathContextManager();
        this.definitions = bpmn2Defs;
    }
    
    public BPMN2PathFinderImpl(FlowElementsContainer bpmn2Container) {
        this.manager = new PathContextManager();
        this.container = bpmn2Container;
    }

    public List<PathContext> findPaths() {
        Map<String, FlowElement> catchingEvents = new HashMap<String, FlowElement>();
        if (this.definitions != null) {
            List<RootElement> rootElements = definitions.getRootElements();
            for (RootElement root : rootElements) {
                if (root instanceof Process) {
                    Process process = (Process) root;
                    readFlowElements(process, catchingEvents);
                }
            }
        } else {
            readFlowElements(container, catchingEvents);
        }
        
        manager.setCatchingEvents(catchingEvents);
        ElementHandler handler = HandlerRegistry.getMainHandler();
        // show what was found
        for (FlowElement fe : triggerElements) {
            if (fe instanceof StartEvent || fe instanceof Activity || fe instanceof IntermediateCatchEvent) {
                handler.handle(fe, manager);
            }
        }
        
        manager.complete();
        
        return manager.getCompletePaths();
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

    public <E> E findPaths(PathFormatConverter<E> converter) {
        findPaths();
        
        return converter.convert(manager.getCompletePaths());
    }
    
    protected void readFlowElements(FlowElementsContainer container, Map<String, FlowElement> catchingEvents) {
     // find flow elements and traverse it find path
        List<FlowElement> flowElements = container.getFlowElements();
        for (FlowElement fElement : flowElements) {
            if (fElement instanceof StartEvent) {
                triggerElements.add(0, fElement);
            } else if((fElement instanceof Activity) && BPMN2Utils.isContainerAdHoc(container)) {
                Activity act = (Activity) fElement;
                if(act.getIncoming() == null || act.getIncoming().size() == 0) {
                    triggerElements.add(0, fElement);
                }
            } else if (fElement instanceof IntermediateCatchEvent) {
                
                IntermediateCatchEvent act = (IntermediateCatchEvent) fElement;
                if(act.getIncoming() == null || act.getIncoming().size() == 0) {
                    triggerElements.add(0, fElement);
                } 
                
                List<EventDefinition> eventDefinitions = ((IntermediateCatchEvent) fElement)
                        .getEventDefinitions();
                processEventDefinitions(fElement, eventDefinitions, catchingEvents);
                
                
            } else if (fElement instanceof BoundaryEvent) {
                List<EventDefinition> eventDefinitions = ((BoundaryEvent) fElement).getEventDefinitions();
                
                processEventDefinitions(fElement, eventDefinitions, catchingEvents);
            }
        }
    }
    
    protected void processEventDefinitions(FlowElement fElement, List<EventDefinition> eventDefinitions, Map<String, FlowElement> catchingEvents) {
        String key = null;
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
                } else if (edef instanceof ErrorEventDefinition) {
                    key = ((ErrorEventDefinition) edef)
                            .getErrorRef().getId();
                }
                if (key != null) {
                    catchingEvents.put(key, fElement);
                }
            }
        }
    }

}

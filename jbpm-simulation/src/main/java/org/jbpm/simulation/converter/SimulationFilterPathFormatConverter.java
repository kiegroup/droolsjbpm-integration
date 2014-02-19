package org.jbpm.simulation.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.ThrowEvent;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathFormatConverter;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;

public class SimulationFilterPathFormatConverter implements
        PathFormatConverter<List<SimulationPath>> {
    
    private SimulationDataProvider provider;
    
    public SimulationFilterPathFormatConverter() {
        
    }

    public SimulationFilterPathFormatConverter(SimulationDataProvider provider) {
        super();
        this.provider = provider;
    }


    public List<SimulationPath> convert(List<PathContext> paths) {
        List<SimulationPath> allPaths = new ArrayList<SimulationPath>();
        double probabilitySummary = 0;
        for (PathContext context : paths) {
            SimulationPath simPath = new SimulationPath();
            simPath.setPathId(context.getPathId());
            
            for (FlowElement fe : context.getPathElements()) {
                if (fe instanceof SequenceFlow) {
                    simPath.addSequenceFlow(fe.getId());
                    simPath.addSequenceFlowSource(fe.getId(), ((SequenceFlow) fe).getSourceRef().getId());
                } else if (fe instanceof BoundaryEvent) {
                  simPath.addBoundaryEventId(fe.getId());  
                } else if (fe instanceof CatchEvent) {
                    CatchEvent act = (CatchEvent) fe;
                    if(act.getIncoming() == null || act.getIncoming().size() == 0) {
                        String ref = processEventDefinitions(((CatchEvent) fe).getEventDefinitions());
                        simPath.setSignalName(ref);
                    }
                } else {
                    simPath.addActivity(fe.getId());
                    if (fe instanceof ThrowEvent) {
                        String ref = processEventDefinitions(((ThrowEvent) fe).getEventDefinitions());
                        if (ref != null) {
                            simPath.addThrowEvent(fe.getId(), ref);
                        }

                    }
                }
                // ensure that only processes that have start nodes will be considered
                if (fe instanceof StartEvent) {
                    simPath.setStartable(true);
                }
            }
            allPaths.add(simPath);
            
            // calcluate path probability if required
            if (provider != null) {
                probabilitySummary += provider.calculatePathProbability(simPath);
            }
        }
        if (provider != null && probabilitySummary != 1) {
            throw new IllegalArgumentException("Process is not valid for simulation - use validation to find errors");
        }
        Collections.sort(allPaths, new Comparator<SimulationPath>() {

            public int compare(SimulationPath o1, SimulationPath o2) {
                double difference = o1.getProbability() - o2.getProbability();
                if (difference > 0) {
                    return -1;
                } else if (difference < 0) {
                    return 1;
                }
                return 0;
            }
        });
        
        return allPaths;
    }

    protected String processEventDefinitions(List<EventDefinition> eventDefinitions) {
        String key = null;
        if (eventDefinitions != null) {
            for (EventDefinition edef : eventDefinitions) {
                if (edef instanceof SignalEventDefinition) {
                    key = ((SignalEventDefinition) edef)
                            .getSignalRef();
                } else if (edef instanceof MessageEventDefinition) {
                    key = "Message-" + ((MessageEventDefinition) edef)
                            .getMessageRef().getId();
                } else if (edef instanceof LinkEventDefinition) {
                    key = ((LinkEventDefinition) edef).getName();
                } else if (edef instanceof CompensateEventDefinition) {
                    key = ((CompensateEventDefinition) edef)
                            .getActivityRef().getId();
                } else if (edef instanceof ErrorEventDefinition) {
                    key = "Error-" + ((ErrorEventDefinition) edef)
                            .getErrorRef().getId();
                }
                if (key != null) {
                    break;
                }
            }
        }
        return key;
    }
}

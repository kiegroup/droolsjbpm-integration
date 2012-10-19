package org.jbpm.simulation.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
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
        
        for (PathContext context : paths) {
            SimulationPath simPath = new SimulationPath();
            simPath.setPathId(context.getPathId());
            
            for (FlowElement fe : context.getPathElements()) {
                if (fe instanceof SequenceFlow) {
                    simPath.addSequenceFlow(fe.getId());
                } else if (fe instanceof BoundaryEvent) {
                  simPath.addBoundaryEventId(fe.getId());  
                } else {
                    simPath.addActivity(fe.getId());
                }
            }
            allPaths.add(simPath);
            
            // calcluate path probability if required
            if (provider != null) {
                provider.calculatePathProbability(simPath);
            }
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
}

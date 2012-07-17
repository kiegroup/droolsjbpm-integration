package org.jbpm.simulation.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathFormatConverter;
import org.jbpm.simulation.impl.SimulationPath;

public class SimulationFilterPathFormatConverter implements
        PathFormatConverter<List<SimulationPath>> {

    public List<SimulationPath> convert(List<PathContext> paths) {
        List<SimulationPath> allPaths = new ArrayList<SimulationPath>();
        
        for (PathContext context : paths) {
            SimulationPath simPath = new SimulationPath();

            for (FlowElement fe : context.getPathElements()) {
                if (fe instanceof SequenceFlow) {
                    simPath.addSequenceFlow(fe.getId());
                } else {
                    simPath.addActivity(fe.getId());
                }
            }
            allPaths.add(simPath);
        }
        return allPaths;
    }

}

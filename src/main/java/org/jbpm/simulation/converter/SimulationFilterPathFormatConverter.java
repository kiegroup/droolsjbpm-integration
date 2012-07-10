package org.jbpm.simulation.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathFormatConverter;

public class SimulationFilterPathFormatConverter implements
        PathFormatConverter<List<List<String>>> {

    public List<List<String>> convert(List<PathContext> paths) {
        List<List<String>> allPaths = new ArrayList<List<String>>();
        
        for (PathContext context : paths) {
            List<String> sequenceFlowsIds = new ArrayList<String>();
            for (FlowElement fe : context.getPathElements()) {
                if (fe instanceof SequenceFlow) {
                    sequenceFlowsIds.add(fe.getId());
                }
            }
            allPaths.add(sequenceFlowsIds);
        }
        return allPaths;
    }

}

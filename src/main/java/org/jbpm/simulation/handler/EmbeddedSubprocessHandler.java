package org.jbpm.simulation.handler;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.jbpm.simulation.PathContextManager;

public class EmbeddedSubprocessHandler extends MainElementHandler {

    
    public void handle(FlowElement element, PathContextManager manager) {
        SubProcess subProcess = ((SubProcess) element);
        
        // process internal nodes of the sub process
        List<FlowElement> sbElements = subProcess.getFlowElements();
        StartEvent start = null;
        for (FlowElement sbElement : sbElements) {
            if (sbElement instanceof StartEvent) {
                start = (StartEvent) sbElement;
                break;
            }
        }
        boolean canBeFinsihed = manager.getContextFromStack().isCanBeFinished();
        manager.getContextFromStack().setCanBeFinished(false);
        super.handle(start, manager);
       
        
        List<SequenceFlow> out = getOutgoing(element);
        for (SequenceFlow flow : out) {
            manager.addToPath(flow, manager.getContextFromStack());
            super.handle(flow.getTargetRef(), manager);
        }
        manager.getContextFromStack().setCanBeFinished(canBeFinsihed);
    }

}

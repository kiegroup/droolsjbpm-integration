package org.jbpm.simulation.handler;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContextManager;

public class DefaultElementHandler extends MainElementHandler {

    public boolean handle(FlowElement element, PathContextManager manager) {
        List<SequenceFlow> outgoing = getOutgoing(element);
        if (outgoing.size() == 0) {
            return false;
        }
        for (SequenceFlow seqFlow : outgoing) {
            FlowElement target = seqFlow.getTargetRef();
            manager.addToPath(seqFlow, manager.getContextFromStack());
            super.handle(target, manager);
        }
        return true;
    }

}

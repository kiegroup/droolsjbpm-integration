package org.jbpm.simulation.handler;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContextManager;

public class DefaultElementHandler extends MainElementHandler {

    public void handle(FlowElement element, PathContextManager manager) {
        
        for (SequenceFlow seqFlow : getOutgoing(element)) {
            FlowElement target = seqFlow.getTargetRef();
            manager.addToPath(seqFlow, manager.getContextFromStack());
            super.handle(target, manager);
        }

    }

}

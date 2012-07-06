package org.jbpm.simulation.handler;

import java.util.List;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContextManager;

public class AdHocSubProcessElementHandler extends MainElementHandler {

    @Override
    public boolean handle(FlowElement element, PathContextManager manager) {
        AdHocSubProcess adHoc = (AdHocSubProcess) element;
        
        List<FlowElement> flowElements = adHoc.getFlowElements();
        
        for (FlowElement fElement : flowElements) {
            if (fElement instanceof Activity) {
                if (((Activity) fElement).getIncoming().size() == 0) {
                    
                    manager.cloneGiven(manager.getContextFromStack());
                    boolean canBeFinsihed = manager.getContextFromStack().isCanBeFinished();
                    manager.getContextFromStack().setCanBeFinished(false);
                    
                    super.handle(fElement, manager);
                    manager.getContextFromStack().setCanBeFinished(canBeFinsihed);
                    List<SequenceFlow> out = getOutgoing(element);
                    for (SequenceFlow flow : out) {
                        manager.addToPath(flow, manager.getContextFromStack());
                        super.handle(flow.getTargetRef(), manager);
                    }
                    
                }
            }
        }
       
       
        return true;
    }

}

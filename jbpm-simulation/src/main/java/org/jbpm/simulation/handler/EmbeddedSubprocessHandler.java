/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation.handler;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContext.Type;
import org.jbpm.simulation.PathContextManager;

public class EmbeddedSubprocessHandler extends MainElementHandler {

    
    public boolean handle(FlowElement element, PathContextManager manager) {
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
        manager.getContextFromStack().setCanBeFinishedNoIncrement(false);
        super.handle(start, manager);
       
        manager.getContextFromStack().setCanBeFinishedNoIncrement(canBeFinsihed);
        List<SequenceFlow> out = getOutgoing(element);
        for (SequenceFlow flow : out) {
            manager.addToPath(flow, manager.getContextFromStack());
            super.handle(flow.getTargetRef(), manager);
        }
        
        if (canBeFinsihed) {
            
            boolean goOn = true;
            while (goOn) {
                PathContext context = manager.getContextFromStack();
                
                if (context.getType() == Type.ACTIVE) {
                    context.setCanBeFinishedNoIncrement(canBeFinsihed);
                    manager.finalizePath();
                } else {
                    break;
                }
            }
        }
        
        
        return true;
    }

}

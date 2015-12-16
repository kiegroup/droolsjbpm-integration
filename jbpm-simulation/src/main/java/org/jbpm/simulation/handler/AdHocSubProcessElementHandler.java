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

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
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

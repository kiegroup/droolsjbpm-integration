/*
 * Copyright 2015 JBoss Inc
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

import java.util.Iterator;
import java.util.List;

import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContext.Type;
import org.jbpm.simulation.PathContextManager;

public class ConvergingGatewayElementHandler extends DefaultElementHandler {
    private PathContextManager manager;

    public boolean handle(FlowElement element, PathContextManager manager) {
        
        this.manager = manager;
        
        if (element instanceof ExclusiveGateway) {
            
            return false;
        } else if (element instanceof InclusiveGateway) {
 
            return true;
        } else if (element instanceof ParallelGateway) {
            handleParallelGateway(element, getOutgoing(element));
            return true;
        } else {
            throw new UnsupportedOperationException("Not supported element to handle " + element.eClass().getName());
        }

    }
    
    protected void handleParallelGateway(FlowElement element, List<SequenceFlow> outgoing) {
        PathContext context = manager.getContextFromStack();
        boolean canBeFinished = context.isCanBeFinished();
        
        if (canBeFinished && context.getType() != Type.ROOT) {

            for (SequenceFlow seqFlow : outgoing) {
                manager.addToPath(seqFlow, context);
                manager.addToPath(seqFlow.getTargetRef(), context);
            }
            
            Iterator<PathContext> it = manager.getPaths().iterator();
            
            while (it.hasNext()) {
                PathContext pathContext = (PathContext) it.next();
                if (pathContext.getType() == Type.ACTIVE) {
                    pathContext.setCanBeFinishedNoIncrement(canBeFinished);
                    manager.finalizePath(pathContext);
                    it.remove();
                }
            }
            
        } else {
            
            super.handle(element, manager);
            
        }

    }
}

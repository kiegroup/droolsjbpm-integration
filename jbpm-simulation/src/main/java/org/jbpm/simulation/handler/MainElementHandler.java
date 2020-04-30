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
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContextManager;
import org.jbpm.simulation.util.BPMN2Utils;

public class MainElementHandler implements ElementHandler {
    
    

    public boolean handle(FlowElement element, PathContextManager manager) {
        PathContext context = manager.getContextFromStack();
        if (!(element instanceof SubProcess)) {
            manager.addToPath(element, context);
        }
        
        List<SequenceFlow> outgoing = getOutgoing(element);

        if (outgoing != null && !outgoing.isEmpty()) {
            boolean handled = false;
            if (element instanceof Gateway) {
                Gateway gateway = ((Gateway) element);
                if (gateway.getGatewayDirection() == GatewayDirection.DIVERGING) {
                    
                    handled = HandlerRegistry.getHandler(element).handle(element, manager);
                } else {
                    if (gateway instanceof ParallelGateway) {
                        handled = HandlerRegistry.getHandler(element).handle(element, manager);
                    } else {
                        handled = HandlerRegistry.getHandler().handle(element, manager);
                    }
                }
            } else if (element instanceof Activity) {
                handled = HandlerRegistry.getHandler(element).handle(element, manager);
            } else if (element instanceof IntermediateThrowEvent) {
                handled = HandlerRegistry.getHandler(element).handle(element, manager);
            } else {
                handled = HandlerRegistry.getHandler().handle(element, manager);
            }
            
            if (!handled && BPMN2Utils.isAdHoc(element)) {
                manager.clearCurrentContext();
            }
        } else {
            ElementHandler handelr = HandlerRegistry.getHandler(element);
            if (handelr != null) {
                boolean handled = handelr.handle(element, manager);
                if (!handled) {
                    manager.finalizePath();
                }
            } else {
                manager.finalizePath();
            }
        }
        
        return true;

    }
    
    protected List<EventDefinition> getEventDefinitions(FlowElement startAt) {
        List<EventDefinition> throwDefinitions = null;

        if (startAt instanceof IntermediateThrowEvent) {
            throwDefinitions = ((IntermediateThrowEvent) startAt)
                    .getEventDefinitions();

        } else if (startAt instanceof EndEvent) {
            EndEvent end = (EndEvent) startAt;

            throwDefinitions = end.getEventDefinitions();
        }

        return throwDefinitions;
    }
    
    
    protected List<SequenceFlow> getOutgoing(FlowElement element) {
        List<SequenceFlow> outgoing = null;
        if (element instanceof StartEvent) {

            outgoing = ((StartEvent) element).getOutgoing();
        } else if (element instanceof SubProcess) {
            
            SubProcess subProcess = ((SubProcess) element);
            outgoing = subProcess.getOutgoing();
        } else if (element instanceof Event) {
            
            outgoing = ((Event) element).getOutgoing();
        } else if (element instanceof Activity) {

            outgoing = ((Activity) element).getOutgoing();
        } else if (element instanceof EndEvent) {
            
            outgoing = ((EndEvent) element).getOutgoing();
        } else if (element instanceof Gateway) {
            
            Gateway gateway = ((Gateway) element);
            outgoing = gateway.getOutgoing();
        }
        
        return outgoing;
    }
    
    

}

package org.jbpm.simulation.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathContextManager;

public class MainElementHandler implements ElementHandler {
    
    protected Map<String, FlowElement> catchingEvents = new HashMap<String, FlowElement>();

    public void handle(FlowElement element, PathContextManager manager) {
        PathContext context = manager.getContextFromStack();
        manager.addToPath(element, context);
        
        List<SequenceFlow> outgoing = getOutgoing(element);
        
        if (element instanceof SubProcess) {
            SubProcess subProcess = ((SubProcess) element);
            HandlerRegistry.getHandler(subProcess).handle(subProcess, manager);
        } 

        if (outgoing != null && !outgoing.isEmpty()) {
            if (element instanceof Gateway) {
                Gateway gateway = ((Gateway) element);
                if (gateway.getGatewayDirection() == GatewayDirection.DIVERGING) {
                    
                    HandlerRegistry.getHandler(element).handle(element, manager);
                } else {
                    HandlerRegistry.getHandler().handle(element, manager);
                }
            } else if (element instanceof Activity) {
                HandlerRegistry.getHandler(element).handle(element, manager);
            } else {
                HandlerRegistry.getHandler().handle(element, manager);
            }
        } else {
            List<EventDefinition> throwDefinitions = getEventDefinitions(element);
            if (throwDefinitions != null && throwDefinitions.size() > 0) {
                for (EventDefinition def : throwDefinitions) {
                    String key = "";
                    if (def instanceof SignalEventDefinition) {
                        key = ((SignalEventDefinition) def).getSignalRef();
                    } else if (def instanceof MessageEventDefinition) {
                        key = ((MessageEventDefinition) def).getMessageRef()
                                .getId();
                    } else if (def instanceof LinkEventDefinition) {
                        key = ((LinkEventDefinition) def).getName();
                    } else if (def instanceof CompensateEventDefinition) {
                        key = ((CompensateEventDefinition) def)
                                .getActivityRef().getId();
                    }

                    FlowElement catchEvent = this.catchingEvents.get(key);
                    if (catchEvent != null) {
                        handle(catchEvent, manager);
                    } else {
                        // not supported event definition
                        manager.finalizePath();
                    }
                }
            } else {
                manager.finalizePath();
            }
        }

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

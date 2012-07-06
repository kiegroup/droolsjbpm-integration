package org.jbpm.simulation.handler;

import java.util.List;

import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.jbpm.simulation.PathContextManager;

public class EventElementHandler extends MainElementHandler {

    @Override
    public boolean handle(FlowElement element, PathContextManager manager) {
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

                FlowElement catchEvent = manager.getCatchingEvents().get(key);
                if (catchEvent != null) {
                    super.handle(catchEvent, manager);
                } else {
                    // not supported event definition
                    manager.finalizePath();
                }
            }
            return true;
        } else {
            return false;
        }
    }

}

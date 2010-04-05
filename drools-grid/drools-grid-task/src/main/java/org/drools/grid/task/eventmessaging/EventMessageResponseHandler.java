package org.drools.grid.task.eventmessaging;

import org.drools.eventmessaging.Payload;
import org.drools.grid.generic.MessageResponseHandler;



public interface EventMessageResponseHandler extends MessageResponseHandler {
    public void execute(Payload payload);
}

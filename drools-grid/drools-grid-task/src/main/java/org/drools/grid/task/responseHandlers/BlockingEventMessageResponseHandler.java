package org.drools.grid.task.responseHandlers;

import org.drools.eventmessaging.Payload;
import org.drools.grid.generic.Message;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingEventResponseHandler;
import org.drools.grid.task.eventmessaging.EventMessageResponseHandler;

public class BlockingEventMessageResponseHandler extends BlockingEventResponseHandler implements EventMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		Payload payload = (Payload) cmd.getArguments().get(0);
		execute(payload);
	}

}
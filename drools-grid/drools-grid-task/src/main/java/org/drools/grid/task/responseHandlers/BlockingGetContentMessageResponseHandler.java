package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.Content;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingGetContentResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.GetContentMessageResponseHandler;

public class BlockingGetContentMessageResponseHandler extends BlockingGetContentResponseHandler implements GetContentMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		Content content = (Content) cmd.getArguments().get(0);
		execute(content);
	}

}
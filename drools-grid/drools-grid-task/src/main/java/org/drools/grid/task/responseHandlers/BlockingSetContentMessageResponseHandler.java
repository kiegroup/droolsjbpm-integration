package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingSetContentResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.SetDocumentMessageResponseHandler;

public class BlockingSetContentMessageResponseHandler extends BlockingSetContentResponseHandler implements SetDocumentMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		long contentId = (Long) cmd.getArguments().get(0);
		execute(contentId);
	}

}
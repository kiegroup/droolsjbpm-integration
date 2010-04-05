package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingAddCommentResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.AddCommentMessageResponseHandler;

public class BlockingAddCommentMessageResponseHandler extends BlockingAddCommentResponseHandler implements AddCommentMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		Long commentId = (Long) cmd.getArguments().get(0);
		execute(commentId);
	}

}
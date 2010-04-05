package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.responsehandlers.BlockingDeleteCommentResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.DeleteCommentMessageResponseHandler;

public class BlockingDeleteCommentMessageResponseHandler extends BlockingDeleteCommentResponseHandler implements DeleteCommentMessageResponseHandler {

	public void receive(Message message) {
		setIsDone(true);
	}

}
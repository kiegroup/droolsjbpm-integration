package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.responsehandlers.BlockingDeleteAttachmentResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.DeleteAttachmentMessageResponseHandler;

public class BlockingDeleteAttachmentMessageResponseHandler extends BlockingDeleteAttachmentResponseHandler implements DeleteAttachmentMessageResponseHandler {

	public void receive(Message message) {
		setDone(true);
	}

}
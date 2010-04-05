package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.TaskOperationMessageResponseHandler;

public class BlockingTaskOperationMessageResponseHandler extends BlockingTaskOperationResponseHandler implements TaskOperationMessageResponseHandler {

	public void receive(Message message) {
		setDone(true);
	}

}
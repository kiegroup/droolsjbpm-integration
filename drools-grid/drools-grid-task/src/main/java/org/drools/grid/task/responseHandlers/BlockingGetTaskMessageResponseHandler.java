package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.Task;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingGetTaskResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.GetTaskMessageResponseHandler;

public class BlockingGetTaskMessageResponseHandler extends BlockingGetTaskResponseHandler implements GetTaskMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		Task task = (Task) cmd.getArguments().get(0);
		execute(task);
	}

}
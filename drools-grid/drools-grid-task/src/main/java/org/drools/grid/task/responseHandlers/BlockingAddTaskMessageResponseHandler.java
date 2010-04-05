package org.drools.grid.task.responseHandlers;

import org.drools.grid.generic.Message;
import org.drools.task.service.Command;
import org.drools.task.service.responsehandlers.BlockingAddTaskResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.AddTaskMessageResponseHandler;

public class BlockingAddTaskMessageResponseHandler extends BlockingAddTaskResponseHandler implements AddTaskMessageResponseHandler {

	public void receive(Message message) {
		Command cmd = (Command) message.getPayload();
		Long taskId = (Long) cmd.getArguments().get(0);
		execute(taskId);
	}

}
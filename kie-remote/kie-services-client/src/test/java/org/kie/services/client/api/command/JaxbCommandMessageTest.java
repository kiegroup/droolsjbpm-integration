package org.kie.services.client.api.command;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.drools.core.command.assertion.AssertEquals;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;


public class JaxbCommandMessageTest {
	
	@Test
	public void testCommandSerialization() throws Exception {
	    String userId = "krisv";
	    long taskId = 1;
	    Command cmd = new StartTaskCommand(taskId, "krisv");
		Command newCmd = testRoundtrip(cmd);
		assertNotNull(newCmd);
		assertEquals("taskId is not equal", taskId, getField("taskId", TaskCommand.class, newCmd));
		assertEquals("userId is not equal", userId, getField("userId", TaskCommand.class, newCmd));
	}
	
	private Object getField(String fieldName, Class<?> clazz, Object obj) throws Exception { 
	    Field field = clazz.getDeclaredField(fieldName);
	    field.setAccessible(true);
	    return field.get(obj);
	}
	
	public <T> Command<T> testRoundtrip(Command<T> command) throws JAXBException {
		String commandXml = JaxbSerializationProvider.convertJaxbObjectToString(new JaxbCommandsRequest("test", command));
		JaxbCommandsRequest newRequest = (JaxbCommandsRequest) JaxbSerializationProvider.convertStringToJaxbObject(commandXml);
		return (Command<T>) newRequest.getCommands().get(0);
	}

	@Test
	public void testTaskSummaryList() throws Exception { 
	    Command<?> cmd = new GetTaskAssignedAsBusinessAdminCommand();
	    List<TaskSummary> result = new ArrayList<TaskSummary>();

	    JaxbCommandsResponse resp = new JaxbCommandsResponse();
	    resp.addResult(result, 0, cmd);
	   
	    cmd = new GetTasksByProcessInstanceIdCommand();
	    List<Long> resultTwo = new ArrayList<Long>();
	    resp.addResult(resultTwo, 1, cmd);
	    
		String commandXml = JaxbSerializationProvider.convertJaxbObjectToString(resp);
		JaxbCommandsResponse newResp = (JaxbCommandsResponse) JaxbSerializationProvider.convertStringToJaxbObject(commandXml);
	}
}

package org.kie.services.client.api.command;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.Context;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;

public class JaxbCommandMessageTest {
	
	@Test
	public void testCommandSerialization() throws JAXBException {
		testRoundtrip(new StartTaskCommand(1, "krisv"));
	}
	
	public <T> void testRoundtrip(Command<T> command) throws JAXBException {
		String commandXml = JaxbSerializationProvider.convertJaxbObjectToString(new JaxbCommandsRequest("test", command));
		JaxbCommandsRequest newRequest = (JaxbCommandsRequest) JaxbSerializationProvider.convertStringToJaxbObject(commandXml);
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

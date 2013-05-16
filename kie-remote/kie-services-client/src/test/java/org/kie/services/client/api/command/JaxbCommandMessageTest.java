package org.kie.services.client.api.command;

import javax.xml.bind.JAXBException;

import org.jbpm.services.task.commands.StartTaskCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbSerializationProvider;

public class JaxbCommandMessageTest {
	
	@Test
	public void testCommandSerialization() throws JAXBException {
		testRoundtrip(new StartTaskCommand(1, "krisv"));
	}
	
	public <T> void testRoundtrip(Command<T> command) throws JAXBException {
		String commandXml = JaxbSerializationProvider.convertJaxbObjectToString(
			new JaxbCommandMessage<T>("test", 1, command));
		System.out.println(commandXml);
		JaxbSerializationProvider.convertStringToJaxbObject(commandXml);
	}

}

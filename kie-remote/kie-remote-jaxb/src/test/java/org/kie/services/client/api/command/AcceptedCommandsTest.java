package org.kie.services.client.api.command;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.drools.core.command.runtime.rule.HaltCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;

public class AcceptedCommandsTest {

    @Test
    public void unsupportedCommandsTest() {
        try {
            JaxbCommandsRequest req = new JaxbCommandsRequest(new HaltCommand());
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        Command [] cmdArrs = { new HaltCommand() };
        List<Command> cmds = Arrays.asList(cmdArrs);
        try {
            JaxbCommandsRequest req = new JaxbCommandsRequest(cmds);
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        try {
            req.setCommands(cmds);
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }
}

/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.serialization;

import java.util.Arrays;
import java.util.List;

import org.drools.core.command.runtime.rule.HaltCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.AbstractRemoteSerializationTest.TestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbProcessInstanceRemoteSerializationTest extends AbstractProcessInstancesRemoteSerializationTest {

    @Override
    public TestType getType() {
        return TestType.JAXB;
    }
    protected static final Logger logger = LoggerFactory.getLogger(JaxbProcessInstanceRemoteSerializationTest.class);

    protected JaxbSerializationProvider jaxbProvider = ClientJaxbSerializationProvider.newInstance();
    {
        jaxbProvider.setPrettyPrint(true);
    }

    @Override
    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClassesAndReinitialize(extraClass);
    }

    @Override
    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }

    @Test
    public void unsupportedCommandsTest() {
        try {
            new JaxbCommandsRequest(new HaltCommand());
            fail("An exception should have been thrown");
        } catch( Exception e ) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        Command[] cmdArrs = { new HaltCommand() };
        List<Command> cmds = Arrays.asList(cmdArrs);
        try {
            new JaxbCommandsRequest(cmds);
            fail("An exception should have been thrown");
        } catch( Exception e ) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        try {
            req.setCommands(cmds);
            fail("An exception should have been thrown");
        } catch( Exception e ) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }
}

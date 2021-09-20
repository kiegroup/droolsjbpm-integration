/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.drools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

public class KieServerBackwardCompatDroolsIntegrationTest extends DroolsKieServerBaseIntegrationTest {
    private static ReleaseId releaseId = new ReleaseId("foo.bar", "baz", "2.1.0.GA");

    private static final String CONTAINER_ID = "kie1";
    private static final String KIE_SESSION = "defaultKieSession";

    private static final String MESSAGE_OUT_IDENTIFIER = "message";
    private static final String MESSAGE_CLASS_NAME = "org.pkg1.Message";
    private static final String MESSAGE_REQUEST = "HelloWorld";
    private static final String MESSAGE_RESPONSE = "echo:HelloWorld";

    private static final String MESSAGE_TEXT_FIELD = "text";

    private static URLClassLoader kjarClassLoader;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(releaseId);

        File jar = KieServerDeployer.getRepository().resolveArtifact(releaseId).getFile();
        kjarClassLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()});

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(MESSAGE_CLASS_NAME, Class.forName(MESSAGE_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testCallContainer() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        KieServerReflections.setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<String> reply = client.executeCommands(CONTAINER_ID, batchExecution);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, KieServerReflections.valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    public void testCallContainerWithStringPayload() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        KieServerReflections.setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        String marshalledCommands = marshaller.marshall(batchExecution);

        ServiceResponse<String> reply = client.executeCommands(CONTAINER_ID, marshalledCommands);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, KieServerReflections.valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    public void testCallContainerRuleClient() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        KieServerReflections.setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, batchExecution);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, KieServerReflections.valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    public void testCallContainerWithStringPayloadRuleClient() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        KieServerReflections.setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        String marshalledCommands = marshaller.marshall(batchExecution);

        ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, marshalledCommands);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, KieServerReflections.valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    public void testCallContainerLookupError() throws Exception {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, "xyz");

        ServiceResponse<String> reply = client.executeCommands(CONTAINER_ID, batchExecution,
                Status.INTERNAL_SERVER_ERROR);
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }
}

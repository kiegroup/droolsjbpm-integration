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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.integrationtests.category.Smoke;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class KieServerDroolsIntegrationTest extends DroolsKieServerBaseIntegrationTest {
    private static ReleaseId releaseId = new ReleaseId("foo.bar", "baz", "2.1.0.GA");
    private static ReleaseId releaseIdScript = new ReleaseId("foo.bar", "baz-script", "2.1.0.GA");

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
        KieServerDeployer.createAndDeployKJar(releaseIdScript);

        File jar = KieServerDeployer.getRepository().resolveArtifact(releaseId).getFile();
        kjarClassLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()});

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(MESSAGE_CLASS_NAME, Class.forName(MESSAGE_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    @Category(Smoke.class)
    public void testCallContainer() throws Exception {
        Object message = createInstance(MESSAGE_CLASS_NAME);
        setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = reply.getResult();
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    public void testCallContainerWithStringPayload() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        String marshalledCommands = marshaller.marshall(batchExecution);

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, marshalledCommands);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = reply.getResult();
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, valueOf(value, MESSAGE_TEXT_FIELD));
    }

    @Test
    @Category(Smoke.class)
    public void testCommandScript() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);
        Object message = createInstance(MESSAGE_CLASS_NAME);
        setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        Command<?> insert = commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER);
        Command<?> fire = commandsFactory.newFireAllRules();
        BatchExecutionCommand batch = commandsFactory.newBatchExecution(Arrays.<Command<?>>asList(insert, fire), KIE_SESSION);

        String payload = marshaller.marshall(batch);

        String containerId = "command-script-container";
        KieServerCommand create = new CreateContainerCommand(new KieContainerResource( containerId, releaseIdScript, null));
        KieServerCommand call = new CallContainerCommand(containerId, payload);
        KieServerCommand dispose = new DisposeContainerCommand(containerId);

        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScript(cmds);

        ServiceResponsesList reply = client.executeScript(script);

        for (ServiceResponse<? extends Object> r : reply.getResponses()) {
            Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, r.getType());
        }
    }

    @Test
    public void testCallContainerLookupError() throws Exception {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, "xyz");

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testCallContainerWithinConversation() throws Exception {
        // Call container info to get conversation Id.
        client.getContainerInfo(CONTAINER_ID);
        String conversationId = client.getConversationId();
        assertNotNull(conversationId);

        Object message = createInstance(MESSAGE_CLASS_NAME);
        setValue(message, MESSAGE_TEXT_FIELD, MESSAGE_REQUEST);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(message, MESSAGE_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        ExecutionResults results = reply.getResult();
        Object value = results.getValue(MESSAGE_OUT_IDENTIFIER);
        Assert.assertEquals(MESSAGE_RESPONSE, valueOf(value, MESSAGE_TEXT_FIELD));

        String afterNextCallConversationId = client.getConversationId();
        assertEquals(conversationId, afterNextCallConversationId);
    }
}

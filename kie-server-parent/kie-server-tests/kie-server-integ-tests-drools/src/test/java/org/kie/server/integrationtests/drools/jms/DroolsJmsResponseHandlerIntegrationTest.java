/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.drools.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.RequestReplyResponseHandler;
import org.kie.server.client.jms.ResponseCallback;
import org.kie.server.client.jms.ResponseHandler;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.drools.DroolsKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.*;

@Category({JMSOnly.class})
public class DroolsJmsResponseHandlerIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "ruleflow-group", "1.0.0.Final");

    private static final String CONTAINER_ID = "ruleflow";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String PROCESS_ID = "simple-ruleflow";
    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        return new ArrayList<Object[]>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration},
                {MarshallingFormat.JSON, jmsConfiguration},
                {MarshallingFormat.XSTREAM, jmsConfiguration}
        }));
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/ruleflow-group").getFile());
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testExecuteSimpleRuleFlowProcessWithAsyncResponseHandler() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()),
                configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback responseCallback = new BlockingResponseCallback(marshaller);
        ResponseHandler asyncResponseHandler = new AsyncResponseHandler(responseCallback);
        ruleClient.setResponseHandler(asyncResponseHandler);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newStartProcess(PROCESS_ID));
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        ServiceResponse<?> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        assertThat(response).isNull();
        ExecutionResults result = responseCallback.get(ExecutionResultImpl.class);

        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).isNotNull();
        assertThat(outcome).containsExactly("Rule from first ruleflow group executed",
                "Rule from second ruleflow group executed");
    }

    @Test
    public void testExecuteSimpleRuleFlowProcessWithFireAndForgetResponseHandler() throws Exception {
        // First command in this test needs to be executed with RequestReplyResponseHandler.
        // If there would be used FireAndForgetResponseHandler then following request could reach Kie server in same time
        // causing stateful kie session for this container to be created concurrently.
        // Concurrent creation of same stateful kie session leads to overriding one kie session with another,
        // causing inconsistency and data loss.
        Command<?> initializeList = commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME);
        ServiceResponse<ExecutionResults> response =  ruleClient.executeCommandsWithResults(CONTAINER_ID, initializeList);
        KieServerAssert.assertSuccess(response);

        try {
            ruleClient.setResponseHandler(new FireAndForgetResponseHandler());
            Command<?> startProcess = commandsFactory.newStartProcess(PROCESS_ID);
            response = ruleClient.executeCommandsWithResults(CONTAINER_ID, startProcess);
            assertThat(response).isNull();
        } finally {
            ruleClient.setResponseHandler(new RequestReplyResponseHandler());
        }

        Command<?> getGlobalCommand = commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME);
        List<String> value = new ArrayList<String>();
        value.add("Rule from first ruleflow group executed");
        value.add("Rule from second ruleflow group executed");
        KieServerSynchronization.waitForCommandResult(ruleClient, CONTAINER_ID, getGlobalCommand, LIST_OUTPUT_NAME, value);
    }

}

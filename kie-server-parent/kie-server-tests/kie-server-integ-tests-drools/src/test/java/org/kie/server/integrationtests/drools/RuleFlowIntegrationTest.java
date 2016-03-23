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

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.rule.ClearRuleFlowGroupCommand;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;

public class RuleFlowIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "ruleflow-group",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "ruleflow";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String KIE_SESSION_STATELESS = "defaultStatelessKieSession";
    private static final String PROCESS_ID = "simple-ruleflow";
    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String RULEFLOW_GROUP_1 = "ruleflow-group1";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/ruleflow-group").getFile());
    }

    @Test
    public void testExecuteSimpleRuleFlowProcess() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newStartProcess(PROCESS_ID));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(2, outcome.size());

        assertEquals("Rule from first ruleflow group executed", outcome.get(0));
        assertEquals("Rule from second ruleflow group executed", outcome.get(1));
    }

    @Test
    public void testExecuteSimpleRuleFlowProcessInStatelessSession() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION_STATELESS);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newStartProcess(PROCESS_ID));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(2, outcome.size());

        assertEquals("Rule from first ruleflow group executed", outcome.get(0));
        assertEquals("Rule from second ruleflow group executed", outcome.get(1));
    }

    @Test
    public void testClearRuleFlowGroup() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        // Replace if/after Clear command is added to command factory.
        // commands.add(commandsFactory.newClearRuleFlowGroup(RULEFLOW_GROUP_1));
        commands.add(new ClearRuleFlowGroupCommand(RULEFLOW_GROUP_1));
        commands.add(commandsFactory.newStartProcess(PROCESS_ID));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(1, outcome.size());

        assertEquals("Rule from second ruleflow group executed", outcome.get(0));
    }
}

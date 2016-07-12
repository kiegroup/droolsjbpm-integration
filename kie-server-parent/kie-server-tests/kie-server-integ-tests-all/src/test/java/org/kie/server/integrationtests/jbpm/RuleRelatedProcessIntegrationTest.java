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

package org.kie.server.integrationtests.jbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.TaskSummary;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;


public class RuleRelatedProcessIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testProcessWithBusinessRuleTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, "business-rule-task");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Object person = createPersonInstance(USER_YODA);

        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("Before rule", taskSummary.getName());


            // let insert person as fact into working memory
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension


            commands.add(commandsFactory.newSetGlobal("people", new ArrayList()));
            commands.add(commandsFactory.newInsert(person, "person-yoda"));


            ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            // check if it was moved to another human task
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("After rule", taskSummary.getName());

            // now let's check if the rule fired
            commands.clear();
            commands.add(commandsFactory.newGetGlobal("people"));

            executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension
            reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            ExecutionResults actualData = reply.getResult();
            assertNotNull(actualData);

        } finally {
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            commands.add(commandsFactory.newFireAllRules());

            ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }


    @Test
    public void testProcessWithConditionalEvent() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, "conditionalevent");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("Before rule", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(0, taskList.size());


            // let insert person as fact into working memory
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            Object person = createPersonInstance(USER_YODA);
            commands.add(commandsFactory.newInsert(person, "person-yoda"));


            ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            // check if it was moved to another human task
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("After rule", taskSummary.getName());


        } finally {

            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            commands.add(commandsFactory.newFireAllRules());

            ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
}

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

package org.kie.server.integrationtests.jbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.TaskSummary;

import static org.junit.Assert.*;


public class RuleRelatedProcessIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testProcessWithBusinessRuleTask() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), marshallingFormat, kieContainer.getClassLoader());

        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, "business-rule-task");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Object person = createPersonInstance("yoda");

        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("Before rule", taskSummary.getName());


            // let insert person as fact into working memory
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension


            commands.add(commandsFactory.newSetGlobal("people", new ArrayList()));
            commands.add(commandsFactory.newInsert(person, "person-yoda"));


            ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), "yoda", taskOutcome);

            // check if it was moved to another human task
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("After rule", taskSummary.getName());

            // now let's check if the rule fired
            commands.clear();
            commands.add(commandsFactory.newGetGlobal("people"));

            executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension
            reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            ExecutionResultImpl actualData = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
            assertNotNull(actualData);

        } finally {
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            commands.add(commandsFactory.newFireAllRules());

            ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }


    @Test
    public void testProcessWithConditionalEvent() throws Exception {

        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, "conditionalevent");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("Before rule", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), "yoda", taskOutcome);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(0, taskList.size());


            // let insert person as fact into working memory
            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            Object person = createPersonInstance("yoda");
            commands.add(commandsFactory.newInsert(person, "person-yoda"));


            ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            // check if it was moved to another human task
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("After rule", taskSummary.getName());


        } finally {

            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID); // use container id as ksession id to use ksession from jBPM extension

            commands.add(commandsFactory.newFireAllRules());

            ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
            assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
}

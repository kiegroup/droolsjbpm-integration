/*
 * Copyright 2020 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import javax.mail.MessagingException;

import com.myspace.test.data.Person;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.subethamail.wiser.Wiser;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

public class RedeploymentIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    public static final String CONTAINER_SIMPLE_USER_TASK = "SimpleUserTask";
    public static final String PROCESS_ID_SIMPLE_USER_TASK = "test-snapshot.SimpleUserTask";

    private static final KieServerConfigItem PPI_RUNTIME_STRATEGY = new KieServerConfigItem(
                                                                                            KieServerConstants.PCFG_RUNTIME_STRATEGY,
                                                                                            RuntimeStrategy.PER_PROCESS_INSTANCE.name(),
                                                                                            String.class.getName());

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "test-snapshot", "1.0.0-SNAPSHOT");
    private static ReleaseId updateReleaseId = new ReleaseId("org.kie.server.testing", "test-snapshot", "1.0.0-SNAPSHOT");

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration configuration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]{{
                                                                                                   MarshallingFormat.JSON,
                                                                                                   configuration}}));

        return parameterData;
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        super.addExtraCustomClasses(extraClasses);
        extraClasses.put(Person.class.getName(), Person.class);
    }

    @Test
    public void testRedeploymentSnapshot() throws InterruptedException, MessagingException, Exception {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/test-snapshot-01");
        createContainer(CONTAINER_SIMPLE_USER_TASK, releaseId, PPI_RUNTIME_STRATEGY);

        Long processInstanceId = processClient.startProcess(CONTAINER_SIMPLE_USER_TASK, PROCESS_ID_SIMPLE_USER_TASK,
                                                            emptyMap());
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(SALABOY, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            Long taskId = taskSummary.getId();

            TaskInstance taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(SALABOY, taskInstance.getActualOwner());

            taskClient.startTask(CONTAINER_SIMPLE_USER_TASK, taskId, SALABOY);

            taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(SALABOY, taskInstance.getActualOwner());

            Map<String, Object> task_inputs = new HashMap<>();
            task_inputs.put("wih_person", new Person("name", "surname", null));
            taskClient.completeTask(CONTAINER_SIMPLE_USER_TASK, taskId, SALABOY, task_inputs);

            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_SIMPLE_USER_TASK,
                                                                               processInstanceId);
            assertNotNull(processInstance);
            assertEquals((int) processInstance.getState(), STATE_COMPLETED);

            List<VariableInstance> desc = processClient.findVariablesCurrentState(CONTAINER_SIMPLE_USER_TASK, processInstanceId);

            Optional<VariableInstance> var = desc.stream().filter(e -> e.getVariableName().equals("person")).findAny();
            assertEquals("person [name=name, surname=surname]", var.get().getValue());

            // update container and try again
            KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/test-snapshot-02");
            updateContainer(CONTAINER_SIMPLE_USER_TASK, updateReleaseId);

            
            processInstanceId = processClient.startProcess(CONTAINER_SIMPLE_USER_TASK, PROCESS_ID_SIMPLE_USER_TASK,
                                                           emptyMap());
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId > 0);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(SALABOY, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            taskId = taskSummary.getId();

            taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(SALABOY, taskInstance.getActualOwner());

            taskClient.startTask(CONTAINER_SIMPLE_USER_TASK, taskId, SALABOY);

            taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(SALABOY, taskInstance.getActualOwner());

            task_inputs = new HashMap<>();
            task_inputs.put("wih_person", new Person("name", "surname", "email"));
            taskClient.completeTask(CONTAINER_SIMPLE_USER_TASK, taskId, SALABOY, task_inputs);

            processInstance = processClient.getProcessInstance(CONTAINER_SIMPLE_USER_TASK, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(STATE_COMPLETED, processInstance.getState().intValue());
            desc = processClient.findVariablesCurrentState(CONTAINER_SIMPLE_USER_TASK, processInstanceId);
            var = desc.stream().filter(e -> e.getVariableName().equals("person")).findAny();
            assertEquals("person [name=name, surname=surname, email=email]", var.get().getValue());

        } catch (Exception e) {
            processClient.abortProcessInstance(CONTAINER_SIMPLE_USER_TASK, processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    private static final long SERVICE_TIMEOUT = 30000;
    private static final long TIMEOUT_BETWEEN_CALLS = 200;

    protected void waitForEmailsRecieve(Wiser wiser) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            if (wiser.getMessages().isEmpty()) {
                Thread.sleep(TIMEOUT_BETWEEN_CALLS);
            } else {
                return;
            }
        }
        throw new TimeoutException("Timeout while waiting for process instance to finish.");
    }

    protected void waitForAssign(Long taskId, String potencialOwner) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            if (taskClient.findTasksAssignedAsPotentialOwner(potencialOwner, 0, 10).isEmpty()) {
                Thread.sleep(TIMEOUT_BETWEEN_CALLS);
            } else {
                return;
            }
        }
        throw new TimeoutException("Timeout while waiting for process instance to finish.");
    }
}

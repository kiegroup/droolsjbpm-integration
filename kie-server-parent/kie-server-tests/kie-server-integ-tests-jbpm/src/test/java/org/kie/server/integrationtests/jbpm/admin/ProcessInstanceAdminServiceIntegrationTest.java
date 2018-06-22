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

package org.kie.server.integrationtests.jbpm.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ProcessNode;
import org.kie.server.api.model.admin.TimerInstance;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class ProcessInstanceAdminServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testCancelAndTrigger() throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<NodeInstance> activeNodeInstances = processAdminClient.getActiveNodeInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(activeNodeInstances);
            assertEquals(1, activeNodeInstances.size());

            NodeInstance active = activeNodeInstances.get(0);
            assertEquals("Evaluate items?", active.getName());

            processAdminClient.cancelNodeInstance(CONTAINER_ID, processInstanceId, active.getId());

            activeNodeInstances = processAdminClient.getActiveNodeInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(activeNodeInstances);
            assertEquals(0, activeNodeInstances.size());

            List<ProcessNode> processNodes = processAdminClient.getProcessNodes(CONTAINER_ID, processInstanceId);
            ProcessNode first = processNodes.stream().filter(pn -> pn.getNodeName().equals("Evaluate items?")).findFirst().orElse(null);
            assertNotNull(first);

            processAdminClient.triggerNode(CONTAINER_ID, processInstanceId, first.getNodeId());

            activeNodeInstances = processAdminClient.getActiveNodeInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(activeNodeInstances);
            assertEquals(1, activeNodeInstances.size());

            NodeInstance activeTriggered = activeNodeInstances.get(0);
            assertEquals("Evaluate items?", activeTriggered.getName());

            assertFalse(activeTriggered.getId().longValue() == active.getId().longValue());

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }

    }

    @Test
    public void testRetrigger() throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<NodeInstance> activeNodeInstances = processAdminClient.getActiveNodeInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(activeNodeInstances);
            assertEquals(1, activeNodeInstances.size());

            NodeInstance active = activeNodeInstances.get(0);
            assertEquals("Evaluate items?", active.getName());

            processAdminClient.retriggerNodeInstance(CONTAINER_ID, processInstanceId, active.getId());

            activeNodeInstances = processAdminClient.getActiveNodeInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(activeNodeInstances);
            assertEquals(1, activeNodeInstances.size());

            NodeInstance activeTriggered = activeNodeInstances.get(0);
            assertEquals("Evaluate items?", activeTriggered.getName());

            assertFalse(activeTriggered.getId().longValue() == active.getId().longValue());

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }

    }

    @Test(timeout = 60 * 1000)
    public void testUpdateTimer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timer", "1h");
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            List<TimerInstance> timers = processAdminClient.getTimerInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(timers);
            assertEquals(1, timers.size());

            TimerInstance timerInstance = timers.get(0);
            assertNotNull(timerInstance);
            assertEquals("timer", timerInstance.getTimerName());

            processAdminClient.updateTimer(CONTAINER_ID, processInstanceId, timerInstance.getTimerId(), 3, 0, 0);

            KieServerSynchronization.waitForProcessInstanceToFinish(processClient, CONTAINER_ID, processInstanceId);

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }

    @Test(timeout = 60 * 1000)
    public void testUpdateTimerRelative() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timer", "1h");
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            List<TimerInstance> timers = processAdminClient.getTimerInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(timers);
            assertEquals(1, timers.size());

            TimerInstance timerInstance = timers.get(0);
            assertNotNull(timerInstance);
            assertEquals("timer", timerInstance.getTimerName());

            processAdminClient.updateTimerRelative(CONTAINER_ID, processInstanceId, timerInstance.getTimerId(), 3, 0, 0);

            KieServerSynchronization.waitForProcessInstanceToFinish(processClient, CONTAINER_ID, processInstanceId);

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }

    @Test
    public void testErrorHandlingFailedToStart() throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("timer", "invalid value");

        try {
            processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
            fail("Process instance should fail as it has invalid timer expression");
        } catch (KieServicesException e) {
            // expected as the variable to configure timer duration is invalid
        }
        List<ExecutionErrorInstance> errors = processAdminClient.getErrors(CONTAINER_ID, false, 0, 10);
        errors = filterErrorsByProcessId(errors, PROCESS_ID_TIMER);
        assertNotNull(errors);
        assertEquals(1, errors.size());

        ExecutionErrorInstance errorInstance = errors.get(0);
        assertNotNull(errorInstance.getErrorId());
        assertNull(errorInstance.getError());
        assertNotNull(errorInstance.getProcessInstanceId());
        assertNotNull(errorInstance.getActivityId());
        assertNotNull(errorInstance.getErrorDate());

        assertEquals(CONTAINER_ID, errorInstance.getContainerId());
        assertEquals(PROCESS_ID_TIMER, errorInstance.getProcessId());
        assertEquals("timer", errorInstance.getActivityName());

        assertFalse(errorInstance.isAcknowledged());
        assertNull(errorInstance.getAcknowledgedAt());
        assertNull(errorInstance.getAcknowledgedBy());

        processAdminClient.acknowledgeError(CONTAINER_ID, errorInstance.getErrorId());

        errors = processAdminClient.getErrors(CONTAINER_ID, false, 0, 10);
        errors = filterErrorsByProcessId(errors, PROCESS_ID_TIMER);
        assertNotNull(errors);
        assertEquals(0, errors.size());

        errorInstance = processAdminClient.getError(CONTAINER_ID, errorInstance.getErrorId());
        assertNotNull(errorInstance);
        assertNotNull(errorInstance.getErrorId());
        assertTrue(errorInstance.isAcknowledged());
        assertNotNull(errorInstance.getAcknowledgedAt());
        assertEquals(USER_YODA, errorInstance.getAcknowledgedBy());
    }

    @Test
    public void testErrorHandlingFailedToSignal() throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("nullAccepted", false);
        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, parameters);

            List<ExecutionErrorInstance> errors = processAdminClient.getErrorsByProcessInstance(CONTAINER_ID, processInstanceId, false, 0, 10);
            assertNotNull(errors);
            assertEquals(0, errors.size());

            try {
                processClient.signalProcessInstance(CONTAINER_ID, processInstanceId, "Signal1", null);
                fail("Process instance signal fail as it provides null as event");
            } catch (KieServicesException e) {
                // expected
            }

            errors = processAdminClient.getErrorsByProcessInstance(CONTAINER_ID, processInstanceId, false, 0, 10);
            assertNotNull(errors);
            assertEquals(1, errors.size());
            ExecutionErrorInstance errorInstance = errors.get(0);
            assertNotNull(errorInstance.getErrorId());
            assertNull(errorInstance.getError());
            assertNotNull(errorInstance.getProcessInstanceId());
            assertNotNull(errorInstance.getActivityId());
            assertNotNull(errorInstance.getErrorDate());

            assertEquals(CONTAINER_ID, errorInstance.getContainerId());
            assertEquals(PROCESS_ID_SIGNAL_PROCESS, errorInstance.getProcessId());
            assertEquals("Signal 1 data", errorInstance.getActivityName());

            assertFalse(errorInstance.isAcknowledged());
            assertNull(errorInstance.getAcknowledgedAt());
            assertNull(errorInstance.getAcknowledgedBy());

            errors = processAdminClient.getErrorsByProcessInstanceAndNode(CONTAINER_ID, processInstanceId, "Signal 1 data", false, 0, 10);
            assertNotNull(errors);
            assertEquals(1, errors.size());
            ExecutionErrorInstance errorInstance2 = errors.get(0);
            assertEquals(errorInstance.getErrorId(), errorInstance2.getErrorId());

            processAdminClient.acknowledgeError(CONTAINER_ID, errorInstance.getErrorId());

            errors = processAdminClient.getErrorsByProcessInstance(CONTAINER_ID, processInstanceId, false, 0, 10);
            assertNotNull(errors);
            assertEquals(0, errors.size());

            errorInstance = processAdminClient.getError(CONTAINER_ID, errorInstance.getErrorId());
            assertNotNull(errorInstance);
            assertNotNull(errorInstance.getErrorId());
            assertTrue(errorInstance.isAcknowledged());
            assertNotNull(errorInstance.getAcknowledgedAt());
            assertEquals(USER_YODA, errorInstance.getAcknowledgedBy());
        } catch (KieServicesException e) {
            logger.error("Unexpected error", e);
            fail(e.getMessage());
        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testErrorHandlingFailedToStartBulkAck() throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("timer", "invalid value");

        try {
            processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
            fail("Process instance should fail as it has invalid timer expression");
        } catch (KieServicesException e) {
            // expected as the variable to configure timer duration is invalid
        }
        try {
            processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
            fail("Process instance should fail as it has invalid timer expression");
        } catch (KieServicesException e) {
            // expected as the variable to configure timer duration is invalid
        }
        List<ExecutionErrorInstance> errors = processAdminClient.getErrors(CONTAINER_ID, false, 0, 10);
        errors = filterErrorsByProcessId(errors, PROCESS_ID_TIMER);
        assertNotNull(errors);
        assertEquals(2, errors.size());

        ExecutionErrorInstance errorInstance = errors.get(0);

        assertFalse(errorInstance.isAcknowledged());
        assertNull(errorInstance.getAcknowledgedAt());
        assertNull(errorInstance.getAcknowledgedBy());

        ExecutionErrorInstance errorInstance2 = errors.get(1);

        assertFalse(errorInstance2.isAcknowledged());
        assertNull(errorInstance2.getAcknowledgedAt());
        assertNull(errorInstance2.getAcknowledgedBy());

        processAdminClient.acknowledgeError(CONTAINER_ID, errorInstance.getErrorId(), errorInstance2.getErrorId());

        errors = processAdminClient.getErrors(CONTAINER_ID, false, 0, 10);
        errors = filterErrorsByProcessId(errors, PROCESS_ID_TIMER);
        assertNotNull(errors);
        assertEquals(0, errors.size());

        errorInstance = processAdminClient.getError(CONTAINER_ID, errorInstance.getErrorId());
        assertNotNull(errorInstance);
        assertNotNull(errorInstance.getErrorId());
        assertTrue(errorInstance.isAcknowledged());
        assertNotNull(errorInstance.getAcknowledgedAt());
        assertEquals(USER_YODA, errorInstance.getAcknowledgedBy());
    }

}

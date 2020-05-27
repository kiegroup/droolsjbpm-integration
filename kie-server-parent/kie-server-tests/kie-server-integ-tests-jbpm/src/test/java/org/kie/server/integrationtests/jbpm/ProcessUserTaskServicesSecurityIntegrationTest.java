/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;;

public class ProcessUserTaskServicesSecurityIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "secured-project",
            "1.0.0.Final");
    
    private static final String NO_PERMISSION_MSG = "User %s does not have permission to access this asset";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/secured-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID_SECURED, releaseId);
    }

    @Test
    public void testNonAllowedUserCannotStartProcess(){
        assertClientException(
                () -> processClient.startProcess(CONTAINER_ID_SECURED, PROCESS_ID_USERTASK_SECURED),
                403,
                String.format(NO_PERMISSION_MSG, USER_YODA));
    }
    
    @Test
    public void testNonAllowedUserCannotStartProcessFromNodeIds() throws Exception{
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        try {
            processClient.abortProcessInstance(CONTAINER_ID_SECURED, processInstanceId);

            List<NodeInstance> list = this.processClient.findNodeInstancesByType(CONTAINER_ID_SECURED, processInstanceId, "ABORTED", 0, 10);
            String[] nodeIds = list.stream().map(NodeInstance::getNodeId).toArray(String[]::new);

            processInstanceId = null;
            changeUser(USER_YODA);
            assertClientException(
                () -> processClient.startProcessFromNodeIds(CONTAINER_ID_SECURED, PROCESS_ID_USERTASK_SECURED, parameters, nodeIds),
                403,
                String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            if (processInstanceId != null) 
                abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedUserCannotStartProcessFromNodeIdsWithCorrelationKey() throws Exception{
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        try {
            processClient.abortProcessInstance(CONTAINER_ID_SECURED, processInstanceId);

            List<NodeInstance> list = this.processClient.findNodeInstancesByType(CONTAINER_ID_SECURED, processInstanceId, "ABORTED", 0, 10);
            String[] nodeIds = list.stream().map(NodeInstance::getNodeId).toArray(String[]::new);

            CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
            CorrelationKey firstKey = correlationKeyFactory.newCorrelationKey("mysimlekey");
            processInstanceId = null;
            changeUser(USER_YODA);
            assertClientException(
                () -> processClient.startProcessFromNodeIds(CONTAINER_ID_SECURED, PROCESS_ID_USERTASK_SECURED, firstKey, parameters, nodeIds),
                403,
                String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            if (processInstanceId != null) 
                abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedUserCannotStartProcessWithCorrelationKey(){
        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
        CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey("key");
        assertClientException(
                () -> processClient.startProcess(CONTAINER_ID_SECURED, PROCESS_ID_USERTASK_SECURED, correlationKey),
                403,
                String.format(NO_PERMISSION_MSG, USER_YODA));
    }
    
    @Test
    public void testNonAllowedUserCannotAbortProcess() throws Exception{
       Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
       try{
           changeUser(USER_YODA);
           assertClientException(
                        () -> processClient.abortProcessInstance(CONTAINER_ID_SECURED, processInstanceId),
                        403,
                        String.format(NO_PERMISSION_MSG, USER_YODA));
       } finally {
           abortProcessAsAdministrator(processInstanceId);
           changeUser(TestConfig.getUsername());
       }
    }

    @Test
    public void testNonAllowedUserCannotAbortMultipleProcesses() throws Exception {
        Long processInstanceId1 = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        Long processInstanceId2 = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);

        List<Long> processInstances = new ArrayList<>(
                Arrays.asList(processInstanceId1, processInstanceId2));
        try {
            changeUser(USER_YODA);
            assertClientException(
                         () -> processClient.abortProcessInstances(CONTAINER_ID_SECURED, processInstances),
                         403,
                         String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId1);
            abortProcessAsAdministrator(processInstanceId2);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testNonAllowedUserCannotSignalProcess() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_SIGNAL_PROCESS_SECURED);
        try{
            changeUser(USER_YODA);
            assertClientException(
                         () -> processClient.signalProcessInstance(CONTAINER_ID_SECURED, processInstanceId, "Signal2", "My custom string event"),
                         403,
                         String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testNonAllowedUserCannotSignalMultipleProcesses() throws Exception {
        Long processInstanceId1 = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        Long processInstanceId2 = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);

        List<Long> processInstances = new ArrayList<>(
                Arrays.asList(processInstanceId1, processInstanceId2));
        try {
            changeUser(USER_YODA);
            assertClientException(
                         () -> processClient.signalProcessInstances(CONTAINER_ID_SECURED, processInstances, "Signal2", "My custom string event"),
                         403,
                         String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId1);
            abortProcessAsAdministrator(processInstanceId2);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testNonAllowedUserCannotSetProcessVariables() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        try{
           changeUser(USER_YODA);
           assertClientException(
                        () -> processClient.setProcessVariables(CONTAINER_ID_SECURED, processInstanceId, new HashMap<String,Object>()),
                        403,
                        String.format(NO_PERMISSION_MSG, USER_YODA));
       } finally {
           abortProcessAsAdministrator(processInstanceId);
           changeUser(TestConfig.getUsername());
       }
    }

    @Test
    public void testNonAllowedUserCannotSetProcessVariable() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        try{
           changeUser(USER_YODA);
           assertClientException(
                        () -> processClient.setProcessVariable(CONTAINER_ID_SECURED, processInstanceId, "stringData", "custom value"),
                        403,
                        String.format(NO_PERMISSION_MSG, USER_YODA));
       } finally {
           abortProcessAsAdministrator(processInstanceId);
           changeUser(TestConfig.getUsername());
       }
    }

    @Test
    public void testNonAllowedUserCannotGetWorkItemByProcessInstance() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_WORKITEM_SECURED);
        try {
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.getWorkItemByProcessInstance(CONTAINER_ID_SECURED, processInstanceId),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedUserCannotGetWorkItem() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_WORKITEM_SECURED);
        try {
            Long workItemId = getWorkItem(processInstanceId);
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.getWorkItem(CONTAINER_ID_SECURED, processInstanceId, workItemId),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testNonAllowedUserCannotCompleteWorkItem() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_WORKITEM_SECURED);
        try {
            Long workItemId = getWorkItem(processInstanceId);
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.completeWorkItem(CONTAINER_ID_SECURED, processInstanceId, workItemId, new HashMap<String,Object>()),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedUserCannotAbortWorkItem() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_WORKITEM_SECURED);
        try {
            Long workItemId = getWorkItem(processInstanceId);
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.abortWorkItem(CONTAINER_ID_SECURED, processInstanceId, workItemId),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedUserForStartingProcessCanCompleteTask() throws Exception{
        try {
            Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
            
            changeUser(USER_YODA);
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertThat(taskList).isNotNull().hasSize(1);
            Long taskId = taskList.get(0).getId();
            
            taskClient.startTask(CONTAINER_ID_SECURED, taskId, USER_YODA);
            taskClient.completeAutoProgress(CONTAINER_ID_SECURED, taskId, USER_YODA, new HashMap<String,Object>());
            
            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID_SECURED, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(STATE_COMPLETED, processInstance.getState().intValue());
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedGetProcessInstanceVariable() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        
        try{
            changeUser(USER_YODA);
            assertClientException(
                () -> processClient.getProcessInstanceVariable(CONTAINER_ID_SECURED, processInstanceId, "myVar"),
                403,
                String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedGetProcessInstanceVariables() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        try{
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.getProcessInstanceVariables(CONTAINER_ID_SECURED, processInstanceId),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testNonAllowedGetAvailableSignals() throws Exception{
        Long processInstanceId = startProcessAsAdministrator(PROCESS_ID_USERTASK_SECURED);
        
        try{
            changeUser(USER_YODA);
            assertClientException(
                    () -> processClient.getAvailableSignals(CONTAINER_ID_SECURED, processInstanceId),
                    403,
                    String.format(NO_PERMISSION_MSG, USER_YODA));
        } finally {
            abortProcessAsAdministrator(processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    private Long startProcessAsAdministrator(String processId) throws Exception {
        changeUser(USER_ADMINISTRATOR);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID_SECURED, processId);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        return processInstanceId;
    }

    private void abortProcessAsAdministrator(Long processInstanceId) throws Exception {
        if (processInstanceId != null) {
            changeUser(USER_ADMINISTRATOR);
            processClient.abortProcessInstance(CONTAINER_ID_SECURED, processInstanceId);
        }
    }

    private Long getWorkItem(Long processInstanceId) {
        List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance(CONTAINER_ID_SECURED, processInstanceId);
        assertThat(workItems).isNotNull().hasSize(1);
        return workItems.get(0).getId();
    }
}

/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring.jbpm.services;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/jbpm/services/resource-local-services.xml"})
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class TxOnMethodResourceLocalJbpmServicesTest extends AbstractJbpmServicesTest {

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private UserTaskService userTaskService;

    @Autowired
    private ProcessService processService;

    @After
    public void cleanup() {
        deploymentService.undeploy(new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION));
    }

    @Test
    public void testWithPerProcessInstanceStrategy() {

        testProcessWithStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
    }

    @Test
    public void testWithPerRequestStrategy() {

        testProcessWithStrategy(RuntimeStrategy.PER_REQUEST);
    }

    @Test
    public void testWithSingletonStrategy() {

        testProcessWithStrategy(RuntimeStrategy.SINGLETON);
    }

    private void testProcessWithStrategy(RuntimeStrategy strategy) {

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentUnit.setStrategy(strategy);

        deploymentService.deploy(deploymentUnit);
        String userId = "john";

        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "hiring");

        List<Long> taskIdList = runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
        Assertions.assertThat(taskIdList).hasSize(1);

        long taskId = taskIdList.get(0);
        userTaskService.claim(taskId, userId);
        userTaskService.start(taskId, userId);
        userTaskService.complete(taskId, userId, null);

        List<Status> taskStatus = new ArrayList<Status>();
        taskStatus.add(Status.Completed);
        taskStatus.add(Status.Created);
        taskStatus.add(Status.InProgress);
        taskStatus.add(Status.Reserved);

        List<TaskSummary> taskSummaryList = runtimeDataService.getTasksByStatusByProcessInstanceId(processInstanceId, taskStatus, null);
        Assertions.assertThat(taskSummaryList).hasSize(1);

        processService.abortProcessInstance(processInstanceId);

        ProcessInstanceDesc pi = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertNotNull(pi);
        assertEquals(ProcessInstance.STATE_ABORTED, pi.getState().intValue());
    }



}

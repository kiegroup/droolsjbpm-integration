/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import java.util.Date;

import org.jbpm.kie.services.impl.model.UserTaskInstanceDesc;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;

import static org.junit.Assert.assertEquals;


public class ConvertUtilsTest {

    @Test
    public void testConvertToTask() {
        Date testDate = new Date();
        UserTaskInstanceDesc userTaskWithSla =
                new UserTaskInstanceDesc(Long.valueOf(1), "Ready", testDate, "Task1_name", "Task1_desc", 1,
                                         "Task1_Owner", "Task1_Creator", "deployment", "Process", 1L, testDate,
                                         testDate, 1L, "formName-1", "subject-1", testDate, ProcessInstance.SLA_PENDING);
        UserTaskInstanceDesc userTask =
                new UserTaskInstanceDesc(Long.valueOf(1), "Ready", testDate, "Task1_name", "Task1_desc", 1,
                                         "Task1_Owner", "Task1_Creator", "deployment", "Process", 1L, testDate,
                                         testDate);
        verifyTaskInstanceEqualsUserTaskInstance(userTask, ConvertUtils.convertToTask(userTask));
        verifyTaskInstanceEqualsUserTaskInstance(userTaskWithSla, ConvertUtils.convertToTask(userTaskWithSla));
    }

    private void verifyTaskInstanceEqualsUserTaskInstance(UserTaskInstanceDesc userTask, TaskInstance taskInstance) {
        assertEquals(userTask.getTaskId(), taskInstance.getId());
        assertEquals(userTask.getName(), taskInstance.getName());
        assertEquals(userTask.getProcessId(), taskInstance.getProcessId());
        assertEquals(userTask.getProcessInstanceId(), taskInstance.getProcessInstanceId());
        assertEquals(userTask.getActivationTime(), taskInstance.getActivationTime());
        assertEquals(userTask.getActualOwner(), taskInstance.getActualOwner());
        assertEquals(userTask.getDeploymentId(), taskInstance.getContainerId());
        assertEquals(userTask.getCreatedBy(), taskInstance.getCreatedBy());
        assertEquals(userTask.getCreatedOn(), taskInstance.getCreatedOn());
        assertEquals(userTask.getDescription(), taskInstance.getDescription());
        assertEquals(userTask.getDueDate(), taskInstance.getExpirationDate());
        assertEquals(userTask.getStatus(), taskInstance.getStatus());
        assertEquals(userTask.getPriority(), taskInstance.getPriority());
        assertEquals(userTask.getWorkItemId(), taskInstance.getWorkItemId());
        assertEquals(userTask.getSlaDueDate(), taskInstance.getSlaDueDate());
        assertEquals(userTask.getSlaCompliance(), taskInstance.getSlaCompliance());
        assertEquals(userTask.getSubject(), taskInstance.getSubject());
        assertEquals(userTask.getFormName(), taskInstance.getFormName());
    }
}

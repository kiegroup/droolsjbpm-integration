/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.junit.Test;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.Completed;
import static org.kie.api.task.model.Status.Created;
import static org.kie.api.task.model.Status.Error;
import static org.kie.api.task.model.Status.Exited;
import static org.kie.api.task.model.Status.Failed;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Obsolete;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.services.taskassigning.runtime.command.DelegateAndSaveCommand.TASK_MODIFIED_ERROR_MSG;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DelegateAndSaveCommandTest extends AbstractPlanningCommandTest<DelegateAndSaveCommand> {

    @Mock
    private Task task;

    @Mock
    private org.kie.api.task.model.TaskData taskData;

    @Mock
    private PeopleAssignments peopleAssignments;

    private List<OrganizationalEntity> potentialOwners = new ArrayList<>();

    @Mock
    private org.kie.api.task.model.User organizationalEntity;

    private DelegateTaskCommand delegateTaskCommand;

    @Captor
    private ArgumentCaptor<PlanningTaskImpl> planningTaskCaptor;

    @Override
    protected DelegateAndSaveCommand createCommand() {
        PlanningItem planningItem = PlanningItem.builder()
                .taskId(TASK_ID)
                .planningTask(PlanningTask.builder()
                                      .taskId(TASK_ID)
                                      .assignedUser(ASSIGNED_USER)
                                      .index(INDEX)
                                      .published(PUBLISHED)
                                      .build()).build();
        return spy(new DelegateAndSaveCommand(planningItem, USER_ID));
    }

    @Test
    public void executeReadyTask() {
        executeWithValidStatus(Ready);
    }

    @Test
    public void executeReservedTask() {
        executeWithValidStatus(Reserved);
    }

    @Test
    public void executeWithTaskInInvalidStatus() {
        List<Status> invalidStatuses = Arrays.asList(Created, InProgress, Suspended, Completed,
                                                     Failed, Error, Exited, Obsolete);
        invalidStatuses.forEach(this::executeWithInvalidStatus);
    }

    private void executeWithValidStatus(Status status) {
        prepareExecution(status);
        command.execute(taskContext);

        verify(delegateTaskCommand).execute(taskContext);
        verify(persistenceContext).merge(planningTaskCaptor.capture());
        assertEquals(TASK_ID, planningTaskCaptor.getValue().getTaskId(), 0);
        assertEquals(ASSIGNED_USER, planningTaskCaptor.getValue().getAssignedUser());
        assertEquals(INDEX, planningTaskCaptor.getValue().getIndex(), 0);
        assertEquals(PUBLISHED, planningTaskCaptor.getValue().isPublished());
        assertTrue(potentialOwners.isEmpty());
    }

    private void executeWithInvalidStatus(Status status) {
        prepareExecution(status);
        Assertions.assertThatThrownBy(() -> command.execute(taskContext)).hasMessage(String.format(TASK_MODIFIED_ERROR_MSG,
                                                                                                   TASK_ID,
                                                                                                   status,
                                                                                                   Arrays.toString(new Status[]{Ready, Reserved})));
    }

    private void prepareExecution(Status status) {
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getStatus()).thenReturn(status);
        when(task.getPeopleAssignments()).thenReturn(peopleAssignments);
        when(peopleAssignments.getPotentialOwners()).thenReturn(potentialOwners);
        when(persistenceContext.findTask(TASK_ID)).thenReturn(task);
        when(organizationalEntity.getId()).thenReturn(ASSIGNED_USER);
        delegateTaskCommand = spy(new DelegateTaskCommand() {
            @Override
            public Void execute(Context context) {
                potentialOwners.add(organizationalEntity);
                return null;
            }
        });
        when(command.createDelegateCommand(TASK_ID, USER_ID, ASSIGNED_USER)).thenReturn(delegateTaskCommand);
    }
}

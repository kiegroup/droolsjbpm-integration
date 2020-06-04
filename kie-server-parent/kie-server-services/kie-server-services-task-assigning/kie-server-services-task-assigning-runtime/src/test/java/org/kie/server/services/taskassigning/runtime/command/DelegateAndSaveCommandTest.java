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
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DelegateAndSaveCommandTest extends AbstractPlanningCommandTest<DelegateAndSaveCommand> {

    private Task task;

    private org.kie.api.task.model.TaskData taskData;

    private PeopleAssignments peopleAssignments;

    private List<OrganizationalEntity> potentialOwners;

    private org.kie.api.task.model.User organizationalEntity;

    private DelegateTaskCommand delegateTaskCommand;

    @Parameterized.Parameter
    public Status status;

    @Parameterized.Parameter(1)
    public boolean statusIsValid;

    @Parameterized.Parameter(2)
    public boolean potentialOwnerBelongsToTask;

    @Parameterized.Parameter(3)
    public PlanningTaskImpl previousPlanningTask;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[]{Ready, true, false, null});
        data.add(new Object[]{Ready, true, false, mockPlanningTask(TASK_ID)});
        data.add(new Object[]{Ready, true, true, null});
        data.add(new Object[]{Ready, true, true, mockPlanningTask(TASK_ID)});

        data.add(new Object[]{Reserved, true, false, null});
        data.add(new Object[]{Reserved, true, false, mockPlanningTask(TASK_ID)});
        data.add(new Object[]{Reserved, true, true, null});
        data.add(new Object[]{Reserved, true, true, mockPlanningTask(TASK_ID)});

        data.add(new Object[]{Created, false, false, null});
        data.add(new Object[]{InProgress, false, false, null});
        data.add(new Object[]{Suspended, false, false, null});
        data.add(new Object[]{Completed, false, false, null});
        data.add(new Object[]{Failed, false, false, null});
        data.add(new Object[]{Error, false, false, null});
        data.add(new Object[]{Exited, false, false, null});
        data.add(new Object[]{Obsolete, false, false, null});
        return data;
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        task = mock(Task.class);
        taskData = mock(TaskData.class);
        peopleAssignments = mock(PeopleAssignments.class);
        potentialOwners = new ArrayList<>();
        organizationalEntity = mock(org.kie.api.task.model.User.class);
        delegateTaskCommand = mock(DelegateTaskCommand.class);
        planningTaskCaptor = ArgumentCaptor.forClass(PlanningTaskImpl.class);
    }

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
    public void execute() {
        if (statusIsValid) {
            executeWithValidStatus(status, potentialOwnerBelongsToTask, previousPlanningTask);
            if (potentialOwnerBelongsToTask) {
                assertThat(potentialOwners).contains(organizationalEntity);
            } else {
                assertThat(potentialOwners).doesNotContain(organizationalEntity);
            }
        } else {
            executeWithInvalidStatus(status, previousPlanningTask);
        }
    }

    private void executeWithValidStatus(Status status, boolean potentialOwnerAlreadyBelongs, PlanningTaskImpl previousPlanningTask) {
        prepareExecution(status, potentialOwnerAlreadyBelongs, previousPlanningTask);
        command.execute(taskContext);

        verify(delegateTaskCommand).execute(taskContext);

        if (previousPlanningTask != null) {
            verifyPlanningTaskMerged(previousPlanningTask);
        } else {
            verify(persistenceContext).persist(planningTaskCaptor.capture());
            verifyPlanningTaskPersisted(planningTaskCaptor.getValue());
        }
    }

    private void executeWithInvalidStatus(Status status, PlanningTaskImpl previousPlanningTask) {
        prepareExecution(status, false, previousPlanningTask);
        Assertions.assertThatThrownBy(() -> command.execute(taskContext)).hasMessage(String.format(TASK_MODIFIED_ERROR_MSG,
                                                                                                   TASK_ID,
                                                                                                   status,
                                                                                                   Arrays.toString(new Status[]{Ready, Reserved})));
    }

    private void prepareExecution(Status status, boolean potentialOwnerAlreadyBelongs, PlanningTaskImpl previousPlanningTask) {
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getStatus()).thenReturn(status);
        when(task.getPeopleAssignments()).thenReturn(peopleAssignments);
        when(peopleAssignments.getPotentialOwners()).thenReturn(potentialOwners);
        if (potentialOwnerAlreadyBelongs) {
            potentialOwners.add(organizationalEntity);
        }
        when(persistenceContext.findTask(TASK_ID)).thenReturn(task);
        if (previousPlanningTask != null) {
            when(persistenceContext.find(PlanningTaskImpl.class, TASK_ID)).thenReturn(previousPlanningTask);
        }
        when(organizationalEntity.getId()).thenReturn(ASSIGNED_USER);
        delegateTaskCommand = spy(new DelegateTaskCommand() {
            @Override
            public Void execute(Context context) {
                if (!potentialOwners.contains(organizationalEntity)) {
                    potentialOwners.add(organizationalEntity);
                }
                return null;
            }
        });
        when(command.createDelegateCommand(TASK_ID, USER_ID, ASSIGNED_USER)).thenReturn(delegateTaskCommand);
    }

    private static PlanningTaskImpl mockPlanningTask(long taskId) {
        PlanningTaskImpl result = new PlanningTaskImpl();
        result.setTaskId(taskId);
        return spy(result);
    }
}

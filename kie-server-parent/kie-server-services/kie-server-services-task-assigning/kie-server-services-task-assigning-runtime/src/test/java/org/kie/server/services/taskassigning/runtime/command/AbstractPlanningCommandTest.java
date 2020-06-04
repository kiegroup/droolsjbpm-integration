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

import org.jbpm.services.task.commands.TaskContext;
import org.junit.Before;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractPlanningCommandTest<T extends PlanningCommand> {

    protected static final Long TASK_ID = 1L;
    protected static final String ASSIGNED_USER = "ASSIGNED_USER";
    protected static final Integer INDEX = 2;
    protected static final Boolean PUBLISHED = Boolean.TRUE;
    protected static final String USER_ID = "USER_ID";

    protected TaskContext taskContext;

    protected TaskPersistenceContext persistenceContext;

    protected ArgumentCaptor<PlanningTaskImpl> planningTaskCaptor;

    protected T command;

    @Before
    public void setUp() {
        taskContext = mock(TaskContext.class);
        persistenceContext = mock(TaskPersistenceContext.class);
        planningTaskCaptor = ArgumentCaptor.forClass(PlanningTaskImpl.class);
        when(taskContext.getPersistenceContext()).thenReturn(persistenceContext);
        command = createCommand();
    }

    protected abstract T createCommand();

    protected void verifyPlanningTaskMerged(PlanningTaskImpl planningTask) {
        verify(persistenceContext).merge(planningTask);
        verify(planningTask).setAssignedUser(ASSIGNED_USER);
        verify(planningTask).setIndex(INDEX);
        verify(planningTask).setPublished(PUBLISHED);
        verify(planningTask).setLastModificationDate(any());
    }

    protected void verifyPlanningTaskPersisted(PlanningTaskImpl planningTask) {
        verify(persistenceContext).persist(planningTask);
        assertThat(planningTask.getTaskId()).isEqualTo(TASK_ID);
        assertThat(planningTask.getAssignedUser()).isEqualTo(ASSIGNED_USER);
        assertThat(planningTask.getIndex()).isEqualTo(INDEX);
        assertThat(planningTask.isPublished()).isEqualTo(PUBLISHED);
        assertThat(planningTask.getLastModificationDate()).isNotNull();
    }
}

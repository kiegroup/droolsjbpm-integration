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

import org.junit.Test;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SavePlanningItemCommandTest extends AbstractPlanningCommandTest<SavePlanningItemCommand> {

    @Override
    protected SavePlanningItemCommand createCommand() {
        PlanningItem planningItem = PlanningItem.builder()
                .taskId(TASK_ID)
                .planningTask(PlanningTask.builder()
                                      .taskId(TASK_ID)
                                      .assignedUser(ASSIGNED_USER)
                                      .index(INDEX)
                                      .published(PUBLISHED)
                                      .build()).build();
        return new SavePlanningItemCommand(planningItem);
    }

    @Test
    public void executeWithPlanningTaskExisting() {
        PlanningTaskImpl planningTask = mock(PlanningTaskImpl.class);
        when(persistenceContext.find(PlanningTaskImpl.class, TASK_ID)).thenReturn(planningTask);
        command.execute(taskContext);
        verifyPlanningTaskMerged(planningTask);
    }

    @Test
    public void executeWithPlanningTaskNotExisting() {
        when(persistenceContext.find(PlanningTaskImpl.class, TASK_ID)).thenReturn(null);
        command.execute(taskContext);
        verify(persistenceContext).persist(planningTaskCaptor.capture());
        verifyPlanningTaskPersisted(planningTaskCaptor.getValue());
    }
}

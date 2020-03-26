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

package org.kie.server.services.taskassigning.planning;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertToString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeDelegateTest {

    @Mock
    private TaskAssigningRuntimeClient runtimeClient;

    @Captor
    private ArgumentCaptor<PlanningItemList> planCaptor;

    private TaskAssigningRuntimeDelegate delegate;

    @Before
    public void setUp() {
        this.delegate = new TaskAssigningRuntimeDelegate(runtimeClient);
    }

    @Test
    public void findTasks() {
        List<String> status = Collections.singletonList(convertToString(Status.Ready));
        List<Status> internalStatus = Collections.singletonList(Status.Ready);
        LocalDateTime lastModificationDate = LocalDateTime.now();
        TaskInputVariablesReadMode mode = TaskInputVariablesReadMode.READ_FOR_ALL;
        TaskDataList taskDataList = new TaskDataList();
        LocalDateTime queryTime = LocalDateTime.now();
        taskDataList.setQueryTime(queryTime);

        when(runtimeClient.findTasks(eq(0L), eq(status), eq(lastModificationDate), anyInt(), anyInt(), eq(mode))).thenReturn(taskDataList);
        TaskAssigningRuntimeDelegate.FindTasksResult result = delegate.findTasks(internalStatus, lastModificationDate, mode);
        verify(runtimeClient).findTasks(eq(0L), eq(status), eq(lastModificationDate), anyInt(), anyInt(), eq(mode));
        assertTrue(result.getTasks().isEmpty());
        assertEquals(queryTime, result.getQueryTime());
    }

    @Test
    public void executePlanning() {
        PlanningItem item = new PlanningItem();
        List<PlanningItem> planningItems = Collections.singletonList(item);
        String targetUser = "TARGET_USER";
        delegate.executePlanning(planningItems, targetUser);
        verify(runtimeClient).executePlanning(planCaptor.capture(), eq(targetUser));
        assertEquals(planningItems, planCaptor.getValue().getItems());
    }
}

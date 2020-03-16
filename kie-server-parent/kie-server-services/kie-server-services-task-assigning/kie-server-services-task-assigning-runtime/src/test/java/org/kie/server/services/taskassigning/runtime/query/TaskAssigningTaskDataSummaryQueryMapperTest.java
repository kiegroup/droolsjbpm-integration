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

package org.kie.server.services.taskassigning.runtime.query;

import java.util.Collections;
import java.util.List;

import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;

public class TaskAssigningTaskDataSummaryQueryMapperTest extends TaskAssigningTaskDataWithPotentialOwnersQueryMapperTest {

    @Override
    protected TaskAssigningTaskDataSummaryQueryMapper createQueryMapper() {
        return TaskAssigningTaskDataSummaryQueryMapper.get();
    }

    @Override
    protected String getExpectedName() {
        return TaskAssigningTaskDataSummaryQueryMapper.NAME;
    }

    @Override
    protected boolean readPotentialOwnersExpectedValue() {
        return false;
    }

    @Override
    protected void assertTask1IsPresent(List<TaskData> result, int index, boolean withPotentialOwners) {
        assertTaskIsPresent(result, index,
                            TASK1_ID,
                            TASK1_ACTUAL_OWNER,
                            TASK1_DEPLOYMENT_ID,
                            TASK1_STATUS,
                            PlanningTask.builder()
                                    .taskId(TASK1_PLANNING_TASK_ID)
                                    .published(TASK1_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK1_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK1_PLANNING_TASK_INDEX)
                                    .build());
    }

    @Override
    protected void assertTask2IsPresent(List<TaskData> result, int index, boolean withPotentialOwners) {
        assertTaskIsPresent(result, index,
                            TASK2_ID,
                            TASK2_ACTUAL_OWNER,
                            TASK2_DEPLOYMENT_ID,
                            TASK2_STATUS,
                            PlanningTask.builder()
                                    .taskId(TASK2_PLANNING_TASK_ID)
                                    .published(TASK2_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK2_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK2_PLANNING_TASK_INDEX)
                                    .build());
    }

    @Override
    protected void assertTask3IsPresent(List<TaskData> result, int index, boolean withPotentialOwners) {
        assertTaskIsPresent(result, index,
                            TASK3_ID,
                            TASK3_ACTUAL_OWNER,
                            TASK3_DEPLOYMENT_ID,
                            TASK3_STATUS,
                            null);
    }

    private void assertTaskIsPresent(List<TaskData> taskDataList, int index, Long taskId, String actualOwner,
                                     String deploymentId, String status, PlanningTask planningTask) {
        assertTaskIsPresent(taskDataList, index,
                            taskId,
                            null,
                            actualOwner,
                            deploymentId,
                            null,
                            null,
                            null,
                            null,
                            status,
                            null,
                            planningTask,
                            Collections.emptyList());
    }
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.util.List;

import org.kie.server.api.model.taskassigning.ExecutePlanningResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.kie.server.client.util.TaskDataReader;

public class TaskAssigningRuntimeDelegate {

    private final TaskAssigningRuntimeClient runtimeClient;
    //TODO configure the page size via System property.
    private static final int PAGE_SIZE = 3000;

    class FindTasksResult {

        LocalDateTime queryTime;
        List<TaskData> tasks;

        public LocalDateTime getQueryTime() {
            return queryTime;
        }

        public List<TaskData> getTasks() {
            return tasks;
        }

        public FindTasksResult(LocalDateTime queryTime, List<TaskData> tasks) {
            this.queryTime = queryTime;
            this.tasks = tasks;
        }
    }

    public TaskAssigningRuntimeDelegate(final TaskAssigningRuntimeClient runtimeClient) {
        this.runtimeClient = runtimeClient;
    }

    public FindTasksResult findTasks(List<String> status, LocalDateTime fromLastModificationDate, TaskInputVariablesReadMode inputVariablesReadMode) {
        TaskDataReader.Result result = TaskDataReader.from(runtimeClient).readTasks(0, status, fromLastModificationDate, PAGE_SIZE, inputVariablesReadMode);
        return new FindTasksResult(result.getQueryTime(), result.getTasks());
    }

    public ExecutePlanningResult executePlanning(List<PlanningItem> planningItems, String userId) {
        return runtimeClient.executePlanning(new PlanningItemList(planningItems), userId);
    }
}

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

package org.kie.server.client.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.TaskAssigningRuntimeClient;

public class TaskDataReader {

    private TaskAssigningRuntimeClient runtimeClient;

    private TaskDataReader(TaskAssigningRuntimeClient runtimeClient) {
        this.runtimeClient = runtimeClient;
    }

    public static class Result {

        LocalDateTime queryTime;

        List<TaskData> tasks;

        private Result(LocalDateTime queryTime, List<TaskData> tasks) {
            this.queryTime = queryTime;
            this.tasks = tasks;
        }

        public LocalDateTime getQueryTime() {
            return queryTime;
        }

        public List<TaskData> getTasks() {
            return tasks;
        }
    }

    public static TaskDataReader from(TaskAssigningRuntimeClient runtimeClient) {
        return new TaskDataReader(runtimeClient);
    }

    public Result readTasks(long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                            int pageSize, TaskInputVariablesReadMode taskInputVariablesReadMode) {
        final List<TaskData> result = new ArrayList<>();
        boolean finished = false;
        List<TaskData> partialResult;
        TaskDataList taskDataList;
        TaskData lastItem = null;
        LocalDateTime queryTime = null;

        long taskId = fromTaskId;
        int nextPageSize = pageSize;
        while (!finished) {
            taskDataList = runtimeClient.findTasks(taskId, status, fromLastModificationDate,
                                                   0, nextPageSize, taskInputVariablesReadMode);
            partialResult = new ArrayList<>(taskDataList.getItems());
            if (queryTime == null) {
                queryTime = taskDataList.getQueryTime();
            }
            if (partialResult.isEmpty()) {
                finished = true;
            } else {
                if (lastItem == null || partialResult.size() > 1) {
                    lastItem = partialResult.remove(partialResult.size() - 1);
                    taskId = lastItem.getTaskId();
                    nextPageSize = pageSize;
                    result.addAll(partialResult);
                } else {
                    if (lastItem.getTaskId().equals(partialResult.get(0).getTaskId())) {
                        if (partialResult.get(0).getPotentialOwners().size() < nextPageSize) {
                            result.add(partialResult.get(0));
                            finished = true;
                        } else {
                            nextPageSize = nextPageSize * 2;
                        }
                    } else {
                        lastItem = partialResult.get(0);
                        taskId = lastItem.getTaskId();
                        nextPageSize = pageSize;
                    }
                }
            }
        }
        return new Result(queryTime, result);
    }
}

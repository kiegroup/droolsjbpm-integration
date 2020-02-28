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

/**
 * Helper class for reading all the elements returned by the {@link TaskAssigningRuntimeClient#findTasks} methods.
 * This class manages the required pagination for getting all the results.
 */
public class TaskDataReader {

    private TaskAssigningRuntimeClient runtimeClient;

    private TaskDataReader(TaskAssigningRuntimeClient runtimeClient) {
        this.runtimeClient = runtimeClient;
    }

    public static class Result {

        private LocalDateTime queryTime;

        private List<TaskData> tasks;

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

    /**
     * Executes the {@link TaskAssigningRuntimeClient#findTasks(Long, List, LocalDateTime, Integer, Integer, TaskInputVariablesReadMode)}
     * method and return all the results. The paging reading is managed internally by this method.
     * <p>
     * @param fromTaskId filters the tasks with taskId >= fromTaskId. If null no filtering is applied.
     * @param status filters the tasks that are in one of the following status. If null or the empty list no filtering
     * is applied.
     * @param fromLastModificationDate filters the tasks with lastModificationDate >= fromLastModificationDate. If null
     * no filtering is applied.
     * @param pageSize sets the pageSize for the paged reading.
     * @param taskInputVariablesReadMode establishes the tasks input variables reading mode.
     * @return a list of TaskData with the jBPM tasks that met the filtering conditions. The potential owners of the task
     * is properly loaded with all the elements. The task inputs data is loaded accordingly with the selected taskInputVariablesReadMode.
     * @see TaskInputVariablesReadMode
     */
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
                    // the lastItem determines if the query returned all the potential owners for it,
                    // or it might be the case when some of them fall out of the pageSize.
                    if (lastItem.getTaskId().equals(partialResult.get(0).getTaskId())) {
                        if (partialResult.get(0).getPotentialOwners().isEmpty()) {
                            // no potential owners, check if a taskId+1 exists prior to exit.
                            result.add(partialResult.get(0));
                            lastItem = null;
                            taskId++;
                            nextPageSize = pageSize;
                        } else if (partialResult.get(0).getPotentialOwners().size() < nextPageSize) {
                            // the potential owners fits the page margins, we can exit.
                            result.add(partialResult.get(0));
                            finished = true;
                        } else {
                            // there might exists more potential owners and in the worst case we loaded only one element
                            // in last page. increase the page size to ensure we can fetch all.
                            nextPageSize = nextPageSize * 2;
                        }
                    } else {
                        // last item might have been disappeared from result since last query, retry.
                        lastItem = partialResult.get(0);
                        taskId = lastItem.getTaskId();
                        nextPageSize = pageSize;
                    }
                }
            }
        }
        return new Result(queryTime, result);
    }

    /**
     * Executes the {@link TaskAssigningRuntimeClient#findTasks(Long, List, LocalDateTime, Integer, Integer, TaskInputVariablesReadMode)}
     * method and return all the results. The paging reading is managed internally by this method.
     * <p>
     * @param fromTaskId filters the tasks with taskId >= fromTaskId. If null no filtering is applied.
     * @param status filters the tasks that are in one of the following status. If null or the empty list no filtering
     * is applied.
     * @param fromLastModificationDate filters the tasks with lastModificationDate >= fromLastModificationDate. If null
     * no filtering is applied.
     * @param pageSize sets the pageSize for the paged reading.
     * @return a list of TaskData with the jBPM tasks that met the filtering conditions. The potential owners of the task
     * is properly loaded with all the elements. No task inputs data is loaded by this method.
     */
    public Result readTasks(long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate, int pageSize) {
        return readTasks(fromTaskId, status, fromLastModificationDate, pageSize, TaskInputVariablesReadMode.DONT_READ);
    }
}

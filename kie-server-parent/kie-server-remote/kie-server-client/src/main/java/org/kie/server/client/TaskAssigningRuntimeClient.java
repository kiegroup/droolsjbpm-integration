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

package org.kie.server.client;

import java.time.LocalDateTime;
import java.util.List;

import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;

public interface TaskAssigningRuntimeClient {

    PlanningExecutionResult executePlanning(PlanningItemList planningItemList, String userId);

    /**
     * Executes a parametrized query over the jBPM tasks. This method is intended for the tasks assigning integration
     * implementation internal use. Third parties interested in consuming the information type returned by this method
     * should use the simplified variant of this of this method {@link #findTasks(Long, List, LocalDateTime, Integer, Integer)}
     * or any other of the jBPM runtime client/queries methods, etc.
     * <p>
     * @param fromTaskId filters the tasks with taskId >= fromTaskId. If null no filtering is applied.
     * @param status filters the tasks that are in one of the following status. If null or the empty list no filtering
     * is applied.
     * @param fromLastModificationDate filters the tasks with lastModificationDate >= fromLastModificationDate. If null
     * no filtering is applied.
     * @param page sets the starting page for the paged reading.
     * @param pageSize sets the pageSize for the paged reading.
     * @param taskInputVariablesReadMode establishes the tasks input variables reading mode.
     * @return a list of TaskData with the jBPM tasks that met the filtering conditions. The potential owners of the task
     * are always loaded but there's no warranty that the potential owners of the last consumed task fits the page/pageSize
     * configuration. The task inputs data is loaded accordingly with the selected taskInputVariablesReadMode.
     * @see TaskInputVariablesReadMode
     */
    TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                           Integer page, Integer pageSize,
                           TaskInputVariablesReadMode taskInputVariablesReadMode);

    /**
     * Executes a parametrized query over the jBPM tasks. This method is intended for the tasks assigning integration
     * implementation. Third parties that might use this method must be aware that no task inputs data is loaded. If this
     * information is needed the standard jBPM runtime client/queries must be used instead.
     * <p>
     * @param fromTaskId filters the tasks with taskId >= fromTaskId. If null no filtering is applied.
     * @param status filters the tasks that are in one of the following status. If null or the empty list no filtering
     * is applied.
     * @param fromLastModificationDate filters the tasks with lastModificationDate >= fromLastModificationDate. If null
     * no filtering is applied.
     * @param page sets the starting page for the paged reading.
     * @param pageSize sets the pageSize for the paged reading.
     * @return a list of TaskData with the jBPM tasks that met the filtering conditions. The potential owners of the task
     * is always loaded but there's no warranty that the potential owners of the last consumed taks fits the page/pageSize
     * configuration. NO task inputs data is loaded by this method.
     * @see TaskInputVariablesReadMode
     */
    TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                           Integer page, Integer pageSize);
}

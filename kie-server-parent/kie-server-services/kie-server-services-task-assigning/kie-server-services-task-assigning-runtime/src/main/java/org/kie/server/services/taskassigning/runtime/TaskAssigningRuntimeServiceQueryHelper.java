/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryMapperRegistry;
import org.jbpm.services.api.query.QueryResultMapper;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Status;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.taskassigning.LocalDateTimeValue;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.api.model.taskassigning.util.StatusConverter;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.taskassigning.runtime.query.AbstractTaskAssigningQueryMapper;
import org.kie.server.services.taskassigning.runtime.query.TaskAssigningTaskDataWithPotentialOwnersQueryMapper;
import org.kie.server.services.taskassigning.runtime.query.TaskAssigningTaskDataSummaryQueryMapper;

import static org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper;
import static org.kie.api.task.model.Status.Created;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_LAST_MODIFICATION_DATE;
import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_TASK_ID;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE_SIZE;
import static org.kie.server.api.model.taskassigning.QueryParamName.STATUS;
import static org.kie.server.api.model.taskassigning.QueryParamName.TASK_INPUT_VARIABLES_MODE;
import static org.kie.server.api.model.taskassigning.QueryParamName.TO_TASK_ID;

public class TaskAssigningRuntimeServiceQueryHelper {

    private static final String TASK_ASSIGNING_TASKS_WITH_POTENTIAL_OWNERS_AND_PLANNING_TASK = "task-assigning-tasks-with-potential-owners-and-planning-task";

    private static final String TASK_ASSIGNING_TASKS_WITH_PLANNING_TASK_OPTIMIZED = "task-assigning-tasks-with-planning-task-optimized";

    private KieServerRegistry registry;
    private UserTaskService userTaskService;
    private QueryService queryService;

    public TaskAssigningRuntimeServiceQueryHelper(KieServerRegistry registry, UserTaskService userTaskService, QueryService queryService) {
        this.registry = registry;
        this.userTaskService = userTaskService;
        this.queryService = queryService;
    }

    public List<TaskData> executeFindTasksQuery(Map<String, Object> params) {
        List<QueryParam> queryParams = buildQueryParams(params);
        Integer page = params.containsKey(PAGE) ? (Integer) params.get(PAGE) : 0;
        Integer pageSize = params.containsKey(PAGE_SIZE) ? (Integer) params.get(PAGE_SIZE) : 10;
        String loadVariablesMode = (String) params.get(TASK_INPUT_VARIABLES_MODE);
        QueryContext queryContext = new QueryContext(page * pageSize, pageSize, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), true);

        AbstractTaskAssigningQueryMapper<TaskData> resultMapper = (AbstractTaskAssigningQueryMapper<TaskData>) QueryMapperRegistry.get()
                .mapperFor(TaskAssigningTaskDataWithPotentialOwnersQueryMapper.NAME, null);

        List<TaskData> result = executeQuery(queryService, TASK_ASSIGNING_TASKS_WITH_POTENTIAL_OWNERS_AND_PLANNING_TASK,
                                             resultMapper, queryContext, queryParams.toArray(new QueryParam[0]));

        Optional<Predicate<TaskData>> loadInputVariables = Optional.empty();
        if (TaskInputVariablesReadMode.READ_FOR_ALL.name().equals(loadVariablesMode)) {
            loadInputVariables = Optional.of(taskData -> true);
        } else if (TaskInputVariablesReadMode.READ_FOR_ACTIVE_TASKS_WITH_NO_PLANNING_ENTITY.name().equals(loadVariablesMode)) {
            final Predicate<TaskData> isActive = taskData -> {
                if (taskData.getPlanningTask() != null) {
                    return false;
                }
                Status taskStatus = StatusConverter.convertFromString(taskData.getStatus());
                return taskStatus == Created || taskStatus == Ready || taskStatus == Reserved || taskStatus == InProgress || taskStatus == Suspended;
            };
            loadInputVariables = Optional.of(isActive);
        }

        loadInputVariables.ifPresent(taskDataPredicate -> result.stream()
                .filter(taskDataPredicate)
                .forEach(taskData -> taskData.setInputData(readTaskVariables(taskData))));
        return result;
    }

    public List<TaskData> readTasksDataSummary(long fromTaskId, List<String> status, int pageSize) {
        boolean finished = false;
        List<TaskData> result = new ArrayList<>();
        List<TaskData> partialResult;
        long taskId = fromTaskId;
        while (!finished) {
            partialResult = executeOptimizedFindTasksDataSummaryQuery(taskId, status, 0, pageSize);
            if (partialResult.isEmpty()) {
                finished = true;
            } else {
                taskId = partialResult.get(partialResult.size() - 1).getTaskId() + 1;
                result.addAll(partialResult);
            }
        }
        return result;
    }

    private List<TaskData> executeOptimizedFindTasksDataSummaryQuery(Long fromTaskId, List<String> status, int page, int pageSize) {
        QueryContext queryContext = new QueryContext(page * pageSize, pageSize, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), true);
        Map<String, Object> params = new HashMap<>();
        params.put(FROM_TASK_ID, fromTaskId);
        params.put(STATUS, status);
        List<QueryParam> queryParams = buildQueryParams(params);
        AbstractTaskAssigningQueryMapper<TaskData> resultMapper = (AbstractTaskAssigningQueryMapper<TaskData>) QueryMapperRegistry.get()
                .mapperFor(TaskAssigningTaskDataSummaryQueryMapper.NAME, null);

        return executeQuery(queryService, TASK_ASSIGNING_TASKS_WITH_PLANNING_TASK_OPTIMIZED, resultMapper, queryContext, queryParams.toArray(new QueryParam[0]));
    }

    private Map<String, Object> readTaskVariables(TaskData taskData) {
        KieContainerInstanceImpl container = registry.getContainer(taskData.getContainerId());
        if (container == null || (container.getStatus() != KieContainerStatus.STARTED && container.getStatus() != KieContainerStatus.DEACTIVATED)) {
            throw new KieServicesException("Container " + taskData.getContainerId() + " is not available to serve requests");
        }
        Map<String, Object> variables = userTaskService.getTaskInputContentByTaskId(taskData.getContainerId(), taskData.getTaskId());
        variables = variables == null ? new HashMap<>() : variables;
        return variables.entrySet().stream()
                .filter(entry -> isSimpleTypeValue(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean isSimpleTypeValue(Object value) {
        return isPrimitiveWrapper(value.getClass()) || value instanceof Date || value instanceof String;
    }

    // helper method for facilitating testing and avoiding ellipsis parameters capturing.
    <T> T executeQuery(QueryService queryService,
                       String queryName, QueryResultMapper<T> resultMapper, QueryContext queryContext, QueryParam[] params) {
        return queryService.query(queryName, resultMapper, queryContext, params);
    }

    List<QueryParam> buildQueryParams(Map<String, Object> params) {
        List<QueryParam> queryParams = new ArrayList<>();
        if (params.get(FROM_TASK_ID) instanceof Number) {
            Long fromTaskId = ((Number) params.get(FROM_TASK_ID)).longValue();
            queryParams.add(QueryParam.greaterOrEqualTo(AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), fromTaskId));
        }

        if (params.get(TO_TASK_ID) instanceof Number) {
            Long toTaskId = ((Number) params.get(TO_TASK_ID)).longValue();
            queryParams.add(QueryParam.lowerOrEqualTo(AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), toTaskId));
        }

        if (params.containsKey(FROM_LAST_MODIFICATION_DATE)) {
            LocalDateTime fromLastModificationDate;
            if (params.get(FROM_LAST_MODIFICATION_DATE) instanceof LocalDateTimeValue) {
                fromLastModificationDate = ((LocalDateTimeValue) params.get(FROM_LAST_MODIFICATION_DATE)).getValue();
            } else {
                fromLastModificationDate = (LocalDateTime) params.get(FROM_LAST_MODIFICATION_DATE);
            }
            if (fromLastModificationDate != null) {
                Date lastModificationDateValue = Date.from(fromLastModificationDate.atZone(ZoneId.systemDefault()).toInstant());
                queryParams.add(QueryParam.greaterOrEqualTo(AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.LAST_MODIFICATION_DATE.columnName(), lastModificationDateValue));
            }
        }

        List<String> status = (List<String>) params.get(STATUS);
        if (status != null && !status.isEmpty()) {
            queryParams.add(QueryParam.equalsTo(AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.STATUS.columnName(), status.toArray(new String[0])));
        }

        return queryParams;
    }
}

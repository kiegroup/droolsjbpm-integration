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

package org.kie.server.client.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.LocalDateTimeValue;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.TaskAssigningRuntimeClient;

import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_LAST_MODIFICATION_DATE;
import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_TASK_ID;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE_SIZE;
import static org.kie.server.api.model.taskassigning.QueryParamName.STATUS;
import static org.kie.server.api.model.taskassigning.QueryParamName.TASK_INPUT_VARIABLES_MODE;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_EXECUTE_PLANNING_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_QUERIES_TASK_DATA_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_RUNTIME_URI;

public class TaskAssigningRuntimeClientImpl extends AbstractKieServicesClientImpl implements TaskAssigningRuntimeClient {

    public TaskAssigningRuntimeClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public TaskAssigningRuntimeClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public PlanningExecutionResult executePlanning(PlanningItemList planningItemList, String userId) {
        if (config.isRest()) {
            final String uri = TASK_ASSIGNING_RUNTIME_URI + "/" + TASK_ASSIGNING_EXECUTE_PLANNING_URI + "?user=" + userId;
            return makeHttpPostRequestAndCreateCustomResponse(RestURI.build(loadBalancer.getUrl(),
                                                                            uri,
                                                                            Collections.emptyMap()),
                                                              planningItemList,
                                                              PlanningExecutionResult.class);
        } else {
            throw new KieServicesException("JMS protocol is not implemented for this service.");
        }
    }

    @Override
    public TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                                  Integer page, Integer pageSize,
                                  TaskInputVariablesReadMode inputVariablesReadMode) {
        final Map<String, Object> params = TaskQueryParamsBuilder.builder()
                .fromTaskId(fromTaskId)
                .status(status)
                .fromLastModificationDate(fromLastModificationDate)
                .page(page)
                .pageSize(pageSize)
                .taskInputVariablesReadMode(inputVariablesReadMode)
                .build();
        return executeFindTasksQuery(params);
    }

    @Override
    public TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                                  Integer page, Integer pageSize) {
        return findTasks(fromTaskId, status, fromLastModificationDate, page, pageSize, TaskInputVariablesReadMode.DONT_READ);
    }

    private TaskDataList executeFindTasksQuery(Map<String, Object> params) {
        if (config.isRest()) {
            final String uri = TASK_ASSIGNING_RUNTIME_URI + "/" + TASK_ASSIGNING_QUERIES_TASK_DATA_URI;
            return makeHttpPostRequestAndCreateCustomResponse(RestURI.build(loadBalancer.getUrl(),
                                                                            uri,
                                                                            Collections.emptyMap()),
                                                              params,
                                                              TaskDataList.class);
        } else {
            throw new KieServicesException("JMS protocol is not implemented for this service.");
        }
    }

    // intended for facilitating testing.
    public KieServicesConfiguration getConfig() {
        return super.config;
    }

    static class TaskQueryParamsBuilder {

        private Map<String, Object> params = new HashMap<>();

        private TaskQueryParamsBuilder() {
        }

        public static TaskQueryParamsBuilder builder() {
            return new TaskQueryParamsBuilder();
        }

        public TaskQueryParamsBuilder fromTaskId(Long taskId) {
            params.put(FROM_TASK_ID, taskId);
            return this;
        }

        public TaskQueryParamsBuilder status(List<String> status) {
            params.put(STATUS, status);
            return this;
        }

        public TaskQueryParamsBuilder fromLastModificationDate(LocalDateTime lastModificationDate) {
            params.put(FROM_LAST_MODIFICATION_DATE, LocalDateTimeValue.from(lastModificationDate));
            return this;
        }

        public TaskQueryParamsBuilder page(Integer page) {
            params.put(PAGE, page);
            return this;
        }

        public TaskQueryParamsBuilder pageSize(Integer pageSize) {
            params.put(PAGE_SIZE, pageSize);
            return this;
        }

        public TaskQueryParamsBuilder taskInputVariablesReadMode(TaskInputVariablesReadMode inputVariablesReadMode) {
            if (inputVariablesReadMode != null) {
                params.put(TASK_INPUT_VARIABLES_MODE, inputVariablesReadMode.name());
            }
            return this;
        }

        public Map<String, Object> build() {
            return params;
        }
    }
}

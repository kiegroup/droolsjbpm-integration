/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.jbpm.search;

import static org.kie.server.services.jbpm.ConvertUtils.buildQueryContext;
import static org.kie.server.services.jbpm.ConvertUtils.convertToErrorInstanceList;
import static org.kie.server.services.jbpm.ConvertUtils.convertToProcessInstanceList;
import static org.kie.server.services.jbpm.ConvertUtils.convertToProcessInstanceWithVarsList;
import static org.kie.server.services.jbpm.ConvertUtils.convertToTaskInstanceList;
import static org.kie.server.services.jbpm.ConvertUtils.convertToTaskInstanceWithVarsList;
import static org.kie.server.services.jbpm.ConvertUtils.convertToTaskSummaryList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryMapperRegistry;
import org.jbpm.services.api.query.QueryResultMapper;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.error.ExecutionError;
import org.kie.server.jbpm.search.api.model.definition.BaseQueryFilterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template which provides functionality to do a query via {@link QueryService}.
 * <p>
 * Users of the template need to provide a {@link QueryCallback} and a {@link RequestCallback}, which provide the context in which to
 * execute the query.
 */
public class QueryServiceTemplate {

	private static final Logger logger = LoggerFactory.getLogger(QueryServiceTemplate.class);

	private QueryService queryService;

	public QueryServiceTemplate(QueryService queryService) {
		this.queryService = queryService;
	}

	public <T> T getWithFilters(Integer page, Integer pageSize, QueryCallback queryCallback, RequestCallback reqCallback) {

		QueryParam[] params = new QueryParam[0];
		Map<String, String> columnMapping = null;
		QueryContext queryContext = buildQueryContext(page, pageSize);

		BaseQueryFilterSpec filterSpec = reqCallback.getQueryFilterSpec();
		if (filterSpec != null) {
			queryContext.setOrderBy(filterSpec.getOrderBy());
			queryContext.setAscending(filterSpec.isAscending());

			// build parameters for filtering the query
			if (filterSpec.getParameters() != null) {
				params = new QueryParam[filterSpec.getParameters().length];
				int index = 0;
				for (org.kie.server.api.model.definition.QueryParam param : filterSpec.getParameters()) {
					params[index] = new QueryParam(param.getColumn(), param.getOperator(), param.getValue());
					index++;
				}
			}

			columnMapping = queryCallback.getQueryStrategy().getColumnMapping(params);
		}

		QueryResultMapper<?> resultMapper = QueryMapperRegistry.get().mapperFor(queryCallback.getMapperName(), columnMapping);

		logger.debug("About to perform query '{}' with page {} and page size {}", queryCallback.getQueryName(), page, pageSize);

		Object result = queryService.query(queryCallback.getQueryName(), resultMapper, queryContext, params);

		logger.debug("Result returned from the query {} mapped with {}", result, resultMapper);

		return (T) transform(result, resultMapper);
	}

	protected Object transform(Object result, QueryResultMapper resultMapper) {
		Object actualResult = null;
		if (result instanceof Collection) {

			if (ProcessInstanceWithVarsDesc.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of ProcessInstanceWithVarsDesc to ProcessInstanceList");
				actualResult = convertToProcessInstanceWithVarsList((Collection<ProcessInstanceWithVarsDesc>) result);
			} else if (ProcessInstanceDesc.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of ProcessInstanceDesc to ProcessInstanceList");
				actualResult = convertToProcessInstanceList((Collection<ProcessInstanceDesc>) result);
			} else if (UserTaskInstanceWithVarsDesc.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of UserTaskInstanceWithVarsDesc to TaskInstanceList");
				actualResult = convertToTaskInstanceWithVarsList((Collection<UserTaskInstanceWithVarsDesc>) result);
			} else if (UserTaskInstanceDesc.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of UserTaskInstanceDesc to TaskInstanceList");
				actualResult = convertToTaskInstanceList((Collection<UserTaskInstanceDesc>) result);
			} else if (TaskSummary.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of TaskSummary to TaskSummaryList");
				actualResult = convertToTaskSummaryList((Collection<TaskSummary>) result);
			} else if (ExecutionError.class.isAssignableFrom(resultMapper.getType())) {

                logger.debug("Converting collection of ExecutionError to ErrorInstanceList");
                actualResult = convertToErrorInstanceList((List<ExecutionError>) result);
            } else if (List.class.isAssignableFrom(resultMapper.getType())) {

				logger.debug("Converting collection of List to ArrayList");
				actualResult = new ArrayList((Collection) result);
			} else {

				logger.debug("Convert not supported for custom type {}", resultMapper.getType());
				actualResult = result;
			}

			logger.debug("Actual result after converting is {}", actualResult);
		} else {
			logger.debug("Result is not a collection - {}, skipping any conversion", result);
			actualResult = result;
		}
		return actualResult;
	}

}

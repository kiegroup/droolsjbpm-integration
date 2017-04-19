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

package org.kie.server.services.jbpm.queries;

import static org.kie.server.services.jbpm.ConvertUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryResultMapper;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueryServiceBase {

	private static final Logger logger = LoggerFactory.getLogger(AbstractQueryServiceBase.class);
	
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

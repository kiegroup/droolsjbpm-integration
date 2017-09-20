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

package org.kie.server.services.jbpm.search.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskQueryStrategy implements QueryStrategy {

	private static final Logger logger = LoggerFactory.getLogger(TaskQueryStrategy.class);
	
	private static final String TASK_QUERY = "select ti.* from AuditTaskImpl ti";
	
	@Override
	public String getQueryExpression() {
		return TASK_QUERY;
	}	

}

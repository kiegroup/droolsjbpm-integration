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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.api.query.model.QueryParam;
import org.kie.server.jbpm.search.api.model.definition.ProcessInstanceField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceQueryStrategy implements QueryStrategy {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceQueryStrategy.class);

	private static final String PROCESS_INSTANCE_QUERY = "select pi.* from ProcessInstanceLog pi";

	@Override
	public String getQueryExpression() {
		return PROCESS_INSTANCE_QUERY;
	}

	@Override
	public Map<String, String> getColumnMapping(QueryParam[] params) {
		Map<String, String> mapping = new HashMap<>();

		for (QueryParam nextParam : params) {
			mapping.put(nextParam.getColumn(), getColumnType(nextParam.getColumn()));
		}

		return mapping;
	}

	private String getColumnType(String param) {
		String columnType = null;

		switch (ProcessInstanceField.valueOf(param)) {
		case ID:
			columnType = "integer";
			break;
		case CORRELATIONKEY:
			// TODO: define mapping for timestamp. Is that date?
			columnType = "string";
			break;
		case DURATION:
			columnType = "integer";
			break;
		case END_DATE:
			columnType = "date";
			break;
		case EXTERNALID:
			// TODO: define mapping for timestamp. Is that date?
			columnType = "string";
			break;
		case USER_IDENTITY:
			columnType = "string";
			break;
		case OUTCOME:
			columnType = "string";
			break;
		case PARENTPROCESSINSTANCEID:
			columnType = "integer";
			break;
		case PROCESSID:
			columnType = "string";
			break;
		case PROCESSINSTANCEDESCRIPTION:
			columnType = "string";
			break;
		case PROCESSINSTANCEID:
			columnType = "integer";
			break;
		case PROCESSNAME:
			columnType = "string";
			break;
		case PROCESSTYPE:
			columnType = "integer";
			break;
		case PROCESSVERSION:
			columnType = "string";
			break;
		case START_DATE:
			columnType = "date";
			break;
		case STATUS:
			columnType = "integer";
			break;
		default:
			String message = "Unknown paramater.";
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		return columnType;
	}
	
}

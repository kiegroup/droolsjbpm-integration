package org.kie.server.services.jbpm.taskqueries.util;

import java.util.Map;

import org.kie.server.api.model.definition.QueryParam;

public class PostgreSQLTaskQueriesStrategy implements TaskQueriesStrategy {

	private static final String TASK_QUERY = "select ti.* from AuditTaskImpl ti";
	
	@Override
	public String getTaskQueryExpression() {
		return TASK_QUERY;
	}
	
	
	@Override
	public Map<String, String> getColumnMapping(QueryParam[] params) {
		// TODO Auto-generated method stub
		return null;
	}

}

package org.kie.server.services.jbpm.taskqueries.util;

import java.util.Map;

import org.jbpm.services.api.query.model.QueryParam;

/**
 * Provides DB specific query and column-mappings for task queries.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface TaskQueriesStrategy {

		String getTaskQueryExpression();
	
		Map<String, String> getColumnMapping(QueryParam[] params);
	
}

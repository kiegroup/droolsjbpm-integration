package org.kie.server.services.jbpm.taskqueries.util;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.api.query.model.QueryParam;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLTaskQueriesStrategy implements TaskQueriesStrategy {

	private static final Logger logger = LoggerFactory.getLogger(PostgreSQLTaskQueriesStrategy.class);
	
	private static final String TASK_QUERY = "select ti.* from AuditTaskImpl ti";
	
	@Override
	public String getTaskQueryExpression() {
		return TASK_QUERY;
	}
	
	/*
	 * 
	 *  
	 * id bigint NOT NULL,
  activationtime timestamp without time zone,
  actualowner character varying(255),
  createdby character varying(255),
  createdon timestamp without time zone,
  deploymentid character varying(255),
  description character varying(255),
  duedate timestamp without time zone,
  name character varying(255),
  parentid bigint NOT NULL,
  priority integer NOT NULL,
  processid character varying(255),
  processinstanceid bigint NOT NULL,
  processsessionid bigint NOT NULL,
  status character varying(255),
  taskid bigint,
  workitemid bigint,
  CONSTRAINT audittaskimpl_pkey PRIMARY KEY (id)
	 * 
	 */
	@Override
	public Map<String, String> getColumnMapping(QueryParam[] params) {
		Map<String, String> mapping = new HashMap<>();
		
		for (QueryParam nextParam: params) {
			mapping.put(nextParam.getColumn(), getColumnType(nextParam.getColumn()));
		}
		
		return mapping;
	}
	
	private String getColumnType(String param) {
		String columnType = null;
		
		switch(TaskField.valueOf(param)) {
		case ID:
			columnType = "integer";
			break;
		case ACTIVATIONTIME:
			//TODO: define mapping for timestamp. Is that date?
			columnType = "date";
			break;
		case ACTUALOWNER:
			columnType = "string";
			break;
		case CREATEDBY:
			columnType = "string";
			break;
		case CREATEDON:
			//TODO: define mapping for timestamp. Is that date?
			columnType = "date";
			break;
		case DEPLOYMENTID:
			columnType = "string";
			break;
		case DESCRIPTION:
			columnType = "string";
			break;
		case DUEDATE:
			columnType = "date";
			break;
		case NAME:
			columnType = "string";
			break;
		case PARENTID:
			columnType = "integer";
			break;
		case PRIORITY:
			columnType = "integer";
			break;
		case PROCESSID:
			columnType = "string";
			break;
		case PROCESSINSTANCEID:
			columnType = "integer";
			break;
		case PROCESSSESSIONID:
			columnType = "integer";
			break;
		case STATUS:
			columnType = "integer";
			break;
		case TASKID:
			columnType = "integer";
			break;
		case WORKITEMID:
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

package org.kie.server.jbpm.taskqueries.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskQueryFilterSpec;

/**
 * QueryFilterSpecBuilder targeted at filters for Tasks.
 * <p/>
 * This provides a higher level-api than the Advanced Query API (i.e. {@link QueryFilterSpecBuilder} as the Task field-names (column-names)
 * are pre-defined and exposed via a strongly-typed API, and thus not bound to specific column-names in the database. This guards users of
 * this API for potential changes in the jBPM schema.
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class TaskQueryFilterSpecBuilder {

	/**
	 * Defines the fields of a Task.
	 * 
	 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
	 *
	 */
	public enum TASK_FIELD {

		//@formatter:off
		ID,			
		ACTIVATIONTIME,		
		ACTUALOWNER,			
		CREATEDBY,			
		CREATEDON,			
		DEPLOYMENTID,			
		DESCRIPTION,			
		DUEDATE,			
		NAME,			
		PARENTID,			
		PRIORITY,			
		PROCESSID,			
		PROCESSINSTANCEID,			
		PROCESSSESSIONID,			
		STATUS,			
		TASKID,			
		WORKITEMID
		//@formatter:on
	};

	private List<QueryParam> parameters = new ArrayList<QueryParam>();
	private TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpec();
	//private Map<String, String> columnMapping = new HashMap<String, String>();

	public TaskQueryFilterSpec get() {
		if (!parameters.isEmpty()) {
			filterSpec.setParameters(parameters.toArray(new QueryParam[parameters.size()]));
		}
		//Don't need column mapping here. Column mapping is implicit.
		/*
		if (!columnMapping.isEmpty()) {
			filterSpec.setColumnMapping(columnMapping);
		}
		*/
		return filterSpec;
	}

	public TaskQueryFilterSpecBuilder orderBy(TASK_FIELD field, boolean isAscending) {
		filterSpec.setOrderBy(field.toString());
		filterSpec.setAscending(isAscending);

		return this;
	}

	public TaskQueryFilterSpecBuilder isNull(TASK_FIELD field) {
		parameters.add(new QueryParam(field.toString(), "IS_NULL", null));

		return this;
	}

	public TaskQueryFilterSpecBuilder isNotNull(TASK_FIELD field) {
		parameters.add(new QueryParam(field.toString(), "NOT_NULL", null));

		return this;
	}

	public TaskQueryFilterSpecBuilder equalsTo(TASK_FIELD field, Comparable<?>... values) {
		parameters.add(new QueryParam(field.toString(), "EQUALS_TO", Arrays.asList(values)));

		return this;
	}

	public TaskQueryFilterSpecBuilder notEqualsTo(TASK_FIELD field, Comparable<?>... values) {
		parameters.add(new QueryParam(field.toString(), "NOT_EQUALS_TO", Arrays.asList(values)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder likeTo(TASK_FIELD field, boolean caseSensitive, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LIKE_TO", Arrays.asList(value, caseSensitive)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder greaterThan(TASK_FIELD field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "GREATER_THAN", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder greaterOrEqualTo(TASK_FIELD field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "GREATER_OR_EQUALS_TO", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder lowerThan(TASK_FIELD field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LOWER_THAN", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder lowerOrEqualTo(TASK_FIELD field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LOWER_OR_EQUALS_TO", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder between(TASK_FIELD field, Comparable<?> start, Comparable<?> end) {
		parameters.add(new QueryParam(field.toString(), "BETWEEN", Arrays.asList(start, end)));

		return this;
	}

	public TaskQueryFilterSpecBuilder in(TASK_FIELD field, List<?> values) {
		parameters.add(new QueryParam(field.toString(), "IN", values));

		return this;
	}

	public TaskQueryFilterSpecBuilder notIn(TASK_FIELD field, List<?> values) {
		parameters.add(new QueryParam(field.toString(), "NOT_IN", values));

		return this;
	}

	/* TODO: Don't need column mapping.
	public TaskQueryFilterSpecBuilder addColumnMapping(TASK_FIELD field, String type) {
		columnMapping.put(field.toString(), type);

		return this;
	}
	*/

}

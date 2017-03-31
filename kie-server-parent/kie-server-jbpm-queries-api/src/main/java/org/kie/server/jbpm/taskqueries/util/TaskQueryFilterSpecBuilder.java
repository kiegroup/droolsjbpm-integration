package org.kie.server.jbpm.taskqueries.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskField;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskQueryFilterSpec;

/**
 * QueryFilterSpecBuilder targeted at filters for Tasks.
 * <p/>
 * This provides a higher level-api than the Advanced Query API (i.e. {@link QueryFilterSpecBuilder} as the Task field-names (column-names)
 * are pre-defined and exposed via a strongly-typed API, and thus not bound to specific column-names in the database. This guards users of
 * this API for potential changes in the jBPM schema. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class TaskQueryFilterSpecBuilder {

	private List<QueryParam> parameters = new ArrayList<QueryParam>();
	private TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpec();
	
	public TaskQueryFilterSpec get() {
		if (!parameters.isEmpty()) {
			filterSpec.setParameters(parameters.toArray(new QueryParam[parameters.size()]));
		}
		
		return filterSpec;
	}

	public TaskQueryFilterSpecBuilder orderBy(TaskField field, boolean isAscending) {
		filterSpec.setOrderBy(field.toString());
		filterSpec.setAscending(isAscending);

		return this;
	}

	public TaskQueryFilterSpecBuilder isNull(TaskField field) {
		parameters.add(new QueryParam(field.toString(), "IS_NULL", null));

		return this;
	}

	public TaskQueryFilterSpecBuilder isNotNull(TaskField field) {
		parameters.add(new QueryParam(field.toString(), "NOT_NULL", null));

		return this;
	}

	public TaskQueryFilterSpecBuilder equalsTo(TaskField field, Comparable<?>... values) {
		parameters.add(new QueryParam(field.toString(), "EQUALS_TO", Arrays.asList(values)));

		return this;
	}

	public TaskQueryFilterSpecBuilder notEqualsTo(TaskField field, Comparable<?>... values) {
		parameters.add(new QueryParam(field.toString(), "NOT_EQUALS_TO", Arrays.asList(values)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder likeTo(TaskField field, boolean caseSensitive, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LIKE_TO", Arrays.asList(value, caseSensitive)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder greaterThan(TaskField field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "GREATER_THAN", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder greaterOrEqualTo(TaskField field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "GREATER_OR_EQUALS_TO", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder lowerThan(TaskField field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LOWER_THAN", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder lowerOrEqualTo(TaskField field, Comparable<?> value) {
		parameters.add(new QueryParam(field.toString(), "LOWER_OR_EQUALS_TO", Arrays.asList(value)));

		return this;
	}

	@SuppressWarnings("unchecked")
	public TaskQueryFilterSpecBuilder between(TaskField field, Comparable<?> start, Comparable<?> end) {
		parameters.add(new QueryParam(field.toString(), "BETWEEN", Arrays.asList(start, end)));

		return this;
	}

	public TaskQueryFilterSpecBuilder in(TaskField field, List<?> values) {
		parameters.add(new QueryParam(field.toString(), "IN", values));

		return this;
	}

	public TaskQueryFilterSpecBuilder notIn(TaskField field, List<?> values) {
		parameters.add(new QueryParam(field.toString(), "NOT_IN", values));

		return this;
	}

}

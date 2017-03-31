package org.kie.server.jbpm.taskqueries.api.model.definition;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.definition.QueryParam;

/*
 * TODO: If QueryFilterSpec would have an abstract class supertype without the column-mapping, we could just inherit from that code, without the need for duplication.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-query-filter-spec")
public class TaskQueryFilterSpec {

	@XmlElement(name = "order-by")
	private String orderBy;
	@XmlElement(name = "order-asc")
	private boolean ascending;
	@XmlElement(name = "query-params")
	private QueryParam[] parameters;

	public TaskQueryFilterSpec() {
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public QueryParam[] getParameters() {
		return parameters;
	}

	public void setParameters(QueryParam[] parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "QueryFilterSpec{" + "orderBy='" + orderBy + '\'' + ", ascending=" + ascending + ", parameters="
				+ Arrays.toString(parameters) + '}';
	}

}

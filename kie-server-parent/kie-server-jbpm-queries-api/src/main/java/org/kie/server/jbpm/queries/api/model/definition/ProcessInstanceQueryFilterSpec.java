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

package org.kie.server.jbpm.queries.api.model.definition;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.definition.QueryParam;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-instance-query-filter-spec")
public class ProcessInstanceQueryFilterSpec implements BaseQueryFilterSpec {

	@XmlElement(name = "order-by")
	private String orderBy;
	@XmlElement(name = "order-asc")
	private boolean ascending;
	@XmlElement(name = "query-params")
	private QueryParam[] parameters;

	public ProcessInstanceQueryFilterSpec() {
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
		return "ProcessInstanceQueryFilterSpec{" + "orderBy='" + orderBy + '\'' + ", ascending=" + ascending + ", parameters="
				+ Arrays.toString(parameters) + '}';
	}

}

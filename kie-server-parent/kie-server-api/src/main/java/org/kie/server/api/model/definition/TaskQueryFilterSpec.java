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

package org.kie.server.api.model.definition;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.Wrapped;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-query-filter-spec")
public class TaskQueryFilterSpec extends QueryFilterSpec implements Wrapped<TaskQueryFilterSpec> {

	public TaskQueryFilterSpec() {
	}

	@Override
	public String toString() {
		return "TaskQueryFilterSpec{" + "orderBy='" + getOrderBy() + '\'' + ", ascending=" + isAscending() + ", parameters="
				+ Arrays.toString(getParameters()) + '}';
	}

	@Override
	public TaskQueryFilterSpec unwrap() {
		unwrapParameters();
		return this;
	}
}

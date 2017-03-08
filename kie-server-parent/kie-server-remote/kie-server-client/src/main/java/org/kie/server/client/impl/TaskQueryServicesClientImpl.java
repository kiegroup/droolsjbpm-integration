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

package org.kie.server.client.impl;

import static org.kie.server.api.rest.RestURI.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.TaskQueryServicesClient;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskQueryFilterSpec;

public class TaskQueryServicesClientImpl extends AbstractKieServicesClientImpl implements TaskQueryServicesClient {

	private static Set<Class<?>> extraClasses;

	static {
		extraClasses = new HashSet<>();
		extraClasses.add(TaskQueryFilterSpec.class);
	}

	// TODO: Adding extra classes to the config at this point doesn't seem to work. They're not picked up by the JaxB Marshaller
	public TaskQueryServicesClientImpl(KieServicesConfiguration config) {
		super(config);
		config.addExtraClasses(extraClasses);
	}

	public TaskQueryServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
		super(config, classLoader);
		config.addExtraClasses(extraClasses);
	}

	@Override
	public List<TaskInstance> findHumanTasksWithFilters(TaskQueryFilterSpec filterSpec, Integer page, Integer pageSize) {

		TaskInstanceList result = null;

		if (config.isRest()) {
			String queryString = getPagingQueryString("?", page, pageSize);
			result = makeHttpPostRequestAndCreateCustomResponse(loadBalancer.getUrl() + "/" + TASKS_GET_FILTERED_URI + queryString,
					filterSpec, TaskInstanceList.class);

		} else {
			// TODO: Need to implement the command (used for non-REST scenarios). Look at QueryServicesClientImpl for examples.
			throw new UnsupportedOperationException("This operation does not yet provide support for non-REST commands.");
		}

		if (result != null) {
			return result.getItems();
		} else {
			return Collections.emptyList();
		}
	}

}

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

import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_GET_FILTERED_URI;
import static org.kie.server.api.rest.RestURI.TASKS_GET_FILTERED_URI;

import java.util.Collections;
import java.util.List;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.SearchServicesClient;

public class SearchServicesClientImpl extends AbstractKieServicesClientImpl implements SearchServicesClient {

	public SearchServicesClientImpl(KieServicesConfiguration config) {
		super(config);
	}
	
	public SearchServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
		super(config, classLoader);
	}

	@Override
	public List<ProcessInstance> findProcessInstancesWithFilters(ProcessInstanceQueryFilterSpec filterSpec, Integer page, Integer pageSize) {
		ProcessInstanceList result = null;

		if (config.isRest()) {
			String queryString = getPagingQueryString("?", page, pageSize);
			result = makeHttpPostRequestAndCreateCustomResponse(loadBalancer.getUrl() + "/" + PROCESS_INSTANCES_GET_FILTERED_URI + queryString,
					filterSpec, ProcessInstanceList.class);

		} else {
		    CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new DescriptorCommand("ProcessInstanceSeachService", "getProcessInstancesWithFilters", serialize(filterSpec), marshaller.getFormat().getType(), new Object[]{page, pageSize})));
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand(script, DescriptorCommand.class.getName(), "BPMQueries").getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();		    
		}

		if (result != null) {
			return result.getItems();
		} else {
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<TaskInstance> findHumanTasksWithFilters(TaskQueryFilterSpec filterSpec, Integer page, Integer pageSize) {

		TaskInstanceList result = null;

		if (config.isRest()) {
			String queryString = getPagingQueryString("?", page, pageSize);
			result = makeHttpPostRequestAndCreateCustomResponse(loadBalancer.getUrl() + "/" + TASKS_GET_FILTERED_URI + queryString,
					filterSpec, TaskInstanceList.class);

		} else {
		    CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new DescriptorCommand("TaskSearchService", "getHumanTasksWithFilters", serialize(filterSpec), marshaller.getFormat().getType(), new Object[]{page, pageSize})));
            ServiceResponse<TaskInstanceList> response = (ServiceResponse<TaskInstanceList>) executeJmsCommand(script, DescriptorCommand.class.getName(), "BPMQueries").getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();  
		}

		if (result != null) {
			return result.getItems();
		} else {
			return Collections.emptyList();
		}
	}

}

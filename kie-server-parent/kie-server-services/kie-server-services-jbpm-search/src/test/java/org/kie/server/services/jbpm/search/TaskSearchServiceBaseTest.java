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

package org.kie.server.services.jbpm.search;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.Test;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.jbpm.search.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.search.TaskSearchServiceBase;
import org.kie.server.services.jbpm.search.util.QueryStrategy;
import org.mockito.Mockito;

public class TaskSearchServiceBaseTest {
	
	@Test
	public void testGetHumanTasksWithFilters() {
		QueryService queryServiceMock = Mockito.mock(QueryService.class);
		KieServerRegistry contextMock = Mockito.mock(KieServerRegistry.class);
		
		
		//Registry mock needs to return extra classes registered by the extension.
		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(TaskQueryFilterSpec.class);
		when(contextMock.getExtraClasses()).thenReturn(extraClasses);
		
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(contextMock.getConfig()).thenReturn(configMock);
		
		QueryStrategy taskQueriesStrategyMock = Mockito.mock(QueryStrategy.class); 
		
		TaskSearchServiceBase base = new TaskSearchServiceBase(queryServiceMock, contextMock, taskQueriesStrategyMock);
		
		//Verify that extra classes are registered on the context.
		verify(contextMock, times(1)).addExtraClasses(extraClasses);
		
		Integer page = new Integer(0);
		Integer pageSize = new Integer(10);
		String payload = getPayload();
		String marshallingType = "JAXB";
		
		Collection<UserTaskInstanceWithVarsDesc> userTaskInstances = getUserTaskInstances();
		
		when(queryServiceMock.query(any(), any(), any(), any(QueryParam.class))).thenReturn(userTaskInstances);
		
		TaskInstanceList taskInstances = base.getHumanTasksWithFilters(page, pageSize, payload, marshallingType);
		
		//TODO: Implement the same logic with JUnit AssertThat
		assertEquals(1, taskInstances.getItems().size());
		
		TaskInstance ti1 = taskInstances.getItems().stream().findFirst().get();
		
		assertEquals("ddoyle", ti1.getActualOwner());
		assertEquals("RESERVED", ti1.getStatus());
		assertEquals("mswiderski", ti1.getCreatedBy());
	}
	
	private String getPayload() {
		StringBuilder payloadBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"); 
		payloadBuilder.append("<task-query-filter-spec>");
		payloadBuilder.append("<order-asc>false</order-asc>");
		payloadBuilder.append("<query-params>");
		payloadBuilder.append("<cond-column>DEPLOYMENTID</cond-column>");
		payloadBuilder.append("<cond-operator>EQUALS_TO</cond-operator>");
		payloadBuilder.append("<cond-values xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">simple-project</cond-values>");
		payloadBuilder.append("</query-params>");
		payloadBuilder.append("</task-query-filter-spec>");
		return payloadBuilder.toString();
	}
	
	private Collection<UserTaskInstanceWithVarsDesc> getUserTaskInstances() {
		List<UserTaskInstanceWithVarsDesc> tasks = new ArrayList<>();
		
		UserTaskInstanceWithVarsDesc task1 = new org.jbpm.kie.services.impl.model.UserTaskInstanceWithVarsDesc(new Long(1L), "RESERVED", new Date(), "test-task", "Test task", new Integer(1), "ddoyle", "mswiderski", "test-deployment", "test-process", new Long(1L), new Date(), new Date());
		
		tasks.add(task1);
		
		return tasks;
	}
	
}

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

import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.Test;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.mockito.Mockito;

public class ProcessInstanceSearchServiceBaseTest {
	
	@Test
	public void testGetProcessInstancesTasksWithFilters() {
		QueryService queryServiceMock = Mockito.mock(QueryService.class);
		KieServerRegistry contextMock = Mockito.mock(KieServerRegistry.class);
		
		
		//Registry mock needs to return extra classes registered by the extension.
		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(ProcessInstanceQueryFilterSpec.class);
		when(contextMock.getExtraClasses()).thenReturn(extraClasses);
		
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(contextMock.getConfig()).thenReturn(configMock);
		
		ProcessInstanceSearchServiceBase base = new ProcessInstanceSearchServiceBase(queryServiceMock, contextMock);
		
		//Verify that extra classes are registered on the context.
		verify(contextMock, times(1)).addExtraClasses(extraClasses);
		
		Integer page = new Integer(0);
		Integer pageSize = new Integer(10);
		String payload = getPayload();
		String marshallingType = "JAXB";
		
		Collection<ProcessInstanceWithVarsDesc> processInstanceCollection = getProcessInstances();
		
		when(queryServiceMock.query(any(), any(), any(), any(QueryParam.class))).thenReturn(processInstanceCollection);
		
		ProcessInstanceList processInstances = base.getProcessInstancesWithFilters(page, pageSize, payload, marshallingType);
		
		//TODO: Implement the same logic with JUnit AssertThat
		assertEquals(1, processInstances.getItems().size());
		
		ProcessInstance pi1 = processInstances.getItems().stream().findFirst().get();
		
		assertEquals("test-process", pi1.getProcessName());
		assertEquals(42L, pi1.getId().longValue());
		assertEquals("mswiderski", pi1.getInitiator());
	}
	
	private String getPayload() {
		StringBuilder payloadBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"); 
		payloadBuilder.append("<process-instance-query-filter-spec>");
		payloadBuilder.append("<order-asc>false</order-asc>");
		payloadBuilder.append("<query-params>");
		payloadBuilder.append("<cond-column>PROCESSNAME</cond-column>");
		payloadBuilder.append("<cond-operator>EQUALS_TO</cond-operator>");
		payloadBuilder.append("<cond-values xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">test-process</cond-values>");
		payloadBuilder.append("</query-params>");
		payloadBuilder.append("</process-instance-query-filter-spec>");
		return payloadBuilder.toString();
	}
	
	private Collection<ProcessInstanceWithVarsDesc> getProcessInstances() {
		List<ProcessInstanceWithVarsDesc> processInstances = new ArrayList<>();
		
		ProcessInstanceWithVarsDesc processInstance1 = new org.jbpm.kie.services.impl.model.ProcessInstanceWithVarsDesc(42L, "test-process-id", "test-process", "1.0", 1, "test-deployment", new Date(), "mswiderski", "test description", "ck101"); 
		
		processInstances.add(processInstance1);
		
		return processInstances;
	}
	
}

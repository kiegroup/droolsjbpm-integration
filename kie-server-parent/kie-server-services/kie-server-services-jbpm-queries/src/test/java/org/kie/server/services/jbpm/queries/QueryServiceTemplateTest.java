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

package org.kie.server.services.jbpm.queries;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryResultMapper;
import org.jbpm.services.api.query.QueryService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.jbpm.queries.api.model.definition.BaseQueryFilterSpec;
import org.kie.server.jbpm.queries.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.queries.util.QueryStrategy;
import org.kie.server.services.jbpm.queries.util.QueryStrategyFactory;
import org.mockito.Mockito;


public class QueryServiceTemplateTest {

	QueryServiceTemplate qst;
	
	
	@Before
	public void init() {
		QueryService queryServiceMock = Mockito.mock(QueryService.class);
		this.qst = new QueryServiceTemplate(queryServiceMock);
		when(queryServiceMock.query(any(), any(), any(), any(org.jbpm.services.api.query.model.QueryParam.class))).thenReturn(getProcessInstances());
	}
	
	
	@Test
	public void testGetWithFilters() {
		
		QueryCallback queryCallback = new QueryCallback() {
			
			@Override
			public QueryStrategy getQueryStrategy() {
				KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
				KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
				when(registryMock.getConfig()).thenReturn(configMock);
				when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.PostgreSQLDialect");
				
				return new QueryStrategyFactory(registryMock).getProcessQueriesStrategy();
			}
			
			@Override
			public String getQueryName() {
				return "TEST_QUERY";
			}
			
			@Override
			public String getMapperName() {
				return "ProcessInstancesWithCustomVariables";
			}
		};
		
		RequestCallback reqCallback = new RequestCallback() {
			
			@Override
			public BaseQueryFilterSpec getQueryFilterSpec() {
				ProcessInstanceQueryFilterSpec querySpec = new ProcessInstanceQueryFilterSpec();
				
				QueryParam[] parameters = new QueryParam[1];
				
				QueryParam param1 = new QueryParam();
				param1.setColumn("PROCESSNAME");
				param1.setOperator("EQUALS");
				List<String> values = new ArrayList<>();
				values.add("test-process");
				param1.setValue(values);
				
				parameters[0] = param1;
				
				querySpec.setParameters(parameters);
				
				return querySpec;
			}
		};
		
		ProcessInstanceList processInstances = qst.getWithFilters(3, 11, queryCallback, reqCallback);
		assertEquals(2, processInstances.getProcessInstances().length);
	}
	
	
	@Test
	public void testTransformProcessInstanceCollection() {
		QueryResultMapper qrmMock = Mockito.mock(QueryResultMapper.class);
		when(qrmMock.getType()).thenReturn(ProcessInstanceWithVarsDesc.class);
		
		ProcessInstanceList processInstances = (ProcessInstanceList) qst.transform(getProcessInstances(), qrmMock);
		
		assertEquals(2,  processInstances.getProcessInstances().length);
	}
	
	@Test
	public void testTransformUserTaskInstanceCollection() {
		QueryResultMapper qrmMock = Mockito.mock(QueryResultMapper.class);
		when(qrmMock.getType()).thenReturn(UserTaskInstanceWithVarsDesc.class);
		
		TaskInstanceList taskInstances = (TaskInstanceList) qst.transform(getUserTaskInstances(), qrmMock);
		
		assertEquals(1,  taskInstances.getTasks().length);
	}
	
	/**
	 * Transforming an instance that is not a {@link Collection} should return the same instance.
	 */
	@Test
	public void testTransformNoCollection() {
		QueryResultMapper qrmMock = Mockito.mock(QueryResultMapper.class);
		when(qrmMock.getType()).thenReturn(String.class);
		
		String nonCollection = "This is not a collection.";
		Object transformed = qst.transform(nonCollection, qrmMock);
		assertEquals(nonCollection, transformed);
	}
	
	@Test
	public void testTransformNull() {
		QueryResultMapper qrmMock = Mockito.mock(QueryResultMapper.class);
		when(qrmMock.getType()).thenReturn(String.class);
		
		Object transformed = qst.transform(null, qrmMock);
		assertNull(transformed);
	}
	
	private Collection<ProcessInstanceWithVarsDesc> getProcessInstances() {
		List<ProcessInstanceWithVarsDesc> processInstances = new ArrayList<>();
		
		ProcessInstanceWithVarsDesc processInstance1 = new org.jbpm.kie.services.impl.model.ProcessInstanceWithVarsDesc(42L, "test-process-id", "test-process", "1.0", 1, "test-deployment", new Date(), "mswiderski", "test description", "ck101");
		ProcessInstanceWithVarsDesc processInstance2 = new org.jbpm.kie.services.impl.model.ProcessInstanceWithVarsDesc(43L, "anotehr-process-id", "another-process", "1.3", 2, "another-deployment", new Date(), "mfusco", "another description", "ck102");
		
		processInstances.add(processInstance1);
		processInstances.add(processInstance2);
		
		return processInstances;
	}
	
	private Collection<UserTaskInstanceWithVarsDesc> getUserTaskInstances() {
		List<UserTaskInstanceWithVarsDesc> tasks = new ArrayList<>();
		
		UserTaskInstanceWithVarsDesc task1 = new org.jbpm.kie.services.impl.model.UserTaskInstanceWithVarsDesc(new Long(1L), "RESERVED", new Date(), "test-task", "Test task", new Integer(1), "ddoyle", "mswiderski", "test-deployment", "test-process", new Long(1L), new Date(), new Date());
		
		tasks.add(task1);
		
		return tasks;
	}
}

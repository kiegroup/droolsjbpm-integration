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

package org.kie.server.services.jbpm.search.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.search.util.H2ProcessInstanceQueryStrategy;
import org.kie.server.services.jbpm.search.util.H2TaskQueryStrategy;
import org.kie.server.services.jbpm.search.util.OracleProcessInstanceQueryStrategy;
import org.kie.server.services.jbpm.search.util.OracleTaskQueryStrategy;
import org.kie.server.services.jbpm.search.util.PostgreSQLProcessInstanceQueryStrategy;
import org.kie.server.services.jbpm.search.util.PostgreSQLTaskQueryStrategy;
import org.kie.server.services.jbpm.search.util.QueryStrategyFactory;
import org.mockito.Mockito;

public class QueryStrategyFactoryTest {

	@Test
	public void testH2Strategy() {
		KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(registryMock.getConfig()).thenReturn(configMock);
		when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.H2Dialect");
		
		QueryStrategyFactory h2ProcessStrategyFactory = new QueryStrategyFactory(registryMock);
		assertTrue(h2ProcessStrategyFactory.getProcessQueriesStrategy() instanceof H2ProcessInstanceQueryStrategy);
		assertTrue(h2ProcessStrategyFactory.getTaskQueriesStrategy() instanceof H2TaskQueryStrategy);
	}
	
	@Test
	public void testPostgreSQLStrategy() {
		KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(registryMock.getConfig()).thenReturn(configMock);
		when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.PostgreSQLDialect");
		
		QueryStrategyFactory h2ProcessStrategyFactory = new QueryStrategyFactory(registryMock);
		assertTrue(h2ProcessStrategyFactory.getProcessQueriesStrategy() instanceof PostgreSQLProcessInstanceQueryStrategy);
		assertTrue(h2ProcessStrategyFactory.getTaskQueriesStrategy() instanceof PostgreSQLTaskQueryStrategy);
	}
	
	@Test
	public void testOracle8iSQLStrategy() {
		KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(registryMock.getConfig()).thenReturn(configMock);
		when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.Oracle8iDialect");
		
		QueryStrategyFactory h2ProcessStrategyFactory = new QueryStrategyFactory(registryMock);
		assertTrue(h2ProcessStrategyFactory.getProcessQueriesStrategy() instanceof OracleProcessInstanceQueryStrategy);
		assertTrue(h2ProcessStrategyFactory.getTaskQueriesStrategy() instanceof OracleTaskQueryStrategy);
	}
	
	@Test
	public void testOracle9iSQLStrategy() {
		KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(registryMock.getConfig()).thenReturn(configMock);
		when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.Oracle9iDialect");
		
		QueryStrategyFactory h2ProcessStrategyFactory = new QueryStrategyFactory(registryMock);
		assertTrue(h2ProcessStrategyFactory.getProcessQueriesStrategy() instanceof OracleProcessInstanceQueryStrategy);
		assertTrue(h2ProcessStrategyFactory.getTaskQueriesStrategy() instanceof OracleTaskQueryStrategy);
	}
	
	@Test
	public void testOracle10gSQLStrategy() {
		KieServerRegistry registryMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(registryMock.getConfig()).thenReturn(configMock);
		when(configMock.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")).thenReturn("org.hibernate.dialect.Oracle10gDialect");
		
		QueryStrategyFactory h2ProcessStrategyFactory = new QueryStrategyFactory(registryMock);
		assertTrue(h2ProcessStrategyFactory.getProcessQueriesStrategy() instanceof OracleProcessInstanceQueryStrategy);
		assertTrue(h2ProcessStrategyFactory.getTaskQueriesStrategy() instanceof OracleTaskQueryStrategy);
	}
	
}

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import org.jbpm.services.api.query.QueryService;
import org.junit.Test;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.search.JbpmSearchKieServerExtension;
import org.mockito.Mockito;

public class JbpmSearchKieServerExtensionTest {

	@Test
	public void testGetAppComponents() throws Exception {
		
		JbpmSearchKieServerExtension kieServerExtension = new JbpmSearchKieServerExtension();
	
		Field contextField = kieServerExtension.getClass().getDeclaredField("context");
		Field queryServiceField = kieServerExtension.getClass().getDeclaredField("queryService");
		
		contextField.setAccessible(true);
		queryServiceField.setAccessible(true);
		
		contextField.set(kieServerExtension, Mockito.mock(KieServerRegistry.class));
		queryServiceField.set(kieServerExtension, Mockito.mock(QueryService.class));
		
		List<Object> appComponents = kieServerExtension.getAppComponents(SupportedTransports.REST);
		
		assertEquals(3, appComponents.size());
	}
	
	
	/**
	 * Helper class to test retrieval of AppComponents. 
	 * <p>
	 * This class is loaded by the {@link ServiceLoader}.
	 */
	public static class TestApplicationComponentsService implements KieServerApplicationComponentsService {

		@Override
		public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
			// The services past by the JbpmKieServerExtension test.
			assertEquals(2, services.length);
			
			//Return services to the JbpmKieServerExtension.
			List<Object> appComponents = new ArrayList<>();
			
			appComponents.add("Test-Component");
			appComponents.add("Another-test-component");
			appComponents.add("Third-test-component");
			
			return appComponents;
		}
	}
	
}

package org.kie.server.services.swagger;

/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;
import org.kie.server.api.KieServerEnvironment;

import io.swagger.config.SwaggerConfig;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;

public class SwaggerKierServerExtensionTest {
	
	@Test
	public void testInit() throws Exception {
		resetSwaggerConfig(SwaggerContextService.CONFIG_ID_DEFAULT);
		
		KieServerEnvironment.setContextRoot("kie-server");
		SwaggerKieServerExtension extension = new SwaggerKieServerExtension();
		extension.init(null, null);
		BeanConfig config = (BeanConfig) SwaggerConfigLocator.getInstance().getConfig(SwaggerContextService.CONFIG_ID_DEFAULT);
		assertEquals("/kie-server/services/rest", config.getBasePath());
	}
	
	@Test
	public void testInitWithEmptyContextRoot() throws Exception {
		resetSwaggerConfig(SwaggerContextService.CONFIG_ID_DEFAULT);
		
		KieServerEnvironment.setContextRoot("");
		SwaggerKieServerExtension extension = new SwaggerKieServerExtension();
		extension.init(null, null);
		BeanConfig config = (BeanConfig) SwaggerConfigLocator.getInstance().getConfig(SwaggerContextService.CONFIG_ID_DEFAULT);
		assertEquals("/services/rest", config.getBasePath());
	}
	

	//Swagger only allows to load a config once for a given id, so for testing purposes, we need to reset the config.
	private void resetSwaggerConfig(String configId) throws Exception {
		SwaggerConfigLocator swaggerConfigLocator = SwaggerConfigLocator.getInstance();
		
		Field configMapField = SwaggerConfigLocator.class.getDeclaredField("configMap");
		configMapField.setAccessible(true);
		ConcurrentMap<String, SwaggerConfig> configMap = (ConcurrentMap<String, SwaggerConfig>) configMapField.get(swaggerConfigLocator);
		configMap.remove(SwaggerContextService.CONFIG_ID_DEFAULT);
	}
	
}

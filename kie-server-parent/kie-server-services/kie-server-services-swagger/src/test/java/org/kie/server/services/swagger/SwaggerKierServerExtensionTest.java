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

package org.kie.server.services.swagger;

import static org.junit.Assert.*;


import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;
import org.kie.server.api.KieServerEnvironment;

public class SwaggerKierServerExtensionTest {
	
	@Test
	public void testInit() throws Exception {
		commonVerify("kie-server");
		}
	
	@Test
	public void testInitWithEmptyContextRoot() throws Exception {
		commonVerify("");
	}

	private void commonVerify(String contextRoot) {
		KieServerEnvironment.setContextRoot(contextRoot);
		SwaggerKieServerExtension extension = new SwaggerKieServerExtension();
		extension.init(null, null);
		OpenApiContext ctx = OpenApiContextLocator.getInstance().getOpenApiContext(OpenApiContext.OPENAPI_CONTEXT_ID_DEFAULT);

		SwaggerConfiguration config = (SwaggerConfiguration) ctx.getOpenApiConfiguration();

		OpenAPI openAPI = config.getOpenAPI();
		assertEquals(1, openAPI.getServers().size());
		assertTrue(openAPI.getServers().stream().anyMatch(server -> server.getUrl().equals("kie-server/services/rest")));
	}
}

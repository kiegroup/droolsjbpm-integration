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

package org.kie.server.remote.rest.swagger;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_REST_MODE_READONLY;
import static org.kie.server.remote.rest.swagger.ReadOnlySwaggerDecoration.SWAGGER_EXTENSION_READ_ONLY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.swagger.SwaggerKieServerExtension;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtensions;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.oas.models.Operation;

public class SwaggerRestApplicationComponentsService implements KieServerApplicationComponentsService {

	private static final String OWNER_EXTENSION = SwaggerKieServerExtension.EXTENSION_NAME;

	@Override
	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
		// skip calls from other than owning extension
		if (!OWNER_EXTENSION.equals(extension)) {
			return Collections.emptyList();
		}
				
        // add filter only if the history mode is active
        if (Boolean.getBoolean(KIE_SERVER_REST_MODE_READONLY)) {
            /*
            SwaggerSpecFilter filter = new AbstractSpecFilter() {

                @Override
                public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
                    return operation.getVendorExtensions().containsKey(SWAGGER_EXTENSION_READ_ONLY);
                }

            };
            FilterFactory.setFilter(filter);
             */
            try {
                new JaxrsOpenApiContextBuilder<>()
                        .openApiConfiguration(
                                new SwaggerConfiguration()
                                        .filterClass(ReadOnlySpecFilter.class.getName())
                        )
                        .buildContext(true);
            } catch (OpenApiConfigurationException e) {
                throw new RuntimeException("Failed to register OpenAPI filter", e);
            }
            List<OpenAPIExtension> extensions = new ArrayList<>(OpenAPIExtensions.getExtensions());
            extensions.add(new ReadOnlySwaggerDecoration());
            OpenAPIExtensions.setExtensions(extensions);
        }
		List<Object> components = new ArrayList<Object>(2);
		// Swagger Resources
        // kept for backward compatibility, to still have the server/swagger.() rest
        components.add(new KieApiListingResource());
        // provides the new /openapi.() rest
		components.add(new OpenApiResource());
		components.add(new SwaggerSerializers());

		return components;
	}

    private static class ReadOnlySpecFilter extends AbstractSpecFilter {
        @Override
        public Optional<Operation> filterOperation(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
            return operation.getExtensions().containsKey(SWAGGER_EXTENSION_READ_ONLY) ? Optional.of(operation) : Optional.empty();
        }
    }

}

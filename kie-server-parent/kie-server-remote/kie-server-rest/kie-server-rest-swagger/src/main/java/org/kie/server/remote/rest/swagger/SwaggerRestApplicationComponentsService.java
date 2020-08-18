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

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.swagger.SwaggerKieServerExtension;

import io.swagger.config.FilterFactory;
import io.swagger.core.filter.AbstractSpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.model.ApiDescription;
import io.swagger.models.Operation;

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
            SwaggerSpecFilter filter = new AbstractSpecFilter() {

                @Override
                public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
                    return operation.getVendorExtensions().containsKey(SWAGGER_EXTENSION_READ_ONLY);
                }

            };
            FilterFactory.setFilter(filter);
            List<SwaggerExtension> extensions = new ArrayList<>(SwaggerExtensions.getExtensions());
            extensions.add(new ReadOnlySwaggerDecoration());
            SwaggerExtensions.setExtensions(extensions);
        }
		List<Object> components = new ArrayList<Object>(2);
		// Swagger Resources
		components.add(new KieApiListingResource());
		components.add(new SwaggerSerializers());

		return components;
	}

}

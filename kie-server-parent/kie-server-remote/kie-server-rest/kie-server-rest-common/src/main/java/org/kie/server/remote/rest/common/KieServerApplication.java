/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.common;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_REST_MODE_READONLY;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.kie.server.remote.rest.common.marker.KieServerEndpointRequestFilter;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class KieServerApplication extends Application {

	private static final Logger logger = LoggerFactory.getLogger(KieServerApplication.class);

	private final Set<Object> instances;
	
	public KieServerApplication() {
		logger.debug("KieServerApplication started");
		instances = new CopyOnWriteArraySet<Object>() {
			private static final long serialVersionUID = 1763183096852523317L;
			{
				KieServerImpl server = KieServerLocator.getInstance();

				add(new KieServerRestImpl(server));

				// next add any resources from server extensions
				List<KieServerExtension> extensions = server.getServerExtensions();

				for (KieServerExtension extension : extensions) {
					logger.debug("Adding extension " + extension.toString());
					addAll(extension.getAppComponents(SupportedTransports.REST));
				}
                // add filter only if the history mode is active
                if (Boolean.getBoolean(KIE_SERVER_REST_MODE_READONLY)) {
                    add(new KieServerEndpointRequestFilter());
                }
			}
		};
		logger.debug("Instances discovered: " + instances.size());
	}

	@Override
	public Set<Object> getSingletons() {
		return instances;
	}

}

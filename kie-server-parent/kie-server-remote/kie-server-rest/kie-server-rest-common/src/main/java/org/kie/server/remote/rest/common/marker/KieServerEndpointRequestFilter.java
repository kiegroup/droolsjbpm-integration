/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.common.marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Provider
public class KieServerEndpointRequestFilter implements ContainerRequestFilter {

    private final List<EndpointType> valid = Arrays.asList(KieServerEndpoint.EndpointType.ALWAYS, KieServerEndpoint.EndpointType.HISTORY);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        KieServerEndpoint endpoint = null;
        if (resourceInfo.getResourceMethod().isAnnotationPresent(KieServerEndpoint.class)) {
            endpoint = resourceInfo.getResourceMethod().getAnnotation(KieServerEndpoint.class);
        } else if(resourceInfo.getResourceClass().isAnnotationPresent(KieServerEndpoint.class)) {
            endpoint = resourceInfo.getResourceClass().getAnnotation(KieServerEndpoint.class);
        } 

        // we allow any get method
        if ("GET".equals(requestContext.getMethod())) {
            return;
        }

        // we disallow any other method if there is no annotation
        if (endpoint == null) {
            requestContext.abortWith(Response.status(SERVICE_UNAVAILABLE).entity("Endpoint disabled").build());
            return;
        }

        // if there is annotation we allow it only if its marked as always or history
        List<EndpointType> current = new ArrayList<>(asList(endpoint.categories()));
        current.retainAll(valid);
        if(current.isEmpty()) {
            requestContext.abortWith(Response.status(SERVICE_UNAVAILABLE).entity("Endpoint disabled").build());
        }
    }
}
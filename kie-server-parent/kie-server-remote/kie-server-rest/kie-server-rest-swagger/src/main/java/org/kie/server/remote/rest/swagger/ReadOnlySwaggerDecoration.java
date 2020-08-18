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

package org.kie.server.remote.rest.swagger;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;

import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;


public class ReadOnlySwaggerDecoration extends AbstractSwaggerExtension {

    public static final String SWAGGER_EXTENSION_READ_ONLY = "x-readonly"; 
    
    private final List<EndpointType> valid = Arrays.asList(KieServerEndpoint.EndpointType.ALWAYS, KieServerEndpoint.EndpointType.HISTORY);

    @Override
    public void decorateOperation(Operation operation, Method method, Iterator<SwaggerExtension> chain) {
        if(method.isAnnotationPresent(GET.class)) {
            operation.setVendorExtension(SWAGGER_EXTENSION_READ_ONLY, Boolean.TRUE);
            if (chain.hasNext()) {
                chain.next().decorateOperation(operation, method, chain);
            }
            return;
        }

        KieServerEndpoint api = method.getAnnotation(KieServerEndpoint.class);
        if(api == null) {
            api = method.getDeclaringClass().getAnnotation(KieServerEndpoint.class);
        }
        
        if (api != null) {
            List<EndpointType> current = new ArrayList<>(asList(api.categories()));
            current.retainAll(valid);
            if(!current.isEmpty()) {
                operation.setVendorExtension(SWAGGER_EXTENSION_READ_ONLY, Boolean.TRUE);
            }
        }

        if (chain.hasNext()) {
            chain.next().decorateOperation(operation, method, chain);
        }
    }
}

/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.integrationtests.config;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.ext.ContextResolver;

/**
 * This class is needed when running the tests using TJWS as we need to specify the marshalling configuration for
 * JSON. There is some RestEasy specific configuration that is required by the TJWS.
 */
public class JacksonRestEasyTestConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonRestEasyTestConfig() {
        mapper = new ObjectMapper();
        AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspectorPair = new AnnotationIntrospector.Pair(primary, secondary);
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().withAnnotationIntrospector(introspectorPair));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withAnnotationIntrospector(introspectorPair));
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
        return mapper;
    }

    public static ResteasyProviderFactory createRestEasyProviderFactory() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        ContextResolver<ObjectMapper> contextResolver = new JacksonRestEasyTestConfig();
        factory.addContextResolver(contextResolver);
        return factory;
    }

}
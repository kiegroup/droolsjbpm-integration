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
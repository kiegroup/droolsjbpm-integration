package org.kie.server.client;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.ws.rs.ext.ContextResolver;

public class JacksonConfig implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonConfig() {
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
}
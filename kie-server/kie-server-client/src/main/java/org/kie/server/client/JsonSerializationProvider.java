package org.kie.server.client;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;

public class JsonSerializationProvider implements SerializationProvider {

    private final ObjectMapper objectMapper;

    public JsonSerializationProvider() {
        objectMapper = new ObjectMapper();
        // this is needed because we are using Jackson 1.x which by default ignores Jaxb annotations
        // one we move to Jackson 2.x, the config below should not be needed
        AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspectorPair = new AnnotationIntrospector.Pair(primary, secondary);
        objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig().withAnnotationIntrospector(introspectorPair));
        objectMapper.setSerializationConfig(objectMapper.getSerializationConfig().withAnnotationIntrospector(introspectorPair));
    }

    @Override
    public String serialize(Object objectInput) {
        try {
            return objectMapper.writeValueAsString(objectInput);
        } catch (IOException e) {
            throw new SerializationException("Can't serialize the provided object!", e);
        }
    }

    @Override
    public Object deserialize(String serializedInput) {
        Class<?> type = null;
        return deserialize(serializedInput, type);
    }

    @Override
    public <T> T deserialize(String serializedInput, Class<T> type) {
        try {
            return objectMapper.readValue(serializedInput, type);
        } catch (IOException e) {
            throw new SerializationException("Can't deserialize provided string!", e);
        }
    }

}

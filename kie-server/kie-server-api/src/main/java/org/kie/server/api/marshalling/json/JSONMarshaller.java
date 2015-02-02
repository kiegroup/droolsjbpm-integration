package org.kie.server.api.marshalling.json;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;


import java.io.IOException;

public class JSONMarshaller implements Marshaller {

    private final ObjectMapper objectMapper;

    public JSONMarshaller() {
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
    public String marshall(Object objectInput) {
        try {
            return objectMapper.writeValueAsString(objectInput);
        } catch (IOException e) {
            throw new MarshallingException("Error marshalling input", e);
        }
    }

    @Override
    public <T> T unmarshall(String serializedInput, Class<T> type) {
        try {
            return objectMapper.readValue(serializedInput, type);
        } catch (IOException e) {
            throw new MarshallingException("Error unmarshalling input", e);
        }
    }

    @Override
    public void dispose() {

    }

}

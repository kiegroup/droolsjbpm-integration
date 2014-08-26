package org.kie.services.client.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class JsonSerializationProvider implements SerializationProvider {

    public final static int JMS_SERIALIZATION_TYPE = 1;

    private ObjectMapper mapper = new JaxbJacksonObjectMapper();
    private Class<?> outputType = null;

    public JsonSerializationProvider() {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonGenerationException jge) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", jge);
        } catch (JsonMappingException jme) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", jme);
        } catch (IOException ie) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", ie);
        }
    }

    @Override
    public Object deserialize(String jsonStr) {
        return deserialize(jsonStr, this.outputType);
    }
    
    public void setDeserializeOutputClass(Class<?> type) {
        this.outputType = type;
    }

    public <T> T deserialize(String jsonStr, Class<T> type) { 
        try {
            return mapper.readValue(jsonStr, type);
        } catch (JsonGenerationException jge) {
            throw new SerializationException("Unable to deserialize String " + type.getClass().getSimpleName() + " instance", jge);
        } catch (JsonMappingException jme) {
            throw new SerializationException("Unable to deserialize String " + type.getClass().getSimpleName() + " instance", jme);
        } catch (IOException ie) {
            throw new SerializationException("Unable to deserialize String to " + type.getClass().getSimpleName() + " instance", ie);
        }
    }

    private static class JaxbJacksonObjectMapper extends ObjectMapper {

        public JaxbJacksonObjectMapper() {
            super();

            final AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();

            this.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
            this.configure(org.codehaus.jackson.map.SerializationConfig.Feature.WRAP_ROOT_VALUE, true);

            this.setDeserializationConfig(this.getDeserializationConfig().withAnnotationIntrospector(introspector));
            this.setSerializationConfig(this.getSerializationConfig().withAnnotationIntrospector(introspector));

            this.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        }
    }

    @Override
    public void dispose() {
        if( this.mapper != null ) { 
            this.mapper = null;
        }
    }

}

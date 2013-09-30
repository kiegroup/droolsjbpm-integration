package org.kie.services.client.serialization.jaxb;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;



// TODO: Add object version checking
public class JsonSerializationProvider {
   
    private static ObjectMapper mapper = new JaxbJacksonObjectMapper();
    
    static { 
      AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector();
      mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    }
    
    public static String convertJaxbObjectToJsonString(Object object) throws JsonGenerationException, JsonMappingException, IOException {
        return mapper.writeValueAsString(object);
    }
    
    public static Object convertJsonStringToJaxbObject(String jsonStr, Class<?> type) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(jsonStr, type);
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
    
}

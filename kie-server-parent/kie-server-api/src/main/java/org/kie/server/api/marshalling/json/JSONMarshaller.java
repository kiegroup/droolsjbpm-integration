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

package org.kie.server.api.marshalling.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;

public class JSONMarshaller implements Marshaller {

    private final ClassLoader classLoader;
    private final ObjectMapper objectMapper;

//    private final ObjectMapper wrappedObjectMapper;

    private final ObjectMapper fallbackObjectMapper;

    public JSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        this.classLoader = classLoader;
        objectMapper = new ObjectMapper();

        List<NamedType> customClasses = prepareCustomClasses(classes);
        // this is needed because we are using Jackson 1.x which by default ignores Jaxb annotations
        // one we move to Jackson 2.x, the config below should not be needed
        // and to pass custom class types so they can be easily deserialized - need to be checked with Jackson 2.x how to achieve that
        AnnotationIntrospector primary = new ExtendedJaxbAnnotationIntrospector(customClasses);
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspectorPair = new AnnotationIntrospector.Pair(primary, secondary);
        objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig().withAnnotationIntrospector(introspectorPair));
        objectMapper.setSerializationConfig(objectMapper.getSerializationConfig().withAnnotationIntrospector(introspectorPair));
        // in case there are custom classes register module to deal with them both for serialization and deserialization
        // this module makes sure that only custom classes are equipped with type information
        if (classes != null && !classes.isEmpty()) {
            ObjectMapper customObjectMapper = new ObjectMapper();
            customObjectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);

            SimpleModule mod = new SimpleModule("custom-object-mapper", Version.unknownVersion());
            mod.addDeserializer(Object.class, new CustomObjectDeserializer(classes, customObjectMapper));

            CustomObjectSerializer customObjectSerializer = new CustomObjectSerializer(customObjectMapper);

            for (Class<?> clazz : classes) {
                mod.addSerializer(clazz, customObjectSerializer);
            }
            objectMapper.registerModule(mod);
        }

        fallbackObjectMapper = new ObjectMapper();

    }

    protected List<NamedType> prepareCustomClasses(Set<Class<?>> classes) {
        List<NamedType> customClasses = new ArrayList<NamedType>();
        if (classes != null) {
            for (Class<?> clazz : classes) {
                customClasses.add(new NamedType(clazz, clazz.getSimpleName()));
                customClasses.add(new NamedType(clazz, clazz.getName()));
            }
        }

        return customClasses;
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
        } catch (JsonMappingException e){

            // in case of mapping exception try with object mapper without annotation introspection
            try {
                return fallbackObjectMapper.readValue(serializedInput, type);
            } catch (IOException ex) {

            }
            throw new MarshallingException("Error unmarshalling input", e);
        } catch (IOException e) {

            throw new MarshallingException("Error unmarshalling input", e);
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public MarshallingFormat getFormat() {
        return MarshallingFormat.JSON;
    }

    class ExtendedJaxbAnnotationIntrospector extends JaxbAnnotationIntrospector {

        private List<NamedType> customClasses;

        public ExtendedJaxbAnnotationIntrospector(List<NamedType> customClasses) {
            this.customClasses = customClasses;
        }

        @Override
        public List<NamedType> findSubtypes(Annotated a) {
            List<NamedType> base = super.findSubtypes(a);

            List<NamedType> complete = new ArrayList<NamedType>();
            if (base != null) {
                complete.addAll(base);
            }
            if (customClasses != null) {
                complete.addAll(customClasses);
            }
            return complete;
        }
    }

    class CustomObjectSerializer extends JsonSerializer<Object> {

        private ObjectMapper customObjectMapper;

        public CustomObjectSerializer(ObjectMapper customObjectMapper) {
            this.customObjectMapper = customObjectMapper;
        }

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

            String json = customObjectMapper.writeValueAsString(value);
            jgen.writeRawValue(json);
        }
    }

    class CustomObjectDeserializer extends UntypedObjectDeserializer {

        private static final long serialVersionUID = 7764405880012867708L;

        private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
        private ObjectMapper customObjectMapper;

        public CustomObjectDeserializer(Set<Class<?>> classes,  ObjectMapper customObjectMapper) {
            this.customObjectMapper = customObjectMapper;
            for (Class<?> c : classes) {
                this.classes.put(c.getSimpleName(), c);
                this.classes.put(c.getName(), c);
            }
        }

        @Override
        protected Object mapObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.START_OBJECT) {
                t = jp.nextToken();
            }
            // 1.6: minor optimization; let's handle 1 and 2 entry cases separately
            if (t != JsonToken.FIELD_NAME) { // and empty one too
                // empty map might work; but caller may want to modify... so better just give small modifiable
                return new LinkedHashMap<String,Object>(4);
            }
            String field1 = jp.getText();
            jp.nextToken();
            if (classes.containsKey(field1)) {
                Object value = objectMapper.readValue(jp, classes.get(field1));
                jp.nextToken();

                return value;
            } else {
                Object value1 = deserialize(jp, ctxt);
                if (jp.nextToken() != JsonToken.FIELD_NAME) { // single entry; but we want modifiable
                    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
                    result.put(field1, value1);
                    return result;
                }
                String field2 = jp.getText();
                jp.nextToken();
                Object value2 = deserialize(jp, ctxt);
                if (jp.nextToken() != JsonToken.FIELD_NAME) {
                    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
                    result.put(field1, value1);
                    result.put(field2, value2);
                    return result;
                }
                // And then the general case; default map size is 16
                LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
                result.put(field1, value1);
                result.put(field2, value2);
                do {
                    String fieldName = jp.getText();
                    jp.nextToken();
                    result.put(fieldName, deserialize(jp, ctxt));
                } while (jp.nextToken() != JsonToken.END_OBJECT);
                return result;
            }
        }
    }
}

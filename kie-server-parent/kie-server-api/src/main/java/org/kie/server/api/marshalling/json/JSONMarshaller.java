/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.runtime.rule.impl.FlatQueryResults;
import org.drools.core.xml.jaxb.util.JaxbListAdapter;
import org.drools.core.xml.jaxb.util.JaxbListWrapper;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.type.JaxbByteArray;

public class JSONMarshaller implements Marshaller {

    private static boolean formatDate = Boolean.parseBoolean(System.getProperty("org.kie.server.json.format.date", "false"));

    private final ClassLoader classLoader;
    private final ObjectMapper objectMapper;

    private final ObjectMapper fallbackObjectMapper;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");

    public JSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        this.classLoader = classLoader;
        objectMapper = new ObjectMapper();

        ObjectMapper customSerializationMapper = new ObjectMapper();

        if (classes == null) {
            classes = new HashSet<Class<?>>();
        }
        // add byte array handling support to allow byte[] to be send as payload
        classes.add(JaxbByteArray.class);

        List<NamedType> customClasses = prepareCustomClasses(classes);
        // this is needed because we are using Jackson 1.x which by default ignores Jaxb annotations
        // one we move to Jackson 2.x, the config below should not be needed
        // and to pass custom class types so they can be easily deserialized - need to be checked with Jackson 2.x how to achieve that
        AnnotationIntrospector primary = new ExtendedJaxbAnnotationIntrospector(customClasses, customSerializationMapper);
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspectorPair = new AnnotationIntrospector.Pair(primary, secondary);
        objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig()
                .withAnnotationIntrospector(introspectorPair)
                .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES));
        objectMapper.setSerializationConfig(objectMapper.getSerializationConfig()
                .withAnnotationIntrospector(introspectorPair)
                .with(SerializationConfig.Feature.INDENT_OUTPUT));

        // setup custom serialization mapper with jaxb adapters
        customSerializationMapper.setDeserializationConfig(customSerializationMapper.getDeserializationConfig().withAnnotationIntrospector(introspectorPair));
        customSerializationMapper.setSerializationConfig(customSerializationMapper.getSerializationConfig().withAnnotationIntrospector(introspectorPair).with(SerializationConfig.Feature.INDENT_OUTPUT));


        // in case there are custom classes register module to deal with them both for serialization and deserialization
        // this module makes sure that only custom classes are equipped with type information
        if (classes != null && !classes.isEmpty()) {
            ObjectMapper customObjectMapper = new ObjectMapper();
            customObjectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);

            SimpleModule mod = new SimpleModule("custom-object-mapper", Version.unknownVersion());
            mod.addDeserializer(Object.class, new CustomObjectDeserializer(classes));

            CustomObjectSerializer customObjectSerializer = new CustomObjectSerializer(customObjectMapper);

            for (Class<?> clazz : classes) {
                mod.addSerializer(clazz, customObjectSerializer);
            }

            objectMapper.registerModule(mod);
            customSerializationMapper.registerModule(mod);
        }

        fallbackObjectMapper = new ObjectMapper();

        if (formatDate) {

            objectMapper.setDateFormat(dateFormat);
            fallbackObjectMapper.setDateFormat(dateFormat);
            customSerializationMapper.setDateFormat(dateFormat);

            objectMapper.getDeserializationConfig().withDateFormat(dateFormat);
            fallbackObjectMapper.getDeserializationConfig().withDateFormat(dateFormat);
            customSerializationMapper.getDeserializationConfig().withDateFormat(dateFormat);

            objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
            fallbackObjectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
            customSerializationMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

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
            return objectMapper.writeValueAsString(wrap(objectInput));

        } catch (IOException e) {
            throw new MarshallingException("Error marshalling input", e);
        }
    }

    @Override
    public <T> T unmarshall(String serializedInput, Class<T> type) {
        try {

            return (T) unwrap(objectMapper.readValue(serializedInput, type));
        } catch (JsonMappingException e){

            // in case of mapping exception try with object mapper without annotation introspection
            try {
                return (T) unwrap(fallbackObjectMapper.readValue(serializedInput, type));
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

    protected Object wrap(Object data) {
        if (data instanceof byte[]) {
            return new JaxbByteArray((byte[]) data);
        }

        return data;
    }

    protected Object unwrap(Object data) {
        if (data instanceof Wrapped) {
            return ((Wrapped) data).unwrap();
        }

        return data;
    }


    class ExtendedJaxbAnnotationIntrospector extends JaxbAnnotationIntrospector {

        private List<NamedType> customClasses;
        private ObjectMapper customObjectMapper;

        public ExtendedJaxbAnnotationIntrospector(List<NamedType> customClasses, ObjectMapper anotherCustomObjectMapper) {
            this.customClasses = customClasses;

            this.customObjectMapper = anotherCustomObjectMapper;
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

        @Override
        public JsonSerializer<?> findSerializer(Annotated am) {
            // replace JaxbUnknownAdapter as it breaks JSON marshaller for list and maps with wrapping serializer
            XmlJavaTypeAdapter adapterInfo = findAnnotation(XmlJavaTypeAdapter.class, am, true, false, false);
            if (adapterInfo != null && adapterInfo.value().isAssignableFrom(JaxbUnknownAdapter.class)) {
                return new WrappingObjectSerializer(customObjectMapper);
            }

            return super.findSerializer(am);

        }

        @Override public JsonDeserializer<?> findDeserializer(Annotated am) {
            return super.findDeserializer(am);
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

    class WrappingObjectSerializer extends JsonSerializer<Object> {

        private ObjectMapper customObjectMapper;

        public WrappingObjectSerializer(ObjectMapper customObjectMapper) {
            this.customObjectMapper = customObjectMapper;
        }

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            String className = value.getClass().getName();
            String json = customObjectMapper.writeValueAsString(value);

            // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
            if (!className.startsWith("java.") && !className.startsWith("javax.") && !json.contains(className))  {
                json = "{\""+ className +"\":" + json + "}";
            }
            jgen.writeRawValue(json);
        }
    }

    class CustomObjectDeserializer extends UntypedObjectDeserializer {

        private final Pattern VALID_JAVA_IDENTIFIER = Pattern.compile("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");


        private static final long serialVersionUID = 7764405880012867708L;

        private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

        public CustomObjectDeserializer(Set<Class<?>> classes) {

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
                return new LinkedHashMap<String, Object>(4);
            }
            String field1 = jp.getText();
            jp.nextToken();
            if (classes.containsKey(field1)) {
                Object value = objectMapper.readValue(jp, classes.get(field1));
                jp.nextToken();

                return value;
            } else {
                if (isFullyQualifiedClassname(field1)) {
                    try {
                        Object value = objectMapper.readValue(jp, Class.forName(field1, true, classLoader));
                        jp.nextToken();

                        return value;
                    } catch (ClassNotFoundException e) {
                    }
                }

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

                // in case nested jaxb list wrapper was not recognized automatically map it manually
                if (result.containsKey("type") && result.containsKey("componentType") && result.containsKey("element")) {
                    JaxbListWrapper wrapper = new JaxbListWrapper();
                    wrapper.setType(JaxbListWrapper.JaxbWrapperType.valueOf((String)result.get("type")));
                    wrapper.setComponentType((String)result.get("componentType"));
                    wrapper.setElements(toArray(result.get("element")));

                    try {
                        Object data = null;
                        if (wrapper.getType().equals(JaxbListWrapper.JaxbWrapperType.MAP)) {
                            Map<Object, Object> tranformed = new LinkedHashMap<Object, Object>();
                            // this is mapped to JaxbStringObjectPair
                            for (Object element : wrapper.getElements()) {
                                Map<Object, Object> map = (Map<Object, Object>) element;
                                tranformed.put(map.get("key"), map.get("value"));
                            }
                            data = tranformed;
                        } else {
                            data = new JaxbListAdapter().unmarshal(wrapper);
                        }
                        return data;
                    } catch (Exception e) {

                    }

                }
                return result;
            }
        }

        private Object[] toArray(Object element) {
            if (element != null) {

                if (element instanceof Collection) {
                    return ((Collection) element).toArray();
                }
            }

            return new Object[0];
        }

        private boolean isFullyQualifiedClassname(String classname) {

            if (!classname.contains(".")) {
                return false;
            }

            return VALID_JAVA_IDENTIFIER.matcher(classname).matches();
        }
    }
}

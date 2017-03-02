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
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
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
    private static String dateFormatStr = System.getProperty("org.kie.server.json.date_format", "yyyy-MM-dd'T'hh:mm:ss.SSSZ");

    private ThreadLocal<Boolean> stripped = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    protected ClassLoader classLoader;
    protected ObjectMapper objectMapper;

    protected Set<Class<?>> classesSet;

    protected ObjectMapper deserializeObjectMapper;

    protected DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);

    public JSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        this.classLoader = classLoader;
        buildMarshaller(classes, classLoader);

        configureMarshaller(classes, classLoader);
    }

    protected void buildMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {

        objectMapper = new ObjectMapper();
        deserializeObjectMapper = new ObjectMapper();
    }

    protected void configureMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {
        ObjectMapper customSerializationMapper = new ObjectMapper();
        if (classes == null) {
            classes = new HashSet<Class<?>>();
        }
        // add byte array handling support to allow byte[] to be send as payload
        classes.add(JaxbByteArray.class);

        List<NamedType> customClasses = prepareCustomClasses(classes);
        // this is needed because we need better control of serialization and deserialization
        AnnotationIntrospector primary = new ExtendedJaxbAnnotationIntrospector(customClasses, customSerializationMapper);
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspectorPair = AnnotationIntrospector.pair(primary, secondary);
        objectMapper.setConfig(
                objectMapper.getSerializationConfig()
                        .with(introspectorPair)
                        .with(SerializationFeature.INDENT_OUTPUT));

        deserializeObjectMapper.setConfig(objectMapper.getDeserializationConfig()
                .with(introspectorPair)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));


        // setup custom serialization mapper with jaxb adapters
        customSerializationMapper.setConfig(customSerializationMapper.getDeserializationConfig().with(introspectorPair));
        customSerializationMapper.setConfig(customSerializationMapper.getSerializationConfig().with(introspectorPair).with(SerializationFeature.INDENT_OUTPUT));


        // in case there are custom classes register module to deal with them both for serialization and deserialization
        // this module makes sure that only custom classes are equipped with type information
        if (classes != null && !classes.isEmpty()) {
            ObjectMapper customObjectMapper = new ObjectMapper();
            TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL){
                @Override
                public boolean useForType(JavaType t) {
                    if (classesSet.contains(t.getRawClass())) {
                        return true;
                    }
                    return false;
                }
            };
            typer = typer.init(JsonTypeInfo.Id.CLASS, null);
            typer = typer.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
            customObjectMapper.setDefaultTyping(typer);

            SimpleModule mod = new SimpleModule("custom-object-mapper", Version.unknownVersion());
            CustomObjectSerializer customObjectSerializer = new CustomObjectSerializer(customObjectMapper);

            for (Class<?> clazz : classes) {
                mod.addSerializer(clazz, customObjectSerializer);

            }

            objectMapper.registerModule(mod);
            TypeResolverBuilder<?> typer2 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL){
                @Override
                public boolean useForType(JavaType t) {
                    if (classesSet.contains(t.getRawClass())) {
                        return true;
                    }
                    return false;
                }

                @Override
                public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
                    if (useForType(baseType)) {
                        if (_idType == JsonTypeInfo.Id.NONE) {
                            return null;
                        }

                        TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
                        switch (_includeAs) {
                            case WRAPPER_OBJECT:
                                return new CustomAsWrapperTypeDeserializer(baseType, idRes, _typeProperty, true, _defaultImpl);

                        }
                    }

                    return super.buildTypeDeserializer(config, baseType, subtypes);
                }


            };
            typer2 = typer2.init(JsonTypeInfo.Id.CLASS, null);
            typer2 = typer2.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
            deserializeObjectMapper.setDefaultTyping(typer2);

            SimpleModule modDeser = new SimpleModule("custom-object-unmapper", Version.unknownVersion());
            modDeser.addDeserializer(Object.class, new CustomObjectDeserializer(classes));

            deserializeObjectMapper.registerModule(modDeser);
        }

        if (formatDate) {

            objectMapper.setDateFormat(dateFormat);
            customSerializationMapper.setDateFormat(dateFormat);
            deserializeObjectMapper.setDateFormat(dateFormat);

            deserializeObjectMapper.getDeserializationConfig().with(dateFormat);

            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            customSerializationMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        this.classesSet = classes;
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
            Class actualType = classesSet.contains(type) ? Object.class : type;
            return (T) unwrap(deserializeObjectMapper.readValue(serializedInput, actualType));
        } catch (IOException e) {
            throw new MarshallingException("Error unmarshalling input", e);
        } finally {
            stripped.set(false);
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

        @Override
        public Object findSerializationConverter(Annotated a)
        {
            Class<?> serType = _rawSerializationType(a);
            // Can apply to both container and regular type; no difference yet here
            XmlAdapter<?,?> adapter = findAdapter(a, true, serType);
            if (adapter != null) {
                return _converter(adapter, true);
            }
            return null;
        }

        private <A extends Annotation> A findAnnotation(Class<A> annotationClass, Annotated annotated, boolean includePackage, boolean includeClass, boolean includeSuperclasses) {
            Annotation annotation = annotated.getAnnotation(annotationClass);
            if(annotation != null) {
                return (A) annotation;
            } else {
                Class memberClass = null;
                if(annotated instanceof AnnotatedParameter) {
                    memberClass = ((AnnotatedParameter)annotated).getDeclaringClass();
                } else {
                    AnnotatedElement pkg = annotated.getAnnotated();
                    if(pkg instanceof Member) {
                        memberClass = ((Member)pkg).getDeclaringClass();
                        if(includeClass) {
                            annotation = memberClass.getAnnotation(annotationClass);
                            if(annotation != null) {
                                return (A) annotation;
                            }
                        }
                    } else {
                        if(!(pkg instanceof Class)) {
                            throw new IllegalStateException("Unsupported annotated member: " + annotated.getClass().getName());
                        }

                        memberClass = (Class)pkg;
                    }
                }

                if(memberClass != null) {
                    if(includeSuperclasses) {
                        for(Class pkg1 = memberClass.getSuperclass(); pkg1 != null && pkg1 != Object.class; pkg1 = pkg1.getSuperclass()) {
                            annotation = pkg1.getAnnotation(annotationClass);
                            if(annotation != null) {
                                return (A) annotation;
                            }
                        }
                    }

                    if(includePackage) {
                        Package pkg2 = memberClass.getPackage();
                        if(pkg2 != null) {
                            return memberClass.getPackage().getAnnotation(annotationClass);
                        }
                    }
                }

                return null;
            }
        }

        private XmlAdapter<Object,Object> findAdapter(Annotated am, boolean forSerialization, Class<?> type) {
            // First of all, are we looking for annotations for class?
            if (am instanceof AnnotatedClass) {
                return findAdapterForClass((AnnotatedClass) am, forSerialization);
            }
            // Otherwise for a member. First, let's figure out type of property
            XmlJavaTypeAdapter adapterInfo = findAnnotation(XmlJavaTypeAdapter.class, am, true, false, false);
            if (adapterInfo != null) {
                XmlAdapter<Object,Object> adapter = checkAdapter(adapterInfo, type, forSerialization);
                if (adapter != null) {
                    return adapter;
                }
            }
            XmlJavaTypeAdapters adapters = findAnnotation(XmlJavaTypeAdapters.class, am, true, false, false);
            if (adapters != null) {
                for (XmlJavaTypeAdapter info : adapters.value()) {
                    XmlAdapter<Object,Object> adapter = checkAdapter(info, type, forSerialization);
                    if (adapter != null) {
                        return adapter;
                    }
                }
            }
            return null;
        }

        private final XmlAdapter<Object,Object> checkAdapter(XmlJavaTypeAdapter adapterInfo, Class<?> typeNeeded, boolean forSerialization) {
            // if annotation has no type, it's applicable; if it has, must match
            Class<?> adaptedType = adapterInfo.type();

            if (adapterInfo.value().isAssignableFrom(JaxbUnknownAdapter.class)) {
                return null;
            }

            if (adaptedType == XmlJavaTypeAdapter.DEFAULT.class) {
                JavaType[] params = _typeFactory.findTypeParameters(adapterInfo.value(), XmlAdapter.class);
                adaptedType = params[1].getRawClass();
            }
            if (adaptedType.isAssignableFrom(typeNeeded)) {
                @SuppressWarnings("rawtypes")
                Class<? extends XmlAdapter> cls = adapterInfo.value();
                return ClassUtil.createInstance(cls, true);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private XmlAdapter<Object,Object> findAdapterForClass(AnnotatedClass ac, boolean forSerialization) {
            XmlJavaTypeAdapter adapterInfo = ac.getAnnotated().getAnnotation(XmlJavaTypeAdapter.class);
            if (adapterInfo != null) {
                @SuppressWarnings("rawtypes")
                Class<? extends XmlAdapter> cls = adapterInfo.value();
                return ClassUtil.createInstance(cls, true);
            }
            return null;
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

            if (value instanceof Collection) {
                String collectionJson = writeCollection((Collection) value, customObjectMapper);
                jgen.writeRawValue(collectionJson);
            } else if (value instanceof Map) {
                String mapJson = writeMap((Map) value, customObjectMapper);
                jgen.writeRawValue(mapJson);
            } else if (value instanceof Object[] || value.getClass().isArray()) {
                String arrayJson = writeArray((Object[]) value, customObjectMapper);
                jgen.writeRawValue(arrayJson);
            } else {

                String json = customObjectMapper.writeValueAsString(value);

                // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
                if (!className.startsWith("java.") && !className.startsWith("javax.") && !json.contains(className)) {
                    json = "{\"" + className + "\":" + json + "}";
                }
                jgen.writeRawValue(json);
            }
        }

        private String writeArray(Object[] value, ObjectMapper customObjectMapper) throws IOException{
            StringBuilder builder = new StringBuilder();
            builder.append("[");

            int size = Array.getLength(value);

            for (Object element : value) {
                size--;
                String elementClassName = element.getClass().getName();
                String json = customObjectMapper.writeValueAsString(element);

                // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
                if (!elementClassName.startsWith("java.") && !elementClassName.startsWith("javax.") && !json.contains(elementClassName)) {
                    json = "{\"" + elementClassName + "\":" + json + "}";
                }

                builder.append(json);

                if (size > 0) {
                    builder.append(",");
                }
            }
            builder.append("]");

            return  builder.toString();
        }

        private String writeMap(Map value, ObjectMapper customObjectMapper) throws IOException{
            StringBuilder builder = new StringBuilder();
            builder.append("{");

            int size = ((Map<?, ?>)value).size();

            for (Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()) {
                size--;
                // handle map key
                Object key = entry.getKey();
                String keyClassName = key.getClass().getName();
                String json = customObjectMapper.writeValueAsString(key);

                // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
                if (!keyClassName.startsWith("java.") && !keyClassName.startsWith("javax.") && !json.contains(keyClassName)) {
                    json = "{\"" + keyClassName + "\":" + json + "}";
                }
                // handle map value
                Object mValue = entry.getValue();
                String mValueClassName = mValue.getClass().getName();
                String jsonValue = customObjectMapper.writeValueAsString(mValue);

                // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
                if (!mValueClassName.startsWith("java.") && !mValueClassName.startsWith("javax.") && !json.contains(mValueClassName)) {
                    jsonValue = "{\"" + mValueClassName + "\":" + jsonValue + "}";
                }

                // add as JSON map
                builder.append(json);
                builder.append(" : ");
                builder.append(jsonValue);

                if (size > 0) {
                    builder.append(",");
                }
            }

            builder.append("}");
            return builder.toString();
        }

        private String writeCollection(Collection collection, ObjectMapper customObjectMapper) throws IOException {

            StringBuilder builder = new StringBuilder();
            builder.append("[");

            int size = collection.size();
            Iterator it = collection.iterator();
            while (it.hasNext()) {
                size--;
                Object element = it.next();
                String elementClassName = element.getClass().getName();
                String json = customObjectMapper.writeValueAsString(element);

                // don't wrap java and javax classes as they are always available, in addition avoid double wrapping
                if (!elementClassName.startsWith("java.") && !elementClassName.startsWith("javax.") && !json.contains(elementClassName)) {
                    json = "{\"" + elementClassName + "\":" + json + "}";
                }

                builder.append(json);

                if (size > 0) {
                    builder.append(",");
                }
            }
            builder.append("]");

            return  builder.toString();
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
                stripped.set(true);
                Object value = deserializeObjectMapper.readValue(jp, classes.get(field1));
                jp.nextToken();

                return value;
            } else {
                if (isFullyQualifiedClassname(field1)) {
                    try {
                        Object value = deserializeObjectMapper.readValue(jp, classLoader.loadClass(field1));
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

    class CustomAsWrapperTypeDeserializer extends AsWrapperTypeDeserializer {

        public CustomAsWrapperTypeDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, Class<?> defaultImpl) {
            super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        protected CustomAsWrapperTypeDeserializer(AsWrapperTypeDeserializer src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(_baseType.getRawClass().getClassLoader());
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                Object value = deser.deserialize(jp, ctxt);
                return value;
            } finally {
                Thread.currentThread().setContextClassLoader(current);
            }
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(_baseType.getRawClass().getClassLoader());
                if (classesSet.contains(_baseType.getRawClass()) && !stripped.get()) {

                    try {
                        return super.deserializeTypedFromObject(jp, ctxt);
                    } catch (Exception e) {
                        JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                        Object value = deser.deserialize(jp, ctxt);
                        return value;
                    }
                }
                stripped.set(false);
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                Object value = deser.deserialize(jp, ctxt);
                return value;
            } finally {
                Thread.currentThread().setContextClassLoader(current);
            }

        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(_baseType.getRawClass().getClassLoader());
                if (classesSet.contains(_baseType.getRawClass())) {
                    return super.deserializeTypedFromScalar(jp, ctxt);
                }
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                Object value = deser.deserialize(jp, ctxt);
                return value;
            } finally {
                Thread.currentThread().setContextClassLoader(current);
            }
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(_baseType.getRawClass().getClassLoader());
            if (classesSet.contains(_baseType.getRawClass())) {
                try {
                    return super.deserializeTypedFromAny(jp, ctxt);
                } catch (Exception e) {
                    JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                    Object value = deser.deserialize(jp, ctxt);
                    return value;
                }
            }

            if (_baseType.isMapLikeType() && jp.getCurrentToken() == JsonToken.START_ARRAY) {

                LinkedHashMap<Object, Object> data = new LinkedHashMap<Object, Object>();
                jp.nextToken();

                if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
                    return data;
                }
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, LinkedHashMap.class.getName());
                Map<Object, Object> value = (Map) deser.deserialize(jp, ctxt);
                jp.nextToken();

                if (value != null) {
                    Collection<Object> values = value.values();
                    if (values.size() == 2) {

                        Iterator<Object> it = values.iterator();
                        data.put(it.next(), it.next());

                        return data;
                    }
                }
                return value;

            } else {
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, baseTypeName());
                Object value = deser.deserialize(jp, ctxt);

                return value;
            }
            } finally {
                Thread.currentThread().setContextClassLoader(current);
            }
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            if (prop != null) {
                if (useForType(prop.getType()) || useForType(prop.getType().getContentType()) ) {
                    return new CustomAsWrapperTypeDeserializer(this, prop);
                }
            }
            return super.forProperty(prop);
        }

        boolean useForType(JavaType t) {
            if (classesSet.contains(t.getRawClass())) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
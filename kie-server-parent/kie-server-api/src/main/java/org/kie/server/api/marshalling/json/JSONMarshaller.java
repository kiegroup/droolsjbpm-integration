package org.kie.server.api.marshalling.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;

public class JSONMarshaller implements Marshaller {

    private final ClassLoader classLoader;
    private final ObjectMapper objectMapper;

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
    public <T> T unmarshall(String input, String type) {
        try {
            Class<?> clazz = Class.forName(type, true, this.classLoader);

            return (T) unmarshall(input, clazz);
        } catch (Exception e) {
            throw new MarshallingException("Error unmarshalling input", e);
        }
    }

    @Override
    public void dispose() {

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
}

package org.kie.server.services;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.ws.rs.core.Application;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.kie.server.services.rest.KieServerRestImpl;

public class KieServerApplication extends Application {

    private final Set<Object> instances = new CopyOnWriteArraySet<Object>() {
        private static final long serialVersionUID = 1763183096852523317L;
        {
            add(new KieServerRestImpl());
            // Register the Jackson provider for JSON
            // Make (de)serializer use a subset of JAXB and (afterwards) Jackson annotations
            // See http://wiki.fasterxml.com/JacksonJAXBAnnotations for more information
            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
            AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
            AnnotationIntrospector introspectorPair = new AnnotationIntrospector.Pair(primary, secondary);
            mapper.setDeserializationConfig(mapper.getDeserializationConfig().withAnnotationIntrospector(introspectorPair));
            mapper.setSerializationConfig(mapper.getSerializationConfig().withAnnotationIntrospector(introspectorPair));

            JacksonJaxbJsonProvider jaxbProvider = new JacksonJaxbJsonProvider();
            jaxbProvider.setMapper(mapper);
            add(jaxbProvider);
        }
    };

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getSingletons() {
        return instances;
    }

}

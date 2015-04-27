package org.kie.server.remote.rest.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;

@ApplicationPath("/")
public class KieServerApplication extends Application {

    private final Set<Object> instances = new CopyOnWriteArraySet<Object>() {
        private static final long serialVersionUID = 1763183096852523317L;
        {
            KieServerImpl server = KieServerLocator.getInstance();

            add(new KieServerRestImpl(server));

            // next add any resources from server extensions
            List<KieServerExtension> extensions = server.getServerExtensions();

            for (KieServerExtension extension : extensions) {
                addAll(extension.getAppComponents(SupportedTransports.REST));
            }

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

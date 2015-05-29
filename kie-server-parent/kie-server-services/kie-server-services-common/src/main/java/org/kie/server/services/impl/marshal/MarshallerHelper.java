package org.kie.server.services.impl.marshal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;

public class MarshallerHelper {

    private KieServerRegistry registry;

    private Map<MarshallingFormat, Marshaller> serverMarshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>();

    public MarshallerHelper(KieServerRegistry registry) {
        this.registry = registry;
    }

    public String marshal(String containerId, String marshallingFormat, Object entity) {
        MarshallingFormat format = getFormat(marshallingFormat);

        KieContainerInstance containerInstance = registry.getContainer(containerId);

        if (containerInstance == null || format == null) {
            throw new IllegalArgumentException("No container found for id " + containerId + " or unknown marshalling format " + marshallingFormat);
        }

        Marshaller marshaller = containerInstance.getMarshaller(format);
        if (marshaller == null) {
            throw new IllegalArgumentException("No marshaller found for format " + format);
        }

        return marshaller.marshall(entity);

    }

    public String marshal(String marshallingFormat, Object entity) {
        MarshallingFormat format = getFormat(marshallingFormat);


        if (format == null) {
            throw new IllegalArgumentException("Unknown marshalling format " + marshallingFormat);
        }

        Marshaller marshaller = serverMarshallers.get(format);
        if (marshaller == null) {
            marshaller = MarshallerFactory.getMarshaller(format, this.getClass().getClassLoader());
            serverMarshallers.put(format, marshaller);
        }

        return marshaller.marshall(entity);

    }

    public <T> T unmarshal(String containerId, String data, String marshallingFormat, Class<T> unmarshalType) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        MarshallingFormat format = getFormat(marshallingFormat);

        KieContainerInstance containerInstance = registry.getContainer(containerId);

        if (containerInstance == null || format == null) {
            throw new IllegalArgumentException("No container found for id " + containerId + " or unknown marshalling format " + marshallingFormat);
        }

        Marshaller marshaller = containerInstance.getMarshaller(format);
        if (marshaller == null) {
            throw new IllegalArgumentException("No marshaller found for format " + format);
        }

        Object instance = marshaller.unmarshall(data, unmarshalType);

        if (instance instanceof Wrapped) {
            return (T) ((Wrapped) instance).unwrap();
        }

        return (T) instance;
    }

    public <T> T unmarshal(String data, String marshallingFormat, Class<T> unmarshalType) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        MarshallingFormat format = getFormat(marshallingFormat);

        Marshaller marshaller = serverMarshallers.get(format);
        if (marshaller == null) {
            marshaller = MarshallerFactory.getMarshaller(format, this.getClass().getClassLoader());
            serverMarshallers.put(format, marshaller);
        }

        Object instance = marshaller.unmarshall(data, unmarshalType);

        if (instance instanceof Wrapped) {
            return (T) ((Wrapped) instance).unwrap();
        }

        return (T) instance;
    }

    public static MarshallingFormat getFormat(String descriptor) {
        MarshallingFormat format = MarshallingFormat.fromType(descriptor);
        if (format == null) {
            format = MarshallingFormat.valueOf(descriptor);
        }

        return format;
    }
}

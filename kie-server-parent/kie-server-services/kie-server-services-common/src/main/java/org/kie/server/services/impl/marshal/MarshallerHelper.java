package org.kie.server.services.impl.marshal;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;

public class MarshallerHelper {

    private KieServerRegistry registry;

    public MarshallerHelper(KieServerRegistry registry) {
        this.registry = registry;
    }

    public String marshal(String containerId, String marshallingFormat, Object entity) {
        MarshallingFormat format = MarshallingFormat.fromType(marshallingFormat);
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

    public <T> T unmarshal(String containerId, String data, String marshallingFormat, String unmarshalType, Class<T> returnType) {

        KieContainerInstance containerInstance = registry.getContainer(containerId);
        Class<?> actualUnfarshalType = null;
        try {
            actualUnfarshalType = Class.forName(unmarshalType, true, containerInstance.getKieContainer().getClassLoader());
            return unmarshal(containerId, data, marshallingFormat, actualUnfarshalType, returnType);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public <T> T unmarshal(String containerId, String data, String marshallingFormat, Class<?> unmarshalType, Class<T> returnType) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        MarshallingFormat format = MarshallingFormat.fromType(marshallingFormat);
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
}

package org.kie.server.client;

import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class JaxbSerializationProvider implements SerializationProvider {
    public static final Class<?>[] KIE_SERVER_JAXB_CLASSES;

    static {
        KIE_SERVER_JAXB_CLASSES = new Class<?>[]{
                CallContainerCommand.class,
                CommandScript.class,
                CreateContainerCommand.class,
                DisposeContainerCommand.class,
                ListContainersCommand.class,

                KieContainerResource.class,
                KieContainerResourceList.class,
                KieContainerStatus.class,
                KieServerInfo.class,
                ReleaseId.class,
                ServiceResponse.class,
                ServiceResponsesList.class
        };
    }

    private final JAXBContext jaxbContext;

    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;


    public JaxbSerializationProvider() {
        try {
            this.jaxbContext = JAXBContext.newInstance(KIE_SERVER_JAXB_CLASSES);
            this.marshaller = jaxbContext.createMarshaller();
            this.unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new SerializationException("Error while create Jaxb context from default jaxb classes!", e);
        }
    }

    @Override
    public String serialize(Object obj) {
        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(obj, writer);
        } catch (JAXBException e) {
            throw new SerializationException("Can't serialize provided object!", e);
        }
        return writer.toString();
    }

    @Override
    public Object deserialize(String str) {
        try {
            return unmarshaller.unmarshal(new StringReader(str));
        } catch (JAXBException e) {
            throw new SerializationException("Can't deserialize provided string!", e);
        }
    }

    @Override
    public <T> T deserialize(String str, Class<T> type) {
        return (T) deserialize(str);
    }

}

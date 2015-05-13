package org.kie.server.api.marshalling.jaxb;

import org.kie.server.api.commands.*;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.model.*;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JaxbMarshaller implements Marshaller {
    public static final Class<?>[] KIE_SERVER_JAXB_CLASSES;

    static {
        KIE_SERVER_JAXB_CLASSES = new Class<?>[]{
                CallContainerCommand.class,
                CommandScript.class,
                CreateContainerCommand.class,
                DisposeContainerCommand.class,
                ListContainersCommand.class,
                GetContainerInfoCommand.class,
                GetScannerInfoCommand.class,
                GetServerInfoCommand.class,
                UpdateScannerCommand.class,
                UpdateReleaseIdCommand.class,

                KieContainerResource.class,
                KieContainerResourceList.class,
                KieContainerStatus.class,
                KieServerInfo.class,
                ReleaseId.class,
                ServiceResponse.class,
                ServiceResponsesList.class,

                KieServerConfig.class,
                KieServerConfigItem.class,

                JaxbList.class,
                JaxbMap.class,

                ProcessInstance.class
        };
    }

    private final JAXBContext jaxbContext;

    private final ClassLoader classLoader;

    private final javax.xml.bind.Marshaller marshaller;
    private final Unmarshaller              unmarshaller;

    public JaxbMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        this.classLoader = classLoader;
        try {
            Set<Class<?>> allClasses = new HashSet<Class<?>>();

            allClasses.addAll(Arrays.asList(KIE_SERVER_JAXB_CLASSES));
            if (classes != null) {
                allClasses.addAll(classes);
            }

            this.jaxbContext = JAXBContext.newInstance( allClasses.toArray(new Class[allClasses.size()]) );
            this.marshaller = jaxbContext.createMarshaller();
            this.unmarshaller = jaxbContext.createUnmarshaller();
        } catch ( JAXBException e ) {
            throw new MarshallingException( "Error while creating JAXB context from default classes!", e );
        }
    }

    @Override
    public String marshall(Object input) {
        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal( input, writer );
        } catch ( JAXBException e ) {
            throw new MarshallingException( "Can't marshall input object: "+input, e );
        }
        return writer.toString();
    }

    @Override
    public <T> T unmarshall(String input, Class<T> type) {
        try {
            return (T) unmarshaller.unmarshal( new StringReader( input ) );
        } catch ( JAXBException e ) {
            throw new MarshallingException( "Can't unmarshall input string: "+input, e );
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
}

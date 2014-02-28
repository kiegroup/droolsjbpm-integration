package org.kie.services.remote.rest.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DynamicJAXBContext extends JAXBContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJAXBContext.class);

    private Set<Class<?>> types = new CopyOnWriteArraySet<Class<?>>();
    private ConcurrentHashMap<String, JAXBContext> contexts = new ConcurrentHashMap<String, JAXBContext>();

    @Inject
    private DeploymentInfoBean deploymentClassNameBean;

    private UriInfo uriInfo;


    public DynamicJAXBContext() {
        types.add(JaxbCommandsRequest.class);
        types.add(JaxbCommandsResponse.class);
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        JAXBContext context = getContext();
        if (context != null) {
            return context.createUnmarshaller();
        }
        return null;
    }

    @Override
    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = getContext();
        if (context != null) {
            return context.createMarshaller();
        }
        return null;
    }

    @Override
    public Validator createValidator() throws JAXBException {
        JAXBContext context = getContext();
        if (context != null) {
            return context.createValidator();
        }
        return null;
    }

    public void addType(Class<?> type) {
        this.types.add(type);
    }

    public JAXBContext getContext() {

        String deploymentId = getDeploymentId();

        if (deploymentId == null) {
            logger.error("Unable to find deployment id which results in returning empty JAXBContext");
            try {
                return JAXBContext.newInstance();
            } catch (JAXBException e) {
                logger.error( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", e);
                return null;
            }
        }

        synchronized (contexts) {
            // if there is already created context return it
            if (contexts.containsKey(deploymentId)) {
                return contexts.get(deploymentId);
            }

            Set<Class<?>> classesForSerialization = new HashSet<Class<?>>();
            classesForSerialization.addAll(JaxbSerializationProvider.PRIMITIVE_ARRAY_CLASS_SET);

            if( deploymentId != null ) {
                // retrieve class list from kjar
                Collection<Class<?>> deploymentClassNames = deploymentClassNameBean.getDeploymentClasses(deploymentId);
                for( Class<?> clazz : deploymentClassNames ) {
                    logger.debug( "Adding {} to JAXBContext instance.", clazz.getName() );
                }
                classesForSerialization.addAll(deploymentClassNames);
            }

            // Add given types to list
            classesForSerialization.addAll(types);

            // Create JAXBContext instance and return it.
            Class<?> [] types = classesForSerialization.toArray(new Class[classesForSerialization.size()]);

            try {
                JAXBContext context = JAXBContext.newInstance(types);
                contexts.put(deploymentId, context);

                return context;
            } catch (JAXBException jaxbe) {
                logger.error( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", jaxbe);
            }
        }

        return null;
    }

    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        contexts.remove(event.getDeploymentId());
    }

    protected String getDeploymentId() {
        String deploymentId = null;
        List<String> deploymentIdParams =  uriInfo.getPathParameters().get("deploymentId");
        if( deploymentIdParams != null && ! deploymentIdParams.isEmpty() ) {
            deploymentId = deploymentIdParams.get(0);
        } else {
            deploymentIdParams = uriInfo.getQueryParameters().get("deploymentId");
            if( deploymentIdParams != null && ! deploymentIdParams.isEmpty() ) {
                deploymentId = deploymentIdParams.get(0);
            }
        }

        return deploymentId;
    }


    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

}

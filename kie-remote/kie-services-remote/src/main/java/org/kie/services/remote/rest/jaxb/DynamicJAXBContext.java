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
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummaryListResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;
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
        types.add(JaxbContentResponse.class);
        types.add(JaxbTaskResponse.class);
        types.add(JaxbTaskSummaryListResponse.class);
        types.add(JaxbProcessInstanceListResponse.class);
        types.add(JaxbProcessInstanceResponse.class);
        types.add(JaxbProcessInstanceWithVariablesResponse.class);
        types.add(JaxbWorkItem.class);
        types.add(JaxbDeploymentJobResult.class);
        types.add(JaxbDeploymentUnit.class);
        types.add(JaxbDeploymentUnitList.class);
        types.add(JaxbHistoryLogList.class);
        types.add(JaxbNodeInstanceLog.class);
        types.add(JaxbProcessInstanceLog.class);
        types.add(JaxbVariableInstanceLog.class);
        types.add(JaxbLongListResponse.class);

        types.add(JaxbOtherResponse.class);
        types.add(JaxbPrimitiveResponse.class);
        types.add(JaxbVariablesResponse.class);
        types.add(JaxbExceptionResponse.class);
        types.add(JaxbGenericResponse.class);
        types.add(JaxbRequestStatus.class);

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
        if (!this.types.contains(type)) {
            this.types.add(type);
            // clear the already known context to ensure all the types will be available for all deployments
            this.contexts.clear();
        }
    }

    public JAXBContext getContext() {
        Set<Class<?>> classesForSerialization = new HashSet<Class<?>>();
        classesForSerialization.addAll(JaxbSerializationProvider.PRIMITIVE_ARRAY_CLASS_SET);
        // Add given types to list
        classesForSerialization.addAll(types);

        String deploymentId = getDeploymentId();

        if (deploymentId == null) {
            logger.debug("Unable to find deployment id which results in returning default JAXBContext");
            try {
                Class<?> [] types = classesForSerialization.toArray(new Class[classesForSerialization.size()]);
                return JAXBContext.newInstance(types);
            } catch (JAXBException e) {
                throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", e);

            }
        }

        synchronized (contexts) {
            // if there is already created context return it
            if (contexts.containsKey(deploymentId)) {
                return contexts.get(deploymentId);
            }



            if( deploymentId != null ) {
                // retrieve class list from kjar
                Collection<Class<?>> deploymentClassNames = deploymentClassNameBean.getDeploymentClasses(deploymentId);
                for( Class<?> clazz : deploymentClassNames ) {
                    logger.debug( "Adding {} to JAXBContext instance.", clazz.getName() );
                }
                classesForSerialization.addAll(deploymentClassNames);
            }



            // Create JAXBContext instance and return it.
            Class<?> [] types = classesForSerialization.toArray(new Class[classesForSerialization.size()]);

            try {
                JAXBContext context = JAXBContext.newInstance(types);
                contexts.put(deploymentId, context);

                return context;
            } catch (JAXBException jaxbe) {
                throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", jaxbe);
            }
        }
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

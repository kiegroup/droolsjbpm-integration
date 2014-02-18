package org.kie.services.remote.rest.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.kie.api.runtime.KieSession;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We use this class in order to use (user) classes from Kjar deployments in serialization. 
 * </p>
 * Users may send inputs to the REST API that contain instances of these classes as parameters to {@link KieSession} operations.
 */
@Provider
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbContextResolver.class);
    
    @Inject
    private DeploymentInfoBean deploymentClassNameBean;
    
    @Context
    private UriInfo uriInfo;

    @Override
    public JAXBContext getContext(Class<?> type) {
        logger.debug( "Resolving JAXBContext for " + type.getName() + " instance in input.");
        String deploymentId = null;
        List<String> deploymentIdParams =  uriInfo.getPathParameters().get("deploymentId");
        if( deploymentIdParams != null && ! deploymentIdParams.isEmpty() ) {
            deploymentId = deploymentIdParams.get(0);
        } else { 
            deploymentIdParams = uriInfo.getQueryParameters().get("deploymentId");
        }
        if( deploymentIdParams != null && ! deploymentIdParams.isEmpty() ) {
            deploymentId = deploymentIdParams.get(0);
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
        
        // Add given type to list
        classesForSerialization.add(type);

        // Create JAXBContext instance and return it. 
        Class<?> [] types = classesForSerialization.toArray(new Class[classesForSerialization.size()]);
        
        try {
            return JAXBContext.newInstance(types);
        } catch (JAXBException jaxbe) {
            logger.error( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", jaxbe);
        }
        
        return null;
    }

}

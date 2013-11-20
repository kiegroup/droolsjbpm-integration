package org.kie.services.remote.rest;

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
import org.kie.services.remote.cdi.DeploymentClassNamesBean;
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
    private DeploymentClassNamesBean deploymentClassNameBean;
    
    @Context
    private UriInfo uriInfo;

    private Collection<Class<?>> otherClasses = new HashSet<Class<?>>();

    public JaxbContextResolver() { 
       otherClasses.add(new Boolean[]{}.getClass()); 
       otherClasses.add(new Byte[]{}.getClass()); 
       otherClasses.add(new Character[]{}.getClass()); 
       otherClasses.add(new Double[]{}.getClass()); 
       otherClasses.add(new Float[]{}.getClass()); 
       otherClasses.add(new Integer[]{}.getClass()); 
       otherClasses.add(new Long[]{}.getClass()); 
       otherClasses.add(new Math[]{}.getClass()); 
       otherClasses.add(new Number[]{}.getClass()); 
       otherClasses.add(new Short[]{}.getClass()); 
       otherClasses.add(new String[]{}.getClass()); 
    }
    
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
        classesForSerialization.addAll(otherClasses);
        
        if( deploymentId != null ) { 
            // retrieve class list from kjar
            Collection<String> deploymentClassNames = deploymentClassNameBean.getClassNames(deploymentId);

            for( String className : deploymentClassNames ) { 
                try {
                    Class<?> classInKjar = Class.forName(className);
                    logger.debug("Added '" + className + "' to the classes used by the JAXBContext instance" );
                    classesForSerialization.add(classInKjar);
                } catch (ClassNotFoundException cnfe) {
                    logger.error("Unable to load '" + className + "': " + cnfe.getMessage(), cnfe);
                }
            }
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

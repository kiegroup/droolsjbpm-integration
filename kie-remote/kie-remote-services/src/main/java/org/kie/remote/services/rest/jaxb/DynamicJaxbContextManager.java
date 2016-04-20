package org.kie.remote.services.rest.jaxb;


import static org.kie.remote.services.rest.jaxb.DynamicJaxbContext.contextsCache;
import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.cdi.Deploy;
import org.jbpm.services.cdi.Undeploy;
import org.kie.remote.services.exception.KieRemoteServicesDeploymentException;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;
import com.sun.xml.bind.v2.runtime.Location;

@ApplicationScoped
public class DynamicJaxbContextManager {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContextManager.class);


    private final static boolean onWebsphere;
    private final static String WEBSPHERE_JAXB_CACHING_CLASS_NAME
        = "org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBProvider";

    static {
        // websphere stuff
        boolean classFound = false;
        try {
            Class.forName(WEBSPHERE_JAXB_CACHING_CLASS_NAME);
            classFound = true;
        } catch (ClassNotFoundException e) {
            // no-op
        }
        onWebsphere = classFound;
    }

    // LOGGING IF MULTIPLE INSTANCES ARE CREATED ----------------------------------------------------------------------------------

    private static AtomicInteger instanceCreated = new AtomicInteger(0);

    public DynamicJaxbContextManager() {
        if( ! instanceCreated.compareAndSet(0, 1) ) {
            logger.error("Instance {} of the {} created!", instanceCreated.incrementAndGet(), DynamicJaxbContext.class.getSimpleName() );
        }
    }

    static Map<String, Collection<Class<?>>> deploymentClassesMap = new ConcurrentHashMap<String, Collection<Class<?>>>();

    private final DynamicJaxbContext _jaxbContextInstance = new DynamicJaxbContext();

    // Deployment jaxbContext management and creation logic -----------------------------------------------------------------------

    /**
     * Called when the workbench/console/business-central deploys a new deployment.
     * @param event
     */
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        String deploymentId = event.getDeploymentId();
        deploymentClassesMap.put(deploymentId, event.getDeployedUnit().getDeployedClasses());
        DeploymentUnit deploymentUnit = event.getDeployedUnit().getDeploymentUnit();
        ClassLoader projectClassLoader = ((KModuleDeploymentUnit) deploymentUnit).getKieContainer().getClassLoader();

        setupDeploymentJaxbContext(deploymentId, projectClassLoader);
    }

    /**
     * Called when the workbench/console/business-central *un*deploys (removes) a deployment.
     * </p>
     * Removes the cached {@link JAXBContext} and deployment lock object
     * </p>
     * It's <b>VERY</b> important that the cached {@link JAXBContext} instance be removed! The new deployment
     * may contain a <b>different</b> version of a class with the same name. Keeping the old class definition
     * will cause problems!
     * @param event The {@link DeploymentEvent} fired on an undeploy of a deployment
     */
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        String deploymentId = event.getDeploymentId();
        deploymentClassesMap.remove(deploymentId);
        JAXBContext deploymentJaxbContext = contextsCache.remove(deploymentId);
        if( deploymentJaxbContext == null ) {
            logger.error("JAXB context instance could not be found when undeploying deployment '" + deploymentId + "'!");
        }
    }

    /**
     * Creates the {@link JAXBContext} instance for the given deployment at deployment time
     * </p>
     * Package scope to allow testing
     * @param deploymentId The deployment identifier.
     */
    void setupDeploymentJaxbContext(String deploymentId, ClassLoader projectClassLoader) {
        if( contextsCache.containsKey(deploymentId) ) {
            logger.error("JAXB context instance already found when deploying deployment '" + deploymentId + "'!");
            contextsCache.remove(deploymentId);
        }

        // retrieve deployment classes
        Collection<Class<?>> depClasses = deploymentClassesMap.get(deploymentId);

        // We are sharing the default jaxb context among different requests here:
        // while we have no guarantees that JAXBContext instances are thread-safe,
        // the REST framework using the JAXBContext instance is responsible for that thread-safety
        // since it is caching the JAXBContext in any case..
        if( depClasses == null || depClasses.size() == 0 ) {
            JAXBContext defaultJaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
            contextsCache.put(deploymentId, defaultJaxbContext);
            return;
        }

        // create set of all classes needed
        List<Class<?>> allClassList = Arrays.asList(ServerJaxbSerializationProvider.getAllBaseJaxbClasses());
        Set<Class<?>> allClasses = new HashSet<Class<?>>(allClassList);
        allClasses.addAll(depClasses);
        Class [] allClassesArr = allClasses.toArray(new Class[allClasses.size()]);

        // create and cache jaxb context
        JAXBContext jaxbContext = null;
        try {
            if( smartJaxbContextInitialization ) {
                jaxbContext = smartJaxbContextInitialization(allClassesArr, deploymentId, projectClassLoader);
            } else {
                jaxbContext = JAXBContext.newInstance(allClassesArr);
            }
            contextsCache.put(deploymentId, jaxbContext);
        } catch( JAXBException jaxbe ) {
            String errMsg = "Unable to instantiate JAXBContext for deployment '" + deploymentId + "'.";
            throw new KieRemoteServicesDeploymentException( errMsg, jaxbe );
            // This is a serious problem if it goes wrong here.
        }
    }

    // SMART JAXB CONTEXT INITIALIZATION ------------------------------------------------------------------------------------------

    private final static String SMART_JAXB_CONTEXT_INIT_PROPERTY_NAME = "org.kie.remote.jaxb.smart.init";
    private final static boolean smartJaxbContextInitialization;
    private final static String EXPECTED_JAXB_CONTEXT_IMPL_CLASS = "com.sun.xml.bind.v2.runtime.JAXBContextImpl";

    // only use smart initialization if we're using the (default) JAXB RI
    static {
         boolean smartJaxbContextInitProperty = Boolean.parseBoolean(System.getProperty(SMART_JAXB_CONTEXT_INIT_PROPERTY_NAME, "true"));
         if( smartJaxbContextInitProperty ) {
            try {
                smartJaxbContextInitProperty = false;
                smartJaxbContextInitProperty = EXPECTED_JAXB_CONTEXT_IMPL_CLASS.equals(JAXBContext.newInstance(new Class[0]).getClass().getName());
            } catch( JAXBException jaxbe ) {
                logger.error("Unable to initialize empty JAXB Context: something is VERY wrong!", jaxbe);
            }
         }
         smartJaxbContextInitialization = smartJaxbContextInitProperty;
    }

    private JAXBContext smartJaxbContextInitialization(Class [] jaxbContextClasses, String deploymentId, ClassLoader projectClassLoader) throws JAXBException {

        List<Class> classList = new ArrayList<Class>(Arrays.asList(jaxbContextClasses));

        JAXBContext jaxbContext = null;
        boolean retryJaxbContextCreation = true;
        while( retryJaxbContextCreation ) {
            try {
                jaxbContext = JAXBContext.newInstance(classList.toArray(new Class[classList.size()]));
                retryJaxbContextCreation = false;
            } catch( IllegalAnnotationsException iae ) {
                // throws any exception it can not process
                removeClassFromJaxbContextClassList(classList, iae, deploymentId, projectClassLoader);
            }
        }

        return jaxbContext;
    }

    private void removeClassFromJaxbContextClassList( List<Class> classList, IllegalAnnotationsException iae, String deploymentId, ClassLoader projectClassLoader)
        throws IllegalAnnotationException {

        Set<Class> removedClasses = new HashSet<Class>();
        for( IllegalAnnotationException error : iae.getErrors() ) {
            List<Location> classLocs = error.getSourcePos().get(0);

            if( classLocs != null && ! classLocs.isEmpty() ) {
               String className = classLocs.listIterator(classLocs.size()).previous().toString();
               Class removeClass = null;
               try {
                   removeClass = Class.forName(className, true, projectClassLoader);
                   if( ! removedClasses.add(removeClass) ) {
                       // we've already determined that this class was bad
                       continue;
                   }
               } catch( ClassNotFoundException cnfe ) {
                   // this should not be possible, after the class object instance has already been found
                   //  and added to the list of classes needed for the JAXB context
                   throw new KieRemoteServicesInternalError("Class [" + className + "] could not be found when creating JAXB context: "  + cnfe.getMessage(), cnfe);
               }
               if( classList.remove(removeClass) ) {
                   logger.warn("Removing class '{}' from serialization context for deployment '{}'", className, deploymentId);
                   // next error
                   continue;
               }
            }
            // We could not figure out which class caused this error (this error is wrapped later)
            throw error;
        }
    }

    public JAXBContext getJaxbContext() {
        if (onWebsphere ) {
            return _jaxbContextInstance.getRequestContext();
        } else {
            return _jaxbContextInstance;
        }
    }
}

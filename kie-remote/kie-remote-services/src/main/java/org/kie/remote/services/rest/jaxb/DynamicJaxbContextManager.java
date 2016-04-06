package org.kie.remote.services.rest.jaxb;

import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID;
import static org.kie.remote.services.rest.jaxb.DynamicJaxbContext.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.cdi.Deploy;
import org.jbpm.services.cdi.Undeploy;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.DeploymentProcessedEvent;
import org.kie.remote.services.exception.KieRemoteServicesDeploymentException;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * See the {@link DynamicJaxbContext} javadoc!!
 */
@ApplicationScoped
public class DynamicJaxbContextManager {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContextManager.class);

    @Inject
    // pkg level for tests
    DeploymentInfoBean deploymentInfoBean;

    // Singleton instance in order make sure that REST framework caching is efficient
    private final DynamicJaxbContext _jaxbContextInstance = new DynamicJaxbContext();

    // SMART JAXB CREATION --------------------------------------------------------------------------------------------------------

    // properties for smart JAXB creation
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

    // LOGGING IF MULTIPLE INSTANCES ARE CREATED ----------------------------------------------------------------------------------

    private static AtomicInteger instanceCreated = new AtomicInteger(0);

    public DynamicJaxbContextManager() {
        if( ! instanceCreated.compareAndSet(0, 1) ) {
            logger.info("Instance {} of the (singleton) {} created!", instanceCreated.incrementAndGet(), DynamicJaxbContextManager.class.getSimpleName() );
        }
    }

    // CDI methods: deploy and undeploy actions -----------------------------------------------------------------------------------

    /**
     * Adds a deployment lock object.
     * @param event The {@link DeploymentEvent} fired on deployment
     */
    public void setupDeploymentJaxbContext(@Observes @Deploy DeploymentProcessedEvent event) {
        String deploymentId = event.getDeploymentId();
        setupDeploymentJaxbContext(deploymentId);
    }

    /**
     * Removes the cached {@link JAXBContext} and deployment lock object
     * </p>
     * It's <b>VERY</b> important that the cached {@link JAXBContext} instance be removed! The new deployment
     * may contain a <b>different</b> version of a class with the same name. Keeping the old class definition
     * will cause problems!
     * @param event The {@link DeploymentEvent} fired on an undeploy of a deployment
     */
    public void cleanUpOnUndeploy(@Observes @Undeploy DeploymentProcessedEvent event) {
        String deploymentId = event.getDeploymentId();
        JAXBContext deploymentJaxbContext = contextsCache.remove(deploymentId);
        if( deploymentJaxbContext == null ) {
            logger.error("JAXB context instance could not be found when undeploying deployment '" + deploymentId + "'!");
        }
    }

    /**
     * Creates the {@link JAXBContext} instance for the given deployment at deployment time
     * @param deploymentId The deployment identifier.
     */
    private void setupDeploymentJaxbContext(String deploymentId) {
        if( contextsCache.containsKey(deploymentId) ) {
            logger.error("JAXB context instance already found when deploying deployment '" + deploymentId + "'!");
            contextsCache.remove(deploymentId);
        }

        // retrieve deployment classes
        Collection<Class<?>> depClasses = deploymentInfoBean.getDeploymentClasses(deploymentId);

        // We are sharing the default jaxb context among different requests here:
        // while we have no guarantees that JAXBContext instances are thread-safe,
        // the REST framework using the JAXBContext instance is responsible for that thread-safety
        // since it is caching the JAXBContext in any case..
        if( depClasses.size() == 0 ) {
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
                jaxbContext = smartJaxbContextInitialization(allClassesArr, deploymentId);
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


    private JAXBContext smartJaxbContextInitialization(Class [] jaxbContextClasses, String deploymentId) throws JAXBException {

        List<Class> classList = new ArrayList<Class>(Arrays.asList(jaxbContextClasses));

        JAXBContext jaxbContext = null;
        boolean retryJaxbContextCreation = true;
        while( retryJaxbContextCreation ) {
            try {
                jaxbContext = JAXBContext.newInstance(classList.toArray(new Class[classList.size()]));
                retryJaxbContextCreation = false;
            } catch( IllegalAnnotationsException iae ) {
                // throws any exception it can not process
                removeClassFromJaxbContextClassList(classList, iae, deploymentId);
            }
        }

        return jaxbContext;
    }


    private void removeClassFromJaxbContextClassList( List<Class> classList, IllegalAnnotationsException iae, String deploymentId)
        throws IllegalAnnotationException {

        Set<Class> removedClasses = new HashSet<Class>();
        for( IllegalAnnotationException error : iae.getErrors() ) {
            List<Location> classLocs = error.getSourcePos().get(0);

            if( classLocs != null && ! classLocs.isEmpty() ) {
               String className = classLocs.listIterator(classLocs.size()).previous().toString();
               Class removeClass = null;
               try {
                   removeClass = Class.forName(className);
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
        return this._jaxbContextInstance;
    }
}

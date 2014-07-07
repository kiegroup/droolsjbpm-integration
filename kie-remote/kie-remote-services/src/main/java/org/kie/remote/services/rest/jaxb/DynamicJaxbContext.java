package org.kie.remote.services.rest.jaxb;

import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID;
import static org.kie.services.client.serialization.JaxbSerializationProvider.KIE_JAXB_CLASS_SET;
import static org.kie.services.client.serialization.JaxbSerializationProvider.PRIMITIVE_ARRAY_CLASS_SET;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.jbpm.services.cdi.Deploy;
import org.jbpm.services.cdi.Undeploy;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.DeploymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>READ THIS BEFORE WORKING ON THIS CLASS!</h3>
 * </p> 
 * There are a couple of hidden issues that this class deals this: 
 * <p><ol> 
 * <li> {@link JAXBContext} instances are cached by the REST framework. This means that we 
 * can not provide multiple {@link JAXBContext} instances, but must make sure that an 
 * instance of <b>this class</b> is cached. When the extended methods are called 
 * ({@link JAXBContext#createMarshaller()} for example), we then look at the internal 
 * cache to retrieve the correct {@link JAXBContext}).</li> 
 * <li>Different deployments may have different version of the same class. This means that
 * We can <i>not</i> use 1 {@link JAXBContext} instance to deal with all deployments. 
 * Tests have been specifically created to test this issue.</li>
 * <li>Concurrency: this is an application scoped class that is being acted on by multiple
 * REST request threads. Fortunately, we just handle normal REST requests (no comet, for 
 * example), so the requests themselves (with regards to the serializaiton logic) are all 
 * handled in one thread.</i> 
 * </ol>
 * <b>Regardless, please preserve existing tests when modifying this class!</b>
 */
@ApplicationScoped
public class DynamicJaxbContext extends JAXBContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContext.class);

    // The contextsCache needs to be a ConcurrentHashMap because parallel operations (involing *different* deployments) can happen on it.
    private static ConcurrentHashMap<String, JAXBContext> contextsCache = new ConcurrentHashMap<String, JAXBContext>();
    static { 
        setupDefaultJaxbContext();
    }

    private static ThreadLocal<JAXBContext> requestJaxbContextLocal = new ThreadLocal<JAXBContext>();
    
    @Inject
    // pkg level for tests
    DeploymentInfoBean deploymentInfoBean;

    private static AtomicInteger instanceCreated = new AtomicInteger(0);
    
    public DynamicJaxbContext() {
        if( ! instanceCreated.compareAndSet(0, 1) ) {
            logger.debug("Instance {} of the {} created!", instanceCreated.incrementAndGet(), DynamicJaxbContext.class.getSimpleName() ); 
        }
    }
  
    // Servlet Filter ------------------------------------------------------------------------------------------------------------
    
    public static void setDeploymentJaxbContext(String deploymentId) {
        JAXBContext jaxbContext = contextsCache.get(deploymentId);
        if( jaxbContext == null ) { 
           logger.debug("No JAXBContext available for deployment '" + deploymentId + "', using default JAXBContext instance."); 
           jaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
        }
        requestJaxbContextLocal.set(jaxbContext);
    }
    
    public static void clearDeploymentJaxbContext() {
       requestJaxbContextLocal.set(null);
    }

    // JAXBContext methods --------------------------------------------------------------------------------------------------------

    /**
     * This method is called by the {@link JAXBContext} methods when they need a {@link JAXBContext} to 
     * create a {@link Marshaller}, {@link Unmarshaller} or {@link Validator} instance.
     * @return The {@link JAXBContext} created or retrieved from cache for the request.
     */
    private JAXBContext getRequestContext() { 
        JAXBContext requestJaxbContext = requestJaxbContextLocal.get();
        if( requestJaxbContext == null ) { 
            throw new IllegalStateException("No jaxb context available for request!");
        }
        return requestJaxbContext;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createUnmarshaller()
     */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        JAXBContext context = getRequestContext();
        if (context != null) {
            return context.createUnmarshaller();
        }
        throw new IllegalStateException("No Unmarshaller available: JAXBContext instance could be found for this request!");
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createMarshaller()
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = getRequestContext();
        if (context != null) {
            return context.createMarshaller();
        }
        throw new IllegalStateException("No Marshaller available: JAXBContext instance could be found for this request!");
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createValidator()
     */
    @Override
    public Validator createValidator() throws JAXBException {
        JAXBContext context = getRequestContext();
        if (context != null) {
            return context.createValidator();
        }
        throw new IllegalStateException("No Validator available: JAXBContext instance could be found for this request!");
    }
    
    // Deployment jaxbContext management and creation logic -----------------------------------------------------------------------
    
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
     * Creates the default JAXB Context at initialization
     */
    private static void setupDefaultJaxbContext() {
        try {
            int kieJaxbClassSetLength = KIE_JAXB_CLASS_SET.size();
            Class<?> [] types = new Class<?> [kieJaxbClassSetLength + PRIMITIVE_ARRAY_CLASS_SET.size()];
            System.arraycopy(KIE_JAXB_CLASS_SET.toArray(new Class<?>[kieJaxbClassSetLength]), 0, types, 0, kieJaxbClassSetLength);
            int primArrClassSetLength = PRIMITIVE_ARRAY_CLASS_SET.size();
            System.arraycopy(PRIMITIVE_ARRAY_CLASS_SET.toArray(new Class<?>[primArrClassSetLength]), 0, types, kieJaxbClassSetLength, primArrClassSetLength);

            JAXBContext defaultJaxbContext = JAXBContext.newInstance(types);

            contextsCache.put(DEFAULT_JAXB_CONTEXT_ID, defaultJaxbContext);
        } catch (JAXBException e) {
            throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", e);
        }
    }

    /**
     * Creates the {@link JAXBContext} instance for the given deployment at deployment time
     * @param deploymentId The deployment identifier.
     */
    private void setupDeploymentJaxbContext(String deploymentId) { 
        if( contextsCache.contains(deploymentId) ) { 
            logger.error("JAXB context instance already found when deploying deployment '" + deploymentId + "'!");
            contextsCache.remove(deploymentId);
        }
        
        // retrieve deployment classes
        Collection<Class<?>> depClasses = deploymentInfoBean.getDeploymentClasses(deploymentId);

        // While we have no guarantees that JAXBContext instances are thread-safe, 
        // the REST framework using the JAXBContext instance is responsible for that thread-safety
        // since it is caching the JAXBContext in any case.. 
        if( depClasses.size() == 0 ) { 
            JAXBContext defaultJaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
            contextsCache.put(deploymentId, defaultJaxbContext);
            return;
        }
        
        // create set of all classes needed
        Set<Class<?>> allClasses = new HashSet<Class<?>>(KIE_JAXB_CLASS_SET);
        allClasses.addAll(PRIMITIVE_ARRAY_CLASS_SET);
        allClasses.addAll(depClasses);
        Class [] allClassesArr = allClasses.toArray(new Class[allClasses.size()]);

        // create and cache jaxb context 
        JAXBContext jaxbContext = null;
        try { 
            jaxbContext = JAXBContext.newInstance(allClassesArr);
            contextsCache.put(deploymentId, jaxbContext);
        } catch( JAXBException jaxbe ) { 
            String errMsg = "Unable to instantiate JAXBContext for deployment '" + deploymentId + "'.";
            throw new IllegalStateException( errMsg, jaxbe );
            // This is a serious problem if it goes wrong here. 
        }
    }
}
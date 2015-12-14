/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.rest.jaxb;

import static org.kie.remote.services.cdi.DeploymentInfoBean.emptyDeploymentId;
import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID;
import static org.kie.services.client.serialization.JaxbSerializationProvider.configureMarshaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.jbpm.kie.services.api.DeploymentIdResolver;
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

/**
 * <h3>READ THIS BEFORE WORKING ON THIS CLASS!</h3>
 * </p>
 * There are a couple of hidden issues that this class deals this:
 * <p><ol>
 * <li> {@link JAXBContext} instances are cached by the REST framework (and then reused
 * with *all* request). This means that we can not provide multiple {@link JAXBContext}
 * instances, but must make sure that an instance of <b>this class</b> is cached -- which
 * will then provide <em>different</em> (un)marshallers depending on the deploymentId used
 * in the request.
 * </p>
 * This means that when  the extended methods are called ({@link JAXBContext#createMarshaller()}
 * for example), we then look at the internal cache to retrieve the correct
 * {@link JAXBContext}).</li>
 * <li>Different deployments may have different versions of the same class. This means that
 * We can <i>not</i> use 1 (internally cached) {@link JAXBContext} instance to deal with
 * all deployments. Tests have been specifically created to test this issue.</li>
 * <li>Concurrency: this is an application scoped class that is being acted on by multiple
 * REST request threads. Fortunately, we just handle normal REST requests (no comet, for
 * example), so the requests themselves (with regards to the serializaiton logic) are all
 * handled in one thread.</i>
 * </ol>
 * </p>
 * With regards to classloading and related issues, these rules apply:
 * <ol>
 * <li>
    Implementations are open-ended; anyone can implement those interfaces, even by different people from different modules, provided they are all given to the JAXBContext.newInstance method. There's no need to list all the implementation classes in anywhere.
    Each implementation of the interface needs to have an unique element name.
    Every reference to interface needs to have the XmlElementRef annotation. The type=Object.class portion tells JAXB that the greatest common base type of all implementations would be java.lang.Object.

 * <b>In all cases, please preserve existing tests when modifying this class!</b>
 */
@ApplicationScoped
public class DynamicJaxbContext extends JAXBContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContext.class);

    // The contextsCache needs to be a ConcurrentHashMap because parallel operations (involing *different* deployments) can happen on it.
    private static ConcurrentHashMap<String, JAXBContext> contextsCache = new ConcurrentHashMap<String, JAXBContext>();
    static {
        setupDefaultJaxbContext();
    }

    private static ThreadLocal<JAXBContext> requestJaxbContext = new ThreadLocal<JAXBContext>();
    static Map<String, Collection<Class<?>>> deploymentClassesMap = new ConcurrentHashMap<String, Collection<Class<?>>>();

    private static AtomicInteger instanceCreated = new AtomicInteger(0);

    public DynamicJaxbContext() {
        if( ! instanceCreated.compareAndSet(0, 1) ) {
            logger.debug("Instance {} of the {} created!", instanceCreated.incrementAndGet(), DynamicJaxbContext.class.getSimpleName() );
        }
    }

    // Servlet Filter ------------------------------------------------------------------------------------------------------------

    public static void setDeploymentJaxbContext(String deploymentId) {
        if( !emptyDeploymentId(deploymentId) && !deploymentId.equals(DEFAULT_JAXB_CONTEXT_ID) ) {
            // resolve in case a latest version is to be used
            deploymentId = DeploymentIdResolver.matchAndReturnLatest(deploymentId, deploymentClassesMap.keySet());
        }

        JAXBContext projectJaxbContext = contextsCache.get(deploymentId);
        if( projectJaxbContext == null ) {
           logger.debug("No JAXBContext available for deployment '" + deploymentId + "', using default JAXBContext instance.");
           projectJaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
        }

        requestJaxbContext.set(projectJaxbContext);
    }


    public static void clearDeploymentJaxbContext() {
       requestJaxbContext.set(null);
    }

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

    // JAXBContext methods --------------------------------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createUnmarshaller()
     */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        JAXBContext context = requestJaxbContext.get();
        if (context != null) {
            return context.createUnmarshaller();
        }
        throw new KieRemoteServicesInternalError("No Unmarshaller available: JAXBContext instance could be found for this request!");
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createMarshaller()
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = requestJaxbContext.get();
        if (context != null) {
            return configureMarshaller(context, false);
        }
        throw new KieRemoteServicesInternalError("No Marshaller available: JAXBContext instance could be found for this request!");
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createValidator()
     */
    @Override
    @SuppressWarnings("deprecation")
    public Validator createValidator() throws JAXBException {
        JAXBContext context = requestJaxbContext.get();
        if (context != null) {
            return context.createValidator();
        }
        throw new KieRemoteServicesInternalError("No Validator available: JAXBContext instance could be found for this request!");
    }

    // Methods for other beans/resources ------------------------------------------------------------------------------------------

    /**
     * Used by classes involved with de/serialzation in order to retrieve (user-defined) clases
     * to be used in de/serialization.
     * @param deploymentId The deployment unit id
     * @return A Collection of Classes that are in the deployment unit
     */
    public Collection<Class<?>> getDeploymentClasses(String deploymentId) {
        if( emptyDeploymentId(deploymentId) ) {
            return Collections.emptySet();
        }
        Collection<Class<?>> classes = deploymentClassesMap.get(deploymentId);
        if( classes == null ) {
            return Collections.emptySet();
        }
        return classes;
     }

    /**
     * Creates the default JAXB Context at initialization
     */
    private static void setupDefaultJaxbContext() {
        try {
            Class<?> [] types = ServerJaxbSerializationProvider.getAllBaseJaxbClasses();
            JAXBContext defaultJaxbContext = JAXBContext.newInstance(types);

            contextsCache.put(DEFAULT_JAXB_CONTEXT_ID, defaultJaxbContext);
        } catch (JAXBException e) {
            throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", e);
        }
    }

    public JAXBContext getDeploymentJaxbContext(String deploymentId) {
        JAXBContext jaxbContext = contextsCache.get(deploymentId);
        if( jaxbContext == null ) {
            logger.debug("No JAXBContext available for deployment '" + deploymentId + "', using default JAXBContext instance.");
            jaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
        }
        return jaxbContext;
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

}
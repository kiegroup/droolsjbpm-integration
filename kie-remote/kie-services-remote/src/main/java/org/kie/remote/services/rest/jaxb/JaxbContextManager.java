package org.kie.remote.services.rest.jaxb;

import static org.kie.services.client.serialization.JaxbSerializationProvider.KIE_JAXB_CLASS_SET;
import static org.kie.services.client.serialization.JaxbSerializationProvider.PRIMITIVE_ARRAY_CLASS_SET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jbpm.kie.services.impl.event.Deploy;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JaxbContextManager {

    private static final Logger logger = LoggerFactory.getLogger(JaxbContextManager.class);

    private ConcurrentHashMap<String, JAXBContext> contextsCache = new ConcurrentHashMap<String, JAXBContext>();
    private ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<String, Object>();

    @Inject
    DeploymentInfoBean deploymentClassNameBean;

    public JaxbContextManager() {
       // default constructor 
    }

    /**
     * Adds a deployment lock object. 
     * @param event The {@link DeploymentEvent} fired on deployment
     */
    public void createDeploymentLockObjectOnDeploy(@Observes @Deploy DeploymentEvent event) {
        String deploymentId = event.getDeploymentId();
        locks.putIfAbsent(deploymentId, new Object());
    }

    /**
     * Remove the cached {@link JAXBContext} and deployment lock object
     * </p>
     * It's <b>VERY</b> important that the cached {@link JAXBContext} instance be removed! The new deployment
     * may contain a <b>different</b> version of a class with the same name. Keeping the old class definition 
     * will cause problems!
     * @param event The {@link DeploymentEvent} fired on an undeploy of a deployment
     */
    public void cleanUpOnUndeploy(@Observes @Undeploy DeploymentEvent event) {
        String deploymentId = event.getDeploymentId();
        contextsCache.remove(deploymentId);
        locks.remove(event.getDeploymentId());
    }

    /**
     * This method does the following:<ol>
     * <li>Determine the deployment id from the {@link UriInfo} instance.</li>
     * <li>If the deployment id can <i>not</i> be determined, return a default {@link JAXBContext} instance.</li>
     * <li>If a {@link JAXBContext} instance has already been created for this deployment, 
     *     retrieve it from the {@link JAXBContext} cache ({@link JaxbContextManager#contextsCache}) and return it.</li>
     * <li>Create a {@link JAXBContext} instance based on the classes from the appropriate deployment.</li>
     * <li>Cache the {@link JAXBContext} instance and return it.</li>
     * </ol>
     * 
     * @param type The {@link Class} of the new object that should be deserialized by returned {@link JAXBContext}.
     * @param uriInfo A {@link UriInfo} instance needed to determine the deployment containing the necessary classes.
     * @return A {@link JAXBContext} instance
     */
    public JAXBContext getJaxbContext(Class<?> type, UriInfo uriInfo) {
        
        String deploymentId = getDeploymentId(uriInfo);
        
        if (deploymentId == null) {
            logger.warn("No deployment id present in URL: default JAXBContext will be used");
            try {
                int kieJaxbClassSetLength = KIE_JAXB_CLASS_SET.size();
                Class<?> [] types = new Class<?> [kieJaxbClassSetLength + PRIMITIVE_ARRAY_CLASS_SET.size()];
                System.arraycopy(KIE_JAXB_CLASS_SET.toArray(new Class<?>[kieJaxbClassSetLength]), 0, types, 0, kieJaxbClassSetLength);
                int primArrClassSetLength = PRIMITIVE_ARRAY_CLASS_SET.size();
                System.arraycopy(PRIMITIVE_ARRAY_CLASS_SET.toArray(new Class<?>[primArrClassSetLength]), 0, types, kieJaxbClassSetLength, primArrClassSetLength);
                return JAXBContext.newInstance(types);
            } catch (JAXBException e) {
                throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", e);

            }
        }

        Object deploymentLockObject = locks.get(deploymentId);
        if( deploymentLockObject == null ) { 
            // I love you {@link ConcurrentHashMap}!! (for not making me do double-checked locking)
            locks.putIfAbsent(deploymentId, deploymentLockObject);
            deploymentLockObject = locks.get(deploymentId);
        }
        
        // synchronized to make action atomic
        synchronized(deploymentLockObject) {
            if (contextsCache.containsKey(deploymentId)) {
                return contextsCache.get(deploymentId);
            }
        }

        // retrieve deployment classes
        Collection<Class<?>> depClasses = deploymentClassNameBean.getDeploymentClasses(deploymentId);
        if( logger.isDebugEnabled() ) { 
            boolean found = false;
            for( Class<?> clazz : depClasses ) {
                // Compare names, since different classloaders (kjar, server) mean that a direct comparison will fail
                if( clazz.getName().equals(type) ) { 
                    found = true;
                }
                logger.debug( "Adding {} to JAXBContext instance.", clazz.getName() );
            }
            if( ! found) { 
                logger.debug( "Class '{}' is not known to the deployment!", type.getCanonicalName());
            }
        }

        // create set of all classes needed
        Set<Class<?>> allClasses = new HashSet<Class<?>>(KIE_JAXB_CLASS_SET);
        allClasses.addAll(PRIMITIVE_ARRAY_CLASS_SET);
        allClasses.addAll(depClasses);
        Class [] allClassesArr = allClasses.toArray(new Class[allClasses.size()]);

        // create and cache jaxb context 
        JAXBContext jaxbContext = null;
        try { 
            // synchronized to make action atomic (see synchronized clause above for conflict) 
            synchronized(deploymentLockObject) {
                jaxbContext = JAXBContext.newInstance(allClassesArr);
                contextsCache.put(deploymentId, jaxbContext);
            }
            return jaxbContext;
        } catch( JAXBException jaxbe ) { 
            logger.error( "Unable to instantiate JAXBContext for deployment '{}'", deploymentId);
            throw new IllegalStateException( "Unable to create new " + JAXBContext.class.getSimpleName() + " instance.", jaxbe);
        }
    }

    /**
     * Extracts the deployment id from the URL, whether it's part of the actual URL (as a path parameter) 
     * or as a URL query parameter.
     * @param uriInfo A {@link UriInfo} instance containing information about the URL
     * @return The deployment id
     */
    private String getDeploymentId(UriInfo uriInfo) {
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

}

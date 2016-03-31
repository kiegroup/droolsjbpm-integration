/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextManager.deploymentClassesMap;
import static org.kie.services.client.serialization.JaxbSerializationProvider.configureMarshaller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.jbpm.kie.services.api.DeploymentIdResolver;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <li> As we've recently discovered, because certain CDI frameworks (openwebbeans, among others)
 * do <b>not</b> delegate the {@link Object#equals(Object)} method, this class can and must <b>not</b>
 * be a CDI bean.</li>
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

public class DynamicJaxbContext extends JAXBContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContext.class);

    // The contextsCache needs to be a ConcurrentHashMap because parallel operations (involing *different* deployments) can happen on it.
    static ConcurrentHashMap<String, JAXBContext> contextsCache = new ConcurrentHashMap<String, JAXBContext>();
    static {
        setupDefaultJaxbContext();
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

    private static ThreadLocal<JAXBContext> requestJaxbContext = new ThreadLocal<JAXBContext>();


    private static final AtomicInteger idGen = new AtomicInteger(0);
    private final int id = idGen.incrementAndGet();

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

    // Deployment jaxbContext management and creation logic -----------------------------------------------------------------------



    @Override
    public boolean equals( Object obj ) {
        if( obj instanceof DynamicJaxbContext ) {
            return this.id == ((DynamicJaxbContext) obj).id;
        }
        return false;
    }


}
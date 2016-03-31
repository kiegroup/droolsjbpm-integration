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

import static org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID;
import static org.kie.services.client.serialization.JaxbSerializationProvider.configureMarshaller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
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
 * <li> As we've recently discovered, because certain CDI frameworks (openwebbeans, among others)
 * do <b>not</b> delegate the {@link Object#equals(Object)} method, this class can and must <b>not</b>
 * be a CDI bean.</li>
 * <li>Different deployments may have different version of the same class. This means that
 * We can <i>not</i> use only 1 {@link JAXBContext} instance to deal with all deployments.
 * Tests have been specifically created to test this issue.</li>
 * <li>Concurrency: this is an application scoped class that is being acted on by multiple
 * REST request threads. Fortunately, we just handle normal REST requests (no comet, for
 * example), so the requests themselves (with regards to the serializaiton logic) are all
 * handled in one thread.</i>
 * </ol>
 * <b>Regardless, please preserve existing tests when modifying this class!</b>
 */

public class DynamicJaxbContext extends JAXBContext {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContext.class);

    // The contextsCache needs to be a ConcurrentHashMap because parallel operations (involing *different* deployments) can happen on it.
    static ConcurrentHashMap<String, JAXBContext> contextsCache = new ConcurrentHashMap<String, JAXBContext>();
    static {
        setupDefaultJaxbContext();
    }

    private static ThreadLocal<JAXBContext> requestJaxbContextLocal = new ThreadLocal<JAXBContext>();

    private static final AtomicInteger idGen = new AtomicInteger(0);
    private final int id = idGen.incrementAndGet();

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
            logger.error("No JAXB context could be found for request, using default!");
            requestJaxbContext = contextsCache.get(DEFAULT_JAXB_CONTEXT_ID);
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
        throw new KieRemoteServicesInternalError("No Unmarshaller available: JAXBContext instance could be found for this request!");
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.JAXBContext#createMarshaller()
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = getRequestContext();
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
        JAXBContext context = getRequestContext();
        if (context != null) {
            return context.createValidator();
        }
        throw new KieRemoteServicesInternalError("No Validator available: JAXBContext instance could be found for this request!");
    }

    // Deployment jaxbContext management and creation logic -----------------------------------------------------------------------

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

    @Override
    public boolean equals( Object obj ) {
        if( obj instanceof DynamicJaxbContext ) {
            return this.id == ((DynamicJaxbContext) obj).id;
        }
        return false;
    }


}
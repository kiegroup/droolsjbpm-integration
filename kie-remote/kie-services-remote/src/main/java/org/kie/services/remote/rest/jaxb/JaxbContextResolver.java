package org.kie.services.remote.rest.jaxb;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.kie.api.runtime.KieSession;
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
    private DynamicJAXBContext dynamicContext;

    @Context
    private UriInfo uriInfo;

    @Override
    public JAXBContext getContext(Class<?> type) {
        dynamicContext.addType(type);
        // this assumes that UriInfo is proxied so it will have right values for every request
        dynamicContext.setUriInfo(uriInfo);
        return dynamicContext;
    }

}

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

import static org.kie.remote.services.cdi.DeploymentInfoBean.*;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;

import org.jbpm.kie.services.api.DeploymentIdResolver;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@link Filter} implemenation that's responsible for 2 things:<ol>
 * <li>Resolving the deploymentId value in the HTTP (REST) request so that we can</li>
 * <li>retrieve (and if necessary create) the {@link JAXBContext} for the given deployment</li>
 * </ol>
 * Lastly, once that {@link JAXBContext} has been created, it's set as a {@link ThreadLocal} instance, since
 * the request (particularly, the de/serialization of the request/response) will all happen in this thread.
 */
public class DynamicJaxbContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContextFilter.class);
    // private FilterConfig _filterConfig; // not used

    // "**" not accepted in URL's..
    public static final String DEFAULT_JAXB_CONTEXT_ID = "**DEFAULT";

    @Inject
    private DeploymentInfoBean deploymentInfoBean;

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String deploymentId = getDeploymentId(httpRequest);
        if( ! emptyDeploymentId(deploymentId) && ! deploymentId.equals(DEFAULT_JAXB_CONTEXT_ID) ) {
            // resolve in case a latest version is to be used
            deploymentId = DeploymentIdResolver.matchAndReturnLatest(deploymentId, deploymentInfoBean.getDeploymentIds());
        }

        DynamicJaxbContext.setDeploymentJaxbContext(deploymentId);
        logger.debug("JAXBContext retrieved and set for for '{}'", deploymentId);
        try {
            chain.doFilter(request, response);
        } finally {
            DynamicJaxbContext.clearDeploymentJaxbContext();
        }
    }

    @Override
    public void destroy() {
        // not used
    }

    // pkg static for tests
    static String getDeploymentId(HttpServletRequest request) {
        String deploymentId = null;

        // extract from header
        deploymentId = request.getHeader(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER);
        if( ! emptyDeploymentId(deploymentId) ) {
           return deploymentId;
        }

        // extract from the proper url
        String requestUri = request.getRequestURI();
        String [] urlParts = requestUri.split("/");
        for( int i = 0; i < urlParts.length; ++i ) {
           if( urlParts[i].equals("deployment")
               || urlParts[i].equals("runtime") ) {
              if( i+1 < urlParts.length ) {
                  deploymentId = urlParts[i+1];
                  if( ! emptyDeploymentId(deploymentId) ) {
                     return deploymentId;
                  }
                  break;
              }
           }
        }

        // get parameter
        deploymentId = request.getParameter("deploymentId");
        if( ! emptyDeploymentId(deploymentId) ) {
           return deploymentId;
        }

        return DEFAULT_JAXB_CONTEXT_ID;
    }
}

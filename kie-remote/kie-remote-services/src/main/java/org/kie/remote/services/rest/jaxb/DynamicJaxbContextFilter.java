package org.kie.remote.services.rest.jaxb;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jbpm.kie.services.api.DeploymentIdResolver;

import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // resolve in case a latest version is to be used
        deploymentId = DeploymentIdResolver.matchAndReturnLatest(deploymentId, deploymentInfoBean.getDeploymentIds());

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
        // _filterConfig = null; // not used
    }

    // pkg static for tests
    static String getDeploymentId(HttpServletRequest request) {
        String deploymentId = null;
    
        // extract from header
        deploymentId = request.getHeader(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER); 
        if( deploymentId != null ) { 
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
                  break;
              }
           }
        }
        
        // get parameter
        if( deploymentId == null ) { 
            deploymentId = request.getParameter("deploymentId");
        }

        // default id
        if( deploymentId == null ) { 
           return DEFAULT_JAXB_CONTEXT_ID; 
        }
        
        return deploymentId;
    } 
}

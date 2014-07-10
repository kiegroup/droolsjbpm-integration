package org.kie.remote.services.rest.jaxb;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class DynamicJaxbContextFilter implements Filter {

    private FilterConfig _filterConfig;
  
    // "**" not accepted in URL's.. 
    public static final String DEFAULT_JAXB_CONTEXT_ID = "**DEFAULT";
    
    public void init(FilterConfig filterConfig) throws ServletException {
        _filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String deploymentId = getDeploymentId(httpRequest);
        DynamicJaxbContext.setDeploymentJaxbContext(deploymentId);
        try { 
            chain.doFilter(request, response);
        } finally { 
            DynamicJaxbContext.clearDeploymentJaxbContext();
        }
    }

    @Override
    public void destroy() {
        _filterConfig = null;
    }

    // pkg static for tests
    static String getDeploymentId(HttpServletRequest request) {
        String deploymentId = null;
       
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

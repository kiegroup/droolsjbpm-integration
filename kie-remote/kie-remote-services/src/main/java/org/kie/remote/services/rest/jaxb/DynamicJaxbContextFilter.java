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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jbpm.kie.services.api.DeploymentIdResolver;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DynamicJaxbContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJaxbContextFilter.class);
    // private FilterConfig _filterConfig; // not used

    // "**" not accepted in URL's..
    public static final String DEFAULT_JAXB_CONTEXT_ID = "**DEFAULT";

    @Inject
    private DeploymentInfoBean deploymentInfoBean;

    public void init( FilterConfig filterConfig ) throws ServletException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String deploymentId = getDeploymentId(httpRequest);
        if( !emptyDeploymentId(deploymentId) && !deploymentId.equals(DEFAULT_JAXB_CONTEXT_ID) ) {
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

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // not used
    }

    /**
     * Retrieves the deployment id from the request, using 1 of 4 possible methods:<ul>
     * <li>Retrieve the deployment id from the "Kie-Deployment-Id" header, if set</li>
     * <li>Retrieve the deployment id from the URL, if present in the URL</li>
     * <li>Retrieve the deployment id from the query parameters</li>
     * <li>Read the {@link HttpServletRequest} content, and if it's a command-request xml, get the deployment id from there</li>
     * </ul>
     * @param request The {@link HttpServletRequest} instance
     * @return The deployment id {@link String}, or null if none available
     */
    static String getDeploymentId( HttpServletRequest request ) {
        String deploymentId = null;

        // extract from header
        deploymentId = request.getHeader(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER);
        if( !emptyDeploymentId(deploymentId) ) {
            return deploymentId;
        }

        // extract from the proper url
        String requestUri = request.getRequestURI();
        String[] urlParts = requestUri.split("/");
        for( int i = 0; i < urlParts.length; ++i ) {
            if( urlParts[i].equals("deployment") || urlParts[i].equals("runtime") ) {
                if( i + 1 < urlParts.length ) {
                    deploymentId = urlParts[i + 1];
                    if( !emptyDeploymentId(deploymentId) ) {
                        return deploymentId;
                    }
                    break;
                }
            }
        }

        // get parameter
        deploymentId = request.getParameter("deploymentId");
        if( !emptyDeploymentId(deploymentId) ) {
            return deploymentId;
        }

        // extract command request
        deploymentId = getDeploymentIdFromXml(request);
        if( !emptyDeploymentId(deploymentId) ) {
            return deploymentId;
        }

        return DEFAULT_JAXB_CONTEXT_ID;
    }

    private static String COMMAND_REQUEST_ROOT_ELEMENT_NAME = null;
    private static String DEPLOYMENT_ID_NODE_NAME = null;
    static { 
        XmlRootElement xmlRootElemAnno = JaxbCommandsRequest.class.getAnnotation(XmlRootElement.class);
        if( xmlRootElemAnno != null ) { 
            COMMAND_REQUEST_ROOT_ELEMENT_NAME = xmlRootElemAnno.name();
        }
        String depIdFieldName = "deploymentId";
        Field deploymentIdField = null;
        try { 
            deploymentIdField = JaxbCommandsRequest.class.getDeclaredField(depIdFieldName);
            XmlElement xmlElemAnno = deploymentIdField.getAnnotation(XmlElement.class);
            if( xmlElemAnno != null ) { 
               DEPLOYMENT_ID_NODE_NAME = xmlElemAnno.name();
            }
        } catch( NoSuchFieldException nsfe ) { 
           logger.error("Unable to find " + JaxbCommandsRequest.class.getSimpleName() + "." + depIdFieldName + " field", nsfe); 
        }
    }
   
    /**
     * Retrieves the {@link HttpServletRequest} content, and parses it to see if 
     * the content contains a command-request xml structure with a deployment-id element containing a deployment id.
     * @param request The {@link HttpServletRequest} instance
     * @return The deployment id {@link String} or null, if none present
     */
    static String getDeploymentIdFromXml( HttpServletRequest request ) {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            if( reader == null || ! reader.ready() ) { 
               return null; 
            }
            String line = null;
            while( (line = reader.readLine()) != null ) { 
                buffer.append(line);
            }
        } catch( IOException ioe ) {
            logger.debug("Unable to read " + HttpServletRequest.class.getSimpleName() + " content: " + ioe.getMessage(), ioe );
        } finally {
            if( reader != null ) {
                try {
                    reader.close();
                } catch( IOException ioe ) {
                    logger.debug( "Unable to close " + HttpServletRequest.class.getSimpleName() + " reader: " + ioe.getMessage(), 
                            ioe);
                }
            }
        }
        
        String body = buffer.toString();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream bodyInput = new ByteArrayInputStream(body.getBytes());
            Document doc = docBuilder.parse(bodyInput);
            
            Node commandRequestNode = doc.getFirstChild();
            if( ! commandRequestNode.getNodeName().equals(COMMAND_REQUEST_ROOT_ELEMENT_NAME) ) { 
               return null; 
            }
            NodeList requestChildNodes = commandRequestNode.getChildNodes();

            for (int i = 0; i < requestChildNodes.getLength(); i++) {
                Node childNode = requestChildNodes.item(i);
                if(childNode.getNodeType()==Node.ELEMENT_NODE && childNode.getNodeName().equals(DEPLOYMENT_ID_NODE_NAME)) {
                    return childNode.getTextContent();
                }
            }
        } catch( ParserConfigurationException pce ) {
            logger.debug( "Unable to create " + DocumentBuilder.class.getSimpleName() + " to parse " 
                    + HttpServletRequest.class.getSimpleName() + " content.", pce);
        } catch( SAXException saxe ) {
            logger.debug( "Unable to parse " + HttpServletRequest.class.getSimpleName() + " content.", saxe );
        } catch( IOException ioe ) {
            logger.debug( "Unable to parse " + HttpServletRequest.class.getSimpleName() + " content.", ioe );
        }

        return null;
    }
}

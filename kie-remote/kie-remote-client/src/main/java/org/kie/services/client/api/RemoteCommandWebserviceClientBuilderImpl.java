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

package org.kie.services.client.api;

import static org.kie.services.client.api.command.AbstractRemoteCommandObject.emptyDeploymentId;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.client.ws.KieRemoteWsAuthenticator;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.Execute;
import org.kie.remote.services.ws.command.generated.ExecuteResponse;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.shared.ServicesVersion;


/**
 * This is the internal implementation of the {@link RemoteWebserviceClientBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteCommandWebserviceClientBuilderImpl extends RemoteWebserviceClientBuilderImpl<CommandWebService> {

    private final static String commandServiceNamespace = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    private final static QName commandServiceQName = new QName(commandServiceNamespace, "CommandServiceBasicAuth");

    @Override
    public CommandWebService buildBasicAuthClient() {
        checkAndFinalizeConfig();
        try {
            // wsdl authentication
            KieRemoteWsAuthenticator auth = new KieRemoteWsAuthenticator();
            auth.setUserAndPassword(config.getUserName(), config.getPassword());
    
            String wsdlLocationRelativePath = config.getWsdlLocationRelativePath();
    
            URL wsdlUrl;
            try {
                wsdlUrl = new URL(config.getServerBaseUrl(), wsdlLocationRelativePath);
            } catch( MalformedURLException murle ) {
                throw new IllegalStateException("WSDL URL is not correct: [" + config.getServerBaseUrl().toExternalForm() + wsdlLocationRelativePath + "]", murle);
            }
    
            wsdlUrl = verifyURLWithRedirect(wsdlUrl);
    
            // initial client proxy setup
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(CommandWebService.class);
            factory.setWsdlLocation(wsdlUrl.toExternalForm());
            factory.setServiceName(commandServiceQName);
    
            // JAXB: service classes
            Set<Class<?>> allClasses = new HashSet<Class<?>>();
            allClasses.add(JaxbCommandsRequest.class);
            allClasses.add(JaxbCommandsResponse.class);
            allClasses.add(Execute.class);
            allClasses.add(ExecuteResponse.class);
    
            // JAXB: extra classes
            Set<Class<?>> extraClasses = config.getExtraJaxbClasses();
            if( extraClasses != null && ! extraClasses.isEmpty() ) {
               allClasses.addAll(extraClasses);
            }
    
            // JAXB: setup
            JAXBDataBinding jaxbDataBinding;
            try {
                jaxbDataBinding = new JAXBDataBinding(allClasses.toArray(new Class[allClasses.size()]));
            } catch( JAXBException jaxbe ) {
                throw new RemoteApiException("Unable to initialize JAXB context for webservice client", jaxbe);
            }
            factory.getClientFactoryBean().setDataBinding(jaxbDataBinding);
    
            // setup auth
            // - for webservice calls
            String pwd = config.getPassword();
            String user = config.getUserName();
            factory.setUsername(user);
            factory.setPassword(pwd);
    
            CommandWebService commandService = (CommandWebService) factory.create();
            Client proxyClient = ClientProxy.getClient(commandService);
            HTTPConduit conduit = (HTTPConduit) proxyClient.getConduit();
    
            // setup timeout
            HTTPClientPolicy httpClientPolicy = conduit.getClient();
            httpClientPolicy.setConnectionTimeout(config.getTimeout());
            httpClientPolicy.setReceiveTimeout(config.getTimeout());
            httpClientPolicy.setAutoRedirect(config.getHttpRedirect());
    
            // if present, add deployment id for JAXB context
            String deploymentId = config.getDeploymentId();
            if( ! emptyDeploymentId(deploymentId) ) {
                Map<String, List<String>> headers = new HashMap<String, List<String>>(1);
                String [] depIdHeader = { deploymentId };
                headers.put(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER, Arrays.asList(depIdHeader));
                proxyClient.getRequestContext().put(org.apache.cxf.message.Message.PROTOCOL_HEADERS, headers);
            }
    
            return commandService;
        } finally {
            clearHttpUrlConnectionAuthCache();
        }
    }

    /**
     * Verify that the given URL points to a valid URL by connecting to the URL and checking the response status.
     * </p>
     * If HTTP redirect has been enabled, return the
     *
     * @param wsdlUrl
     * @return
     */
    private URL verifyURLWithRedirect(URL wsdlUrl) {
        int redirectTries = 0;
        URL newWsdlUrl = wsdlUrl;
        int connStatus = -1;
        HttpURLConnection conn = null;
        try {
            do {
                wsdlUrl = newWsdlUrl;
    
                
                try {
                    conn = (HttpURLConnection) wsdlUrl.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    String encoded = Base64Utility.encode((config.getUserName() + ":" + config.getPassword()).getBytes("UTF-8"));
                    conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Basic "+encoded);
                    connStatus = conn.getResponseCode();
                } catch (Exception e) {
                    throw new IllegalStateException("Could not verify WSDL URL: [" + wsdlUrl.toExternalForm() + "]", e);
                }
    
                switch( connStatus ) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER:
                    String newWsdlLoc = conn.getHeaderField(HttpHeaders.LOCATION);
                    if( config.getHttpRedirect() ) {
                        if( newWsdlLoc.startsWith("/") ) {
                            URL baseUrl = config.getServerBaseUrl();
                            newWsdlLoc = baseUrl.getProtocol() + "://" + baseUrl.getAuthority() + newWsdlLoc;
                        } else if( ! newWsdlLoc.startsWith("http") ) {
                            throw new RemoteCommunicationException("Could not parse redirect URL: [" + newWsdlLoc + "]");
                        }
                        try {
                            newWsdlUrl = new URL(newWsdlLoc);
                        } catch( MalformedURLException murle ) {
                            throw new RemoteCommunicationException("Redirect URL returned by server is invalid: [" + newWsdlLoc + "]", murle);
                        }
                    } else {
                        throw new RemoteCommunicationException("HTTP Redirect is not set but server redirected client to [" + newWsdlLoc + "]" );
                    }
                    break;
                default:
                    throw new RemoteCommunicationException("Status " + connStatus + " received when verifying WSDL URL: [" + wsdlUrl.toExternalForm() + "]");
                }
                ++redirectTries;
            } while( redirectTries < 3 && ! wsdlUrl.equals(newWsdlUrl) && connStatus != 200 );
    
            if( connStatus != 200 ) {
                if( newWsdlUrl.equals(wsdlUrl) && connStatus >= 300 && connStatus < 400 ) {
                    throw new RemoteCommunicationException("Unable to verify WSDL URL: request returned a redirect to the same URL [" + newWsdlUrl + "]");
                } else {
                    throw new RemoteCommunicationException("Unable to verify WSDL URL: request returned status " + connStatus + " after " + redirectTries + " redirects [" + newWsdlUrl + "]");
                }
            }
    
            if( ! wsdlUrl.equals(newWsdlUrl) ) {
                throw new RemoteCommunicationException("Server redirected (WSDL) request 3 times in a row. The last request URL was [" + newWsdlUrl + "]");
            }
    
            return wsdlUrl;
        } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
    }
    
    protected void clearHttpUrlConnectionAuthCache() {
        try {
            Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheValue");   
            Class<?> cint = Class.forName("sun.net.www.protocol.http.AuthCache");
            Class<?> cimpl = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
            
            Method m = c.getMethod("setAuthCache", new Class[]{cint});
            m.invoke(null, new Object[]{cimpl.newInstance()});
            
        } catch (Exception e) {
            // ignore as it might not exists as this is sun specific api/impl
        }
    }
}
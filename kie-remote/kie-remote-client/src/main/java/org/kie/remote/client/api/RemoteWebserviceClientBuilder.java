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

package org.kie.remote.client.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.kie.remote.services.ws.command.generated.CommandWebService;


/**
 * This interface defines the fluent builder methods that can be used when either configuring a Kie Remote Webservice client.
 * 
 * @param <B> The builder instance type
 * @param <S> The web service type
 */
public interface RemoteWebserviceClientBuilder<B, S> extends RemoteClientBuilder<RemoteWebserviceClientBuilder<B, S>> {
  
    /**
     * The URL used here should be in the following form:
     * <code>http://HOST:PORT/INSTANCE/</code>
     * The different parts of the URL are:<ul>
     *   <li><code>HOST</code>: the hostname or ip address</li>
     *   <li><code>PORT</code>: the port number that the application is available on (often 8080)</li>
     *   <li><code>INSTANCE</code>: the name of the application, often one of the following:<ul>
     *     <li>business-central</li>
     *     <li>kie-wb</li>
     *     <li>jbpm-console</li></ul></li>
     * </ul>
     * 
     * @param instanceUrl The URL of the application
     * @return The builder instance
     */
    RemoteWebserviceClientBuilder<B, S> addServerUrl(URL instanceUrl);
    
    /**
     * The URL used here should be in the following form:
     * <code>http://HOST:PORT/INSTANCE/</code>
     * The different parts of the URL are:<ul>
     *   <li><code>HOST</code>: the hostname or ip address</li>
     *   <li><code>PORT</code>: the port number that the application is available on (often 8080)</li>
     *   <li><code>INSTANCE</code>: the name of the application, often one of the following:<ul>
     *     <li>business-central</li>
     *     <li>kie-wb</li>
     *     <li>jbpm-console</li></ul></li>
     * </ul>
     * 
     * @param instanceUrlString A string with the URL of the application
     * @return The builder instance
     * @throws MalformedURLException If the url is malformed
     */
    RemoteWebserviceClientBuilder<B, S> addServerUrl(String instanceUrlString) throws MalformedURLException;
  
    /**
     * The string parameter here represents the URL path of the webserivce WSDL, not including
     * the hostname, port or application.
     * </p>
     * For example, if the WSDL was available at 
     *   <pre>http://localhost:8080/business-central/random/path/to/wsdl/Webservice.wsdl</pre>
     * Then the submitted string should be 
     *   <pre>random/path/to/wsdl/Webservice.wsdl</pre>
     * </p>
     * If this method is not used, the default value  for this location is 
     *   <pre>http://localhost:8080/business-central/ws/Webservice?wsdl</pre>
     * 
     * @param wsdlLocationRelativePath The relative path of the WSDL
     * @return The builder instance
     */
    RemoteWebserviceClientBuilder<B, S> setWsdlLocationRelativePath(String wsdlLocationRelativePath);
   
    /**
     * When initializing the webservice or doing webservice operation, make sure
     * that HTTP redirect responses are accepted and used. 
     * </p>
     * The default is to ignore HTTP redirect responses.
     * 
     * @return The builder instance
     */
    RemoteWebserviceClientBuilder<B, S> useHttpRedirect();
    
    /**
     * Creates a {@link CommandWebService} instance, using the 
     * configuration built up to this point. 
     * </p>
     * 
     * @return The {@link CommandWebService} instance
     * @throws @{link InsufficientInfoToBuildException} when insufficient information 
     * is provided to build the {@link CommandWebService}
     */
    S buildBasicAuthClient();

}

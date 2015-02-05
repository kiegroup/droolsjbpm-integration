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

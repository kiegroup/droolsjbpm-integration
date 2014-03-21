package org.kie.services.client.api.builder;

import java.net.URL;

import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;

/**
 * This is fluent API builder class for creating a {@link RemoteRestRuntimeEngineFactory}.
 */
public interface RemoteRestRuntimeEngineFactoryBuilder extends RemoteRuntimeEngineFactoryBuilder<RemoteRestRuntimeEngineFactoryBuilder, RemoteRestRuntimeEngineFactory> {

    /**
     * Form-based authentication is sometimes necessary to use, for example when using Tomcat
     * as the underlying application server. 
     * @param formBasedAuth Indicates whether or not to use form-based authentication.
     * @return The builder instance
     */
    RemoteRestRuntimeEngineFactoryBuilder useFormBasedAuth(boolean formBasedAuth);
    
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
    RemoteRestRuntimeEngineFactoryBuilder addUrl(URL instanceUrl);
}
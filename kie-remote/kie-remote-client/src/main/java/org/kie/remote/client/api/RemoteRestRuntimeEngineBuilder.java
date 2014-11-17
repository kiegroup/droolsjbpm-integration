package org.kie.remote.client.api;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.builder.RemoteRestRuntimeEngineFactoryBuilder;


/**
 * This is fluent API builder class for creating a remote {@link RuntimeEngine} instance 
 * or a {@link RemoteRestRuntimeEngineFactory}.
 */
public interface RemoteRestRuntimeEngineBuilder extends RemoteRestRuntimeEngineFactoryBuilder {

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
    RemoteRestRuntimeEngineBuilder addUrl(URL instanceUrl);
}
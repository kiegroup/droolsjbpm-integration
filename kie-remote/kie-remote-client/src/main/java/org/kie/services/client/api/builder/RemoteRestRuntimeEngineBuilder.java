package org.kie.services.client.api.builder;

import java.net.URL;

import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;

/**
 * This is fluent API builder class for creating a {@link RemoteRestRuntimeEngineFactory}.
 */
public interface RemoteRestRuntimeEngineBuilder extends RemoteRuntimeEngineBuilder<RemoteRestRuntimeEngineBuilder, RemoteRestRuntimeEngineFactory> {

    /**
     * As of the Drools/jBPM 6.2.0 release, form based authentication is no longer necessary 
     * for tomcat instances. All instances of kie-wb, kie-drools-wb, or jbpm-console
     * now always use normal ("preemptive") http authentication. 
     * </p>
     * <b>This method thus no longer does anything</b> and has been deprecated.
     * @param formBasedAuth Indicates whether or not to use form-based authentication.
     * @return The builder instance
     */
    @Deprecated
    RemoteRestRuntimeEngineBuilder useFormBasedAuth(boolean formBasedAuth);
    
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
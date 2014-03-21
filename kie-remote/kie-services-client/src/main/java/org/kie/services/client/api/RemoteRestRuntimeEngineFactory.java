package org.kie.services.client.api;

import static org.kie.services.client.api.command.RemoteConfiguration.*;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.builder.RemoteRestRuntimeEngineFactoryBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * A factory for creating REST remote API client instances of the {@link RuntimeEngine}.
 */
public class RemoteRestRuntimeEngineFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration config;

    RemoteRestRuntimeEngineFactory() { 
        // default constructor for builder use
    }
    
    RemoteRestRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }
  
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteRestRuntimeEngineFactory#newBuilder()} method. 
     * 
     * @param deploymentId The deployment id
     * @param baseUrl The {@link URL} of the application instance (for example: http://localhost:8080/business-central)
     * @param username The user name used to authenticate and authorize the REST request
     * @param password The password associated with the user name
     */
    public RemoteRestRuntimeEngineFactory(String deploymentId, URL baseUrl, String username, String password) {
        this.config = new RemoteConfiguration(deploymentId, baseUrl, username, password);
    }
   
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteRestRuntimeEngineFactory#newBuilder()} method. 
     * 
     * @param deploymentId The deployment id
     * @param baseUrl The {@link URL} of the application instance (for example: http://localhost:8080/business-central)
     * @param username The user name used to authenticate and authorize the REST request
     * @param password The password associated with the user name
     * @param useFormBasedAuth Whether or not to use form-based authentication (only suggested when the application is running on Tomcat)
     */
    @Deprecated
    public RemoteRestRuntimeEngineFactory(String deploymentId, URL baseUrl, String username, String password, boolean useFormBasedAuth) {
        this.config = new RemoteConfiguration(deploymentId, baseUrl, username, password, DEFAULT_TIMEOUT, useFormBasedAuth);
    }
   
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteRestRuntimeEngineFactory#newBuilder()} method. 
     * 
     * @param deploymentId The deployment id
     * @param baseUrl The {@link URL} of the application instance (for example: http://localhost:8080/business-central)
     * @param username The user name used to authenticate and authorize the REST request
     * @param password The password associated with the user name
     * @param timeoutInSeconds The maximum amount of time to wait for a response from the application instance
     */
    @Deprecated
    public RemoteRestRuntimeEngineFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds) {
        this.config = new RemoteConfiguration(deploymentId, baseUrl, username, password, timeoutInSeconds);
    }
  
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteRestRuntimeEngineFactory#newBuilder()} method. 
     * 
     * @param deploymentId The deployment id
     * @param baseUrl The {@link URL} of the application instance (for example: http://localhost:8080/business-central)
     * @param username The user name used to authenticate and authorize the REST request
     * @param password The password associated with the user name
     * @param timeoutInSeconds The maximum amount of time to wait for a response from the application instance
     * @param useFormBasedAuth Whether or not to use form-based authentication (only suggested when the application is running on Tomcat)
     */
    @Deprecated
    public RemoteRestRuntimeEngineFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds, boolean useFormBasedAuth) {
        this.config = new RemoteConfiguration(deploymentId, baseUrl, username, password, timeoutInSeconds, useFormBasedAuth);
    }
   
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteRestRuntimeEngineFactory#newBuilder()} method.
     * 
     * Adds a list of classes that will be used as parameters (and will thus need to be known to the client-side serialization
     * context).
     */
    @Deprecated
    public void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses ) { 
        this.config.addJaxbClasses(new HashSet<Class<?>>(extraJaxbClasses));
    }

    public static RemoteRestRuntimeEngineFactoryBuilder newBuilder() {
        return new RemoteRestRuntimeEngineFactoryBuilderImpl();
    }

}

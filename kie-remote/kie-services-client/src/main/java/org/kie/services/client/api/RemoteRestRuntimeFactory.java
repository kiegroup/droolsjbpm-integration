package org.kie.services.client.api;

import static org.kie.services.client.api.command.RemoteConfiguration.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteRestRuntimeFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration configuration;

    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password);
    }
   
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, boolean useFormBasedAuth) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password, DEFAULT_TIMEOUT, useFormBasedAuth);
    }
   
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password, timeoutInSeconds);
    }
   
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds, boolean useFormBasedAuth) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password, timeoutInSeconds, useFormBasedAuth);
    }
   
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(configuration);
    }

    public void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses ) { 
        this.configuration.addJaxbClasses(new HashSet<Class<?>>(extraJaxbClasses));
    }
    
}

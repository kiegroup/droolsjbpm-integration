package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteConfiguration.AuthenticationType;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteRestSessionFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration configuration;
    
    public RemoteRestSessionFactory(String deploymentId, String url) {
        this.configuration = new RemoteConfiguration(deploymentId, url);
    }
    
    public RemoteRestSessionFactory(String deploymentId, String url, AuthenticationType authenticationType, String username, String password) {
        this.configuration = new RemoteConfiguration(deploymentId, url, authenticationType, username, password);
    }
    
    public RuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(configuration);
    }

}

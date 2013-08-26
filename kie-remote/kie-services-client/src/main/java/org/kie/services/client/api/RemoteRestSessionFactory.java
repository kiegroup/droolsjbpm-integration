package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteRestSessionFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration configuration;

    public RemoteRestSessionFactory(String deploymentId, String url, String username, String password) {
        this.configuration = new RemoteConfiguration(deploymentId, url, username, password);
    }
    
    public RuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(configuration);
    }

}

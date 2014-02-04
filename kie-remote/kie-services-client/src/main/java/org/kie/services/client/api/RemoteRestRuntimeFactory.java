package org.kie.services.client.api;

import java.net.URL;

import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteRestRuntimeFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration configuration;

    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password);
    }
   
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds) {
        this.configuration = new RemoteConfiguration(deploymentId, baseUrl, username, password, timeoutInSeconds);
    }
   
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(configuration);
    }

}

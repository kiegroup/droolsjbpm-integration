package org.kie.services.client.api.command;

import org.kie.api.runtime.manager.RuntimeManager;

public class RemoteSessionFactory {

    private String url;
    private String deploymentId;
    
    public RemoteSessionFactory(String url, String deploymentId) {
        this.url = url;
        this.deploymentId = deploymentId;
    }
    
    public RuntimeManager newRuntimeManager() {
    	return new RemoteRuntimeManager("Remote Runtime Manager", url, deploymentId);
    }
    
}

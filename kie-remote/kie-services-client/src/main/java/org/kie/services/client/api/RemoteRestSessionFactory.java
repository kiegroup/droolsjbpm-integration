package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.services.client.api.command.RemoteRuntimeManager;

public class RemoteRestSessionFactory {

    private String url;
    private String deploymentId;
    
    public RemoteRestSessionFactory(String url, String deploymentId) {
        this.url = url;
        this.deploymentId = deploymentId;
    }
    
    public RuntimeManager newRuntimeManager() {
    	return new RemoteRuntimeManager("Remote Runtime Manager", url, deploymentId);
    }
    
}

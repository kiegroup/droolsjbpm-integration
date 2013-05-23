package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.services.client.api.command.RemoteRuntimeManager;

public class RemoteRestSessionFactory {

    private String url;
    private String deploymentId;
    private AuthenticationType authenticationType;
    private String username;
    private String password;
    
    public RemoteRestSessionFactory(String url, String deploymentId) {
        this.url = url;
        this.deploymentId = deploymentId;
    }
    
    public RemoteRestSessionFactory(String url, String deploymentId, AuthenticationType authenticationType, String username, String password) {
        this.url = url;
        this.deploymentId = deploymentId;
        this.authenticationType = authenticationType;
        this.username = username;
        this.password = password;
    }
    
    public RuntimeManager newRuntimeManager() {
    	return new RemoteRuntimeManager("Remote Runtime Manager", url, deploymentId, authenticationType, username, password);
    }
    
    public enum AuthenticationType {
    	BASIC,
    	FORM_BASED
    }
    
}

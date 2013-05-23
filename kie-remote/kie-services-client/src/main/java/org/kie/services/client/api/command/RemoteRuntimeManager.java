package org.kie.services.client.api.command;

import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.client.api.RemoteRestSessionFactory.AuthenticationType;

public class RemoteRuntimeManager implements RuntimeManager {
	
	private String identifier;
	private String url;
	private String deploymentId;
	private AuthenticationType authenticationType;
	private String username;
	private String password;
	
	public RemoteRuntimeManager(String identifier, String url, String deploymentId, AuthenticationType authenticationType, String username, String password) {
		this.identifier = identifier;
		this.deploymentId = deploymentId;
		this.url = url;
		this.authenticationType = authenticationType;
		this.username = username;
		this.password = password;
	}

	public RuntimeEngine getRuntimeEngine(Context<?> context) {
		if (context instanceof EmptyContext) {
			return new RemoteRuntimeEngine(url, deploymentId, authenticationType, username, password, null);
		} else if (context instanceof ProcessInstanceIdContext) {
			return new RemoteRuntimeEngine(url, deploymentId, authenticationType, username, password, ((ProcessInstanceIdContext) context).getContextId() + "");
		} else {
			throw new UnsupportedOperationException(context.getClass() + " not supported");
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public void disposeRuntimeEngine(RuntimeEngine runtime) {
		// do nothing
	}

	public void close() {
		// do nothing
	}

    public String getUrl() {
        return url;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

}

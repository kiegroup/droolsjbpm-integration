package org.kie.services.client.api.command;

import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class RemoteRuntimeManager implements RuntimeManager {
	
	private String identifier;
	private String url;
	private String deploymentId;
	
	public RemoteRuntimeManager(String identifier, String url, String deploymentId) {
		this.identifier = identifier;
		this.deploymentId = deploymentId;
		this.url = url;
	}

	public RuntimeEngine getRuntimeEngine(Context<?> context) {
		if (context instanceof EmptyContext) {
			return new RemoteRuntimeEngine(url, deploymentId, null);
		} else if (context instanceof ProcessInstanceIdContext) {
			return new RemoteRuntimeEngine(url, deploymentId, ((ProcessInstanceIdContext) context).getContextId() + "");
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

}

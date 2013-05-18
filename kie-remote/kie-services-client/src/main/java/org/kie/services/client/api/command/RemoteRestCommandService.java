package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;

public class RemoteRestCommandService extends AbstractRemoteCommandObject implements CommandService {

	public RemoteRestCommandService(String url, String deploymentId) {
		super(url, deploymentId);
	}
	
	public Context getContext() {
		return null;
	}
    
}

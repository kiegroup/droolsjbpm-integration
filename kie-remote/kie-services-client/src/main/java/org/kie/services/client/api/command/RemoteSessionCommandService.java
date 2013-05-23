package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;
import org.kie.services.client.api.RemoteRestSessionFactory.AuthenticationType;

public class RemoteSessionCommandService extends AbstractRemoteCommandObject implements CommandService {

	public RemoteSessionCommandService(String baseUrl, String url, String deploymentId, AuthenticationType authenticationType, String username, String password) {
		super(baseUrl, url, deploymentId, authenticationType, username, password);
	}
	
	public Context getContext() {
		return null;
	}
    
}

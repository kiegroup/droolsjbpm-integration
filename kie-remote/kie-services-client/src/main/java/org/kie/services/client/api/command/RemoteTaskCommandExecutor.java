package org.kie.services.client.api.command;

import org.kie.api.runtime.CommandExecutor;
import org.kie.services.client.api.RemoteRestSessionFactory.AuthenticationType;

public class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandExecutor {
	
	public RemoteTaskCommandExecutor(String baseUrl, String url, String deploymentId, AuthenticationType authenticationType, String username, String password) {
		super(baseUrl, url, deploymentId, authenticationType, username, password);
	}
	
}

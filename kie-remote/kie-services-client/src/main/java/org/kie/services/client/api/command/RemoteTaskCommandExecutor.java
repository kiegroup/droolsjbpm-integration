package org.kie.services.client.api.command;

import org.kie.api.runtime.CommandExecutor;

public class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandExecutor {
	
	public RemoteTaskCommandExecutor(String url, String deploymentId) {
		super(url, deploymentId);
	}
	
}

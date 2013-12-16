package org.kie.services.client.api.command;

import org.kie.api.runtime.CommandExecutor;

class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandExecutor {
	
	RemoteTaskCommandExecutor(RemoteConfiguration configuration) { 
		super(configuration);
		isTaskService = true;
	}
	
}

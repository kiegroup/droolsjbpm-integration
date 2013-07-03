package org.kie.services.client.api.command;

import org.kie.api.runtime.CommandExecutor;
import org.kie.services.client.api.RemoteConfiguration;

public class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandExecutor {
	
	public RemoteTaskCommandExecutor(RemoteConfiguration configuration) { 
		super(configuration);
	}
	
	public RemoteTaskCommandExecutor(String url, RemoteConfiguration configuration) { 
		super(url, configuration);
	}
	
}

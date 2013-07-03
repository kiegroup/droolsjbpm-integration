package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;
import org.kie.services.client.api.RemoteConfiguration;

public class RemoteSessionCommandService extends AbstractRemoteCommandObject implements CommandService {

	public RemoteSessionCommandService(RemoteConfiguration config) {
		super(config);
	}
	
	public RemoteSessionCommandService(String url, RemoteConfiguration config) {
		super(url, config);
	}
	
	public Context getContext() {
		return null;
	}
    
}

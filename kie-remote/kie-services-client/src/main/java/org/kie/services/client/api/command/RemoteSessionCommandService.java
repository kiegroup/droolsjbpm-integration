package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;

class RemoteSessionCommandService extends AbstractRemoteCommandObject implements CommandService {

	RemoteSessionCommandService(RemoteConfiguration config) {
		super(config);
	}
	
	public Context getContext() {
		return null;
	}
    
}

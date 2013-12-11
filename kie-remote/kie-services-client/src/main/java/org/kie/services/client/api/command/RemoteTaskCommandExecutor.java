package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.command.Context;

class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandService {
	
	RemoteTaskCommandExecutor(RemoteConfiguration configuration) { 
		super(configuration);
		isTaskService = true;
	}

    @Override
    public Context getContext() {
        return null;
    }
}

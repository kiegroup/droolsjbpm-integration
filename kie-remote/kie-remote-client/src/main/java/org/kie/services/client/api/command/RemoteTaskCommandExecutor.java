package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.builder.exception.MissingRequiredInfoException;

class RemoteTaskCommandExecutor extends AbstractRemoteCommandObject implements CommandService {
	
	RemoteTaskCommandExecutor(RemoteConfiguration configuration) { 
		super(configuration);
		isTaskService = true;
		if( config.isJms() && config.getTaskQueue() == null ) { 
		    throw new MissingRequiredInfoException("No Task queue was specified.");
		}
	}

    @Override
    public Context getContext() {
        return null;
    }
}

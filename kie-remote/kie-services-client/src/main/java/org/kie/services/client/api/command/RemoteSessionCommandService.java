package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.kie.internal.command.Context;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.builder.exception.MissingRequiredInfoException;

class RemoteSessionCommandService extends AbstractRemoteCommandObject implements CommandService {

	RemoteSessionCommandService(RemoteConfiguration config) {
		super(config);
	    if( config.isJms() && config.getKsessionQueue() == null ) { 
	        throw new MissingRequiredInfoException("No KieSession queue was specified for the builder.");
	    }
	}
	
	public Context getContext() {
		return null;
	}
    
}

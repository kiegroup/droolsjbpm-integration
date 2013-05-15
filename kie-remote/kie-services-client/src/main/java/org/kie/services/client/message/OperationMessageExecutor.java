package org.kie.services.client.message;

import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;

public abstract class OperationMessageExecutor {

    protected Object executeOperation(OperationMessage request, CommandExecutor serviceInstance) {
        Object result = null;
        try {
            Command command = request.getCommand();
            result = serviceInstance.execute(command);
        } catch (Exception e ) {
            handleException(request, e);
        }

        return result;
    }
    
    protected abstract void handleException(OperationMessage request, Exception e);
    
}

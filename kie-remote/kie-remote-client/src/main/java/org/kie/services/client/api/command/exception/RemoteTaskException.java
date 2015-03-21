package org.kie.services.client.api.command.exception;

import org.kie.internal.task.exception.TaskException;


/**
 * This class will be deleted as of 7.x
 * 
 * @see org.kie.remote.client.api.exception.RemoteTaskException
 */
@Deprecated
public class RemoteTaskException extends TaskException {

    public RemoteTaskException(String message) {
        super(message);
    }

    public RemoteTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}

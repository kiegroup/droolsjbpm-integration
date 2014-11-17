package org.kie.services.client.api.command.exception;

import org.kie.internal.task.exception.TaskException;


@Deprecated
public class RemoteTaskException extends TaskException {

    public RemoteTaskException(String message) {
        super(message);
    }

    public RemoteTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}

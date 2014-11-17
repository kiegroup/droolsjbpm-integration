package org.kie.remote.client.api.exception;

import org.kie.internal.task.exception.TaskException;

public class RemoteTaskException extends org.kie.services.client.api.command.exception.RemoteTaskException {

    /** Generated serial versio UID */
    private static final long serialVersionUID = 2853230138916596256L;

    public RemoteTaskException(String message) {
        super(message);
    }

    public RemoteTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}

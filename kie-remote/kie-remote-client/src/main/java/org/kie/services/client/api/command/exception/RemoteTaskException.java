/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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

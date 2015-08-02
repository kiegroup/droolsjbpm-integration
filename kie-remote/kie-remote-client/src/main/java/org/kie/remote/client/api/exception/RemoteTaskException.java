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

package org.kie.remote.client.api.exception;

import org.kie.remote.client.internal.RemoteClientException;

public class RemoteTaskException extends org.kie.services.client.api.command.exception.RemoteTaskException {

    /** Generated serial versio UID */
    private static final long serialVersionUID = 2853230138916596256L;

    private String shortMessage;
    
    public RemoteTaskException(String message, String stackTrace) {
        super(message, message + ":\n" + stackTrace);
    }
    
    public RemoteTaskException(String message) {
        super(message);
    }

    public RemoteTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getShortMessage() {
        return shortMessage;
    }
}

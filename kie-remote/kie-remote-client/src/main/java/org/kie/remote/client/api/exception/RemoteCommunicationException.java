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

/**
 * This exception is thrown when communications with a remote REST or JMS service 
 * fail in the client RemoteRuntime* classes. 
 * </p>
 * In other words, this exception indicates<ul>
 * <li>That the communication has failed</li>
 * </ul>
 */
public class RemoteCommunicationException extends org.kie.services.client.api.command.exception.RemoteCommunicationException {

    /** generated serial version UID **/
    private static final long serialVersionUID = 7230681758239352495L;

    public RemoteCommunicationException(String msg, Throwable cause) { 
        super(msg, cause);
    }
    
    public RemoteCommunicationException(String msg) { 
        super(msg);
    }
    
}

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


/**
 * This class will be deleted as of 7.x
 * </p>
 * This exception is thrown when the remote API returns a message indicating
 * that the request operation (REST or JMS) has failed. 
 * </p>
 * In other words, this exception indicates<ul>
 * <li>That the communication has succeeded</li>
 * <li>But that the requested operation has failed due to problems on the server side</li>
 * </ul>
 * @see org.kie.remote.client.api.exception.RemoteApiException
 */
@Deprecated
public class RemoteApiException extends RuntimeException {

    public RemoteApiException(String s) {
        super(s);
    }

    public RemoteApiException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
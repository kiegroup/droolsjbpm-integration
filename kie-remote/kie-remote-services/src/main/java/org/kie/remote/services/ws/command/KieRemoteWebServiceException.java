/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.ws.command;

import javax.xml.ws.WebServiceException;

public class KieRemoteWebServiceException extends WebServiceException {

    /** generated serial version UID */
    private static final long serialVersionUID = -5846490699830607523L;
    

    /** Constructs a new exception with the specified detail 
     *  message.  The cause is not initialized.
     *  @param message The detail message which is later 
     *                 retrieved using the getMessage method
    **/
    public KieRemoteWebServiceException(String message) {
      super(message);
    }

    /** Constructs a new exception with the specified detail 
     *  message and cause.
     *
     *  @param message The detail message which is later retrieved
     *                 using the getMessage method
     *  @param cause   The cause which is saved for the later
     *                 retrieval throw by the getCause method 
    **/ 
    public KieRemoteWebServiceException(String message, Throwable cause) {
      super(message,cause);
    }
}

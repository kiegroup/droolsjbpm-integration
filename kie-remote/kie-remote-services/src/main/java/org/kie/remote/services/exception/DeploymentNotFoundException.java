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

package org.kie.remote.services.exception;

import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;


/**
 * This exception should be thrown when a REST or JMS operation references a deployment unit
 * that does not exist or can not be found.
 */
public class DeploymentNotFoundException extends KieRemoteRestOperationException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 6533087530265037387L;

    public DeploymentNotFoundException(String s) {
        super(s, Status.NOT_FOUND);
    }

    public DeploymentNotFoundException(String s, Throwable throwable) {
        super(s, throwable, Status.NOT_FOUND);
    }

}

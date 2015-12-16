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

/**
 * This class is meant to be thrown in situations that should never happen. 
 * </p> 
 * If this exception *is* thrown, then it's almost certain that it's caused by a the code, 
 * and not be the circumstances or data presented to this component. 
 */
public class KieRemoteServicesInternalError extends Error {

    /** generated serial version UID */
    private static final long serialVersionUID = -6741972907562227891L;
   
    public KieRemoteServicesInternalError(String msg) { 
        super(msg);
    }

    public KieRemoteServicesInternalError(String msg, Throwable cause) { 
        super(msg, cause);
    }

}

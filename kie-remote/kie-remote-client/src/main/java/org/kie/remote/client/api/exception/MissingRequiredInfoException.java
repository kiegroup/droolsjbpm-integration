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

package org.kie.remote.client.api.exception;

import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.internal.command.RemoteClientException;


/**
 * This exception is thrown by the *RuntimeFactory classes
 * when a method is called on instances created by the
 * {@link RemoteRuntimeEngineFactory}
 * </p>
 * It indicates that the client instance can not execute
 * the method called because required information is missing.
 */
public class MissingRequiredInfoException extends RemoteClientException {

    /** generated serial version UID */
    private static final long serialVersionUID = 7415935205523780077L;

    public MissingRequiredInfoException() {
        super();
    }

    public MissingRequiredInfoException(String msg) {
        super(msg);
    }
}

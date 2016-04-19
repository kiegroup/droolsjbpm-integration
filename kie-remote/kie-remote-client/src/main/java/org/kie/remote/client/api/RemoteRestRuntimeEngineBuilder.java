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

package org.kie.remote.client.api;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;


/**
 * This is fluent API builder class for creating a remote {@link RuntimeEngine} instance
 * or a {@link RemoteRestRuntimeEngineFactory}.
 */
public interface RemoteRestRuntimeEngineBuilder extends RemoteRuntimeEngineBuilder<RemoteRestRuntimeEngineBuilder, RemoteRestRuntimeEngineFactory> {

    /**
     * The URL used here should be in the following form:
     * <code>http://HOST:PORT/INSTANCE/</code>
     * The different parts of the URL are:<ul>
     *   <li><code>HOST</code>: the hostname or ip address</li>
     *   <li><code>PORT</code>: the port number that the application is available on (often 8080)</li>
     *   <li><code>INSTANCE</code>: the name of the application, often one of the following:<ul>
     *     <li>business-central</li>
     *     <li>kie-wb</li>
     *     <li>jbpm-console</li></ul></li>
     * </ul>
     *
     * @param instanceUrl The URL of the application
     * @return The builder instance
     */
    RemoteRestRuntimeEngineBuilder addUrl(URL instanceUrl);

    /**
     * This method should be used with the "org.kie.task.insecure" property (used on the server side).
     * </br>
     * This allows users to send task commands that refer to other users besides the user used for authenticating
     * the REST request.
     *
     * @return An instance of this builder
     */
    RemoteRestRuntimeEngineBuilder disableTaskSecurity();

    /**
     * Adds a header field that will be sent with each request.
     * </p>
     * Multiple calls to this method with the same header field name will *not* replace existing header fields
     * with the same header field name.
     *
     * @param headerFieldName The header field name
     * @param headerFieldValue The header field value
     *
     * @return An instance of this builder
     */
    RemoteRestRuntimeEngineBuilder addHeader(String headerFieldName, String headerFieldValue);

    /**
     * Clears all existing header field values for this builder.
     *
     * @return An instance of this builder
     */
    RemoteRestRuntimeEngineBuilder clearHeaderFields();

}
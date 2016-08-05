/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.jms;

import org.kie.server.api.model.ServiceResponsesList;

/**
 * Receives callbacks upon received response from the server.
 * Used with ASYNC_REPLY_PATTERN to allow processing of further work before receiving response.
 */
public interface ResponseCallback {

    /**
     * Invoked by async message listener when response was received that matches given selector
     * @param selector message selector used to filter messages
     * @param response actual (unmarshalled) response received from the server.
     */
    void onResponse(String selector, ServiceResponsesList response);

    /**
     * Returns received value if any. It's up to implementation to either block
     * while waiting for the response or return directly with null in case there is no
     * response available
     * @return returns message received from the server, if exists
     */
    ServiceResponsesList get();

    /**
     * Returns deserialized version of the response - it's taken from
     * ServiceResponseList.getResponses().get(0).getResult().
     * It attempts to provide as much as possible smooth usage as it would be
     * directly via *ServiceClient
     * @param type class type of expected result
     * @param <T> actual type expected
     * @return
     */
    <T> T get(Class<T> type);
}

/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.spi;

import java.util.Map;
import java.util.Set;

import io.undertow.server.HttpServerExchange;
import org.kie.server.router.ContainerInfo;

/**
 * Responsible for finding the proper container to deal with request
 */
public interface ContainerResolver {

    static final String NOT_FOUND = "NOT_FOUND";

    /**
     * Based on given request (exchange) find the proper container id to be used to locate the server that the request
     * should be routed to.
     * @param exchange exchange representing request to be routed
     * @param containerInfoPerContainer set of known containers with mapped details
     * @return actual container id if found otherwise <code>NOT_FOUND</code> should be returned
     */
    String resolveContainerId(HttpServerExchange exchange, Map<String, Set<ContainerInfo>> containerInfoPerContainer);

}

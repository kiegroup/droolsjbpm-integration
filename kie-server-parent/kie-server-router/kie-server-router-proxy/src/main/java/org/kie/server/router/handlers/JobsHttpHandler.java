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

package org.kie.server.router.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class JobsHttpHandler extends AbstractAggregateHttpHandler {
    
    private static final String PREFIX = "/jobs";

    public JobsHttpHandler(HttpHandler httpHandler, AdminHttpHandler adminHandler) {
        super(httpHandler, adminHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equals(HttpString.tryFromString("GET"))) {
            exchange.setRelativePath(PREFIX + exchange.getRelativePath());
            
        }
        if (exchange.getRequestMethod().equals(HttpString.tryFromString("GET"))
                && exchange.getQueryParameters().containsKey("containerId")) {
            exchange.setRelativePath(PREFIX + exchange.getRelativePath());
            httpHandler.handleRequest(exchange);
            
            return;
            
        }
        super.handleRequest(exchange);
    }

}

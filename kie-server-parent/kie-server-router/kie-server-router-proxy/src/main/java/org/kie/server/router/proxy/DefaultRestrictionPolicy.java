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

package org.kie.server.router.proxy;

import io.undertow.server.HttpServerExchange;
import org.kie.server.router.spi.RestrictionPolicy;

public class DefaultRestrictionPolicy implements RestrictionPolicy {

    @Override
    public boolean restrictedEndpoint(HttpServerExchange exchange, String containerId) {
        String relativePath = exchange.getRelativePath();

        if (relativePath.endsWith("/containers/" + containerId) || relativePath.endsWith("/scanner") || relativePath.endsWith("/release-id")) {
            // disallow requests that modify the container as that can lead to inconsistent setup
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Default restriction policy (disabled management endpoints)";
    }
}

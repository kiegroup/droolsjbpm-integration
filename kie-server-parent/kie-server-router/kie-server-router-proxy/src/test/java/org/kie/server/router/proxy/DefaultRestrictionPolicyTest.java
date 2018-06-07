/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import io.undertow.server.ServerConnection;
import io.undertow.util.HttpString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRestrictionPolicyTest {

    @Mock
    private ServerConnection serverConnection;

    private DefaultRestrictionPolicy restrictionPolicy;

    @Before
    public void setup() {
        restrictionPolicy = new DefaultRestrictionPolicy();
    }

    @Test
    public void testRestrictedEndpointWhenEndpointIsNotRestricted() {

        final HttpServerExchange exchange = makeHttpServerExchange("/containers/containerId", "GET");
        final String containerId = "containerId";
        final boolean isRestricted = restrictionPolicy.restrictedEndpoint(exchange, containerId);

        assertFalse(isRestricted);
    }

    @Test
    public void testRestrictedEndpointWhenRequestMethodIsNotGET() {

        final HttpServerExchange exchange = makeHttpServerExchange("/containers/containerId", "POST");
        final String containerId = "containerId";
        final boolean isRestricted = restrictionPolicy.restrictedEndpoint(exchange, containerId);

        assertTrue(isRestricted);
    }

    @Test
    public void testRestrictedEndpointWhenRelativePathEndsWithContainerId() {

        final HttpServerExchange exchange = makeHttpServerExchange("/scanner", "POST");
        final String containerId = "containerId";
        final boolean isRestricted = restrictionPolicy.restrictedEndpoint(exchange, containerId);

        assertTrue(isRestricted);
    }

    @Test
    public void testRestrictedEndpointWhenRelativePathEndsWithReleaseId() {

        final HttpServerExchange exchange = makeHttpServerExchange("/release-id", "POST");
        final String containerId = "containerId";
        final boolean isRestricted = restrictionPolicy.restrictedEndpoint(exchange, containerId);

        assertTrue(isRestricted);
    }

    private HttpServerExchange makeHttpServerExchange(final String relativePath,
                                                      final String requestMethod) {

        final HttpServerExchange exchange = new HttpServerExchange(serverConnection);
        exchange.setRelativePath(relativePath);
        exchange.setRequestMethod(HttpString.tryFromString(requestMethod));
        return exchange;
    }
}

/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.router.KieServerRouterConstants;
import org.kie.server.router.KieServerRouterEnvironment;

import static org.junit.Assert.assertEquals;

public class KieServerInfoHandlerInvalidPortTest {

    @Before
    public void setUp() {
        System.setProperty(KieServerRouterConstants.ROUTER_HOST, "localhost");
        System.setProperty(KieServerRouterConstants.ROUTER_PORT, "-1");
    }

    @After
    public void tearDown() {
        System.clearProperty(KieServerRouterConstants.ROUTER_HOST);
        System.clearProperty(KieServerRouterConstants.ROUTER_PORT);
    }

    @Test
    public void testGetLocationUrl() {
        String locationUrl = new KieServerRouterEnvironment().getRouterExternalUrl();
        assertEquals("https://localhost:" + KieServerRouterConstants.DEFAULT_PORT_TLS_NUM, locationUrl);
    }
}

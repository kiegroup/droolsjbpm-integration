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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.router.KieServerRouterConstants;


public class KieServerInfoHandlerTest {

    @Before
    public void setUp() {
        System.setProperty(KieServerRouterConstants.ROUTER_HOST, "localhost");
        System.setProperty(KieServerRouterConstants.ROUTER_PORT, "9000");
    }

    @After
    public void tearDown() {
        System.clearProperty(KieServerRouterConstants.ROUTER_HOST);
        System.clearProperty(KieServerRouterConstants.ROUTER_PORT);
        System.clearProperty(KieServerRouterConstants.ROUTER_EXTERNAL_URL);
    }

    @Test
    public void testGetLocationUrl() {
        String locationUrl = KieServerInfoHandler.getLocationUrl();
        assertEquals("http://localhost:9000/", locationUrl);
    }

    @Test
    public void testGetLocationUrlExternalLocation() {
        System.setProperty(KieServerRouterConstants.ROUTER_EXTERNAL_URL, "https://my-domain:8900/");
        String locationUrl = KieServerInfoHandler.getLocationUrl();
        assertEquals("https://my-domain:8900/", locationUrl);
    }
}

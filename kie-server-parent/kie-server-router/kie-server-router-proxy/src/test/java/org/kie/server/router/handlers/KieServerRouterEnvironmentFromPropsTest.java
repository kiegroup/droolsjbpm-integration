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
import static org.junit.Assert.assertTrue;

public class KieServerRouterEnvironmentFromPropsTest {

    private static final String PROPERTIES_PATH = KieServerRouterEnvironment.class.getClassLoader().getResource("properties/custom.properties").getFile();

    @Before
    public void setUp() {
        System.setProperty(KieServerRouterConstants.ROUTER_CONFIG_FILE, PROPERTIES_PATH);
        //Set some property to check that file values override system properties
        System.setProperty(KieServerRouterConstants.ROUTER_EXTERNAL_URL, "https://my-domain:8900/");
    }

    @After
    public void tearDown() {
        System.clearProperty(KieServerRouterConstants.ROUTER_EXTERNAL_URL);
        System.clearProperty(KieServerRouterConstants.ROUTER_CONFIG_FILE);
    }

    @Test
    public void testRouterEnvironmentFromProperties() {
        KieServerRouterEnvironment env = new KieServerRouterEnvironment();
        assertEquals("my-id", env.getRouterId());
        assertEquals("my-name", env.getRouterName());
        assertEquals("my-host", env.getRouterHost());
        assertEquals(9019, env.getPort());
        assertEquals(19019, env.getSslPort());
        assertEquals("https://my-external-host:9111", env.getRouterExternalUrl());
        
        assertEquals("my-keystore-path", env.getKeystorePath());
        assertEquals("my-password", env.getKeystorePassword());
        assertEquals("my-keyalias", env.getKeystoreKey());
        assertTrue(env.isTlsEnabled());
        
        assertEquals("my-repo", env.getRepositoryDir());
        assertEquals("my-idp", env.getIdentityProvider());
        
        assertEquals("my-controller-url", env.getKieControllerUrl());
        assertEquals("my-controller-user", env.getKieControllerUser());
        assertEquals("my-controller-pwd", env.getKieControllerPwd());
        assertEquals("my-controller-token", env.getKieControllerToken());
        
        assertTrue(env.isConfigFileWatcherEnabled());
        assertEquals(9000, env.getConfigFileWatcherInterval());
        
        assertEquals(9900, env.getKieControllerAttemptInterval());
        assertEquals(9, env.getKieControllerRecoveryAttemptLimit());
        assertTrue(env.isManagementSecured());
    }
}

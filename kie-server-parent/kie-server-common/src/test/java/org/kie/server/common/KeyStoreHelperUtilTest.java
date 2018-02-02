/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.common;

import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;

import static org.junit.Assert.assertEquals;
import static org.kie.server.common.KeyStoreHelperUtil.loadControllerPassword;

public class KeyStoreHelperUtilTest {

    @Test
    public void testDefaultPassword(){
        final String defaultPassword = "default";
        final String password = loadControllerPassword(defaultPassword);
        assertEquals(defaultPassword, password);
    }

    @Test
    public void testConfigDefaultPassword(){
        final KieServerConfig serverConfig = new KieServerConfig();
        final String password = loadControllerPassword(serverConfig);
        assertEquals("kieserver1!", password);
    }

    @Test
    public void testConfigPassword(){
        final KieServerConfig serverConfig = new KieServerConfig();
        final String defaultPassword = "default";
        serverConfig.addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, defaultPassword, null));
        final String password = loadControllerPassword(serverConfig);
        assertEquals(defaultPassword, password);
    }

}

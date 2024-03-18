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

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.drools.core.util.KeyStoreConstants;
import org.drools.core.util.KeyStoreHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.server.common.KeyStoreHelperUtil.loadControllerPassword;

public class KeyStoreHelperUtilTest {

    private static final String KEYSTORE_PATH = "target/keystore.jks";
    private static final String KEYSTORE_PWD = "password";
    private static final String KEYSTORE_KEY_ALIAS = "selfsigned";
    private static final String KEYSTORE_KEY_PWD = "password";

    @BeforeClass
    public static void init() throws Exception {
        File file = new File(KEYSTORE_PATH);
        file.delete();

        // generate self signed certificate
        String[] cmd = { "keytool", "-genkey",
                "-keyalg", "RSA",
                "-alias", KEYSTORE_KEY_ALIAS,
                "-keystore", KEYSTORE_PATH,
                "-storepass", KEYSTORE_PWD,
                "-validity", "360",
                "-keysize", "1024",
                "-keypass", KEYSTORE_KEY_PWD,
                "-dname", "CN=root, OU=root, O=root, L=root, ST=root, C=root"
        };

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmd);
        Process p = builder.start();
        p.waitFor(10, TimeUnit.SECONDS);
    }

    @Test
    public void testKeyPairReading() throws Exception {
        try {
            // this test if we can read our own keys properly
            URI uri = Paths.get(KEYSTORE_PATH).toAbsolutePath().toUri();
            System.setProperty(KeyStoreConstants.PROP_PVT_KS_URL, uri.toURL().toExternalForm());
            System.setProperty(KeyStoreConstants.PROP_PVT_KS_PWD, KEYSTORE_PWD);
            System.setProperty(KeyStoreHelperUtil.PROP_PWD_JWT_ALIAS, KEYSTORE_KEY_ALIAS);
            System.setProperty(KeyStoreHelperUtil.PROP_PWD_JWT_PWD, KEYSTORE_KEY_PWD);

            KeyStoreHelper.reInit();

            assertNotNull(KeyStoreHelperUtil.getJwtKeyPair());

        } finally {
            System.clearProperty(KeyStoreConstants.PROP_PVT_KS_URL);
            System.clearProperty(KeyStoreConstants.PROP_PVT_KS_PWD);
            System.clearProperty(KeyStoreHelperUtil.PROP_PWD_JWT_ALIAS);
            System.clearProperty(KeyStoreHelperUtil.PROP_PWD_JWT_PWD);
        }

    }

    @Test
    public void testDefaultPassword() {
        final String defaultPassword = "default";
        final String password = loadControllerPassword(defaultPassword);
        assertEquals(defaultPassword, password);
    }

    @Test
    public void testConfigDefaultPassword() {
        final KieServerConfig serverConfig = new KieServerConfig();
        final String password = loadControllerPassword(serverConfig);
        assertEquals("kieserver1!", password);
    }

    @Test
    public void testConfigPassword() {
        final KieServerConfig serverConfig = new KieServerConfig();
        final String defaultPassword = "default";
        serverConfig.addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, defaultPassword, null));
        final String password = loadControllerPassword(serverConfig);
        assertEquals(defaultPassword, password);
    }

}

/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.services.impl;

import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;

public class KieServerStateTest {

    @BeforeClass
    public static void setupOnce() {
        System.getProperty(KieServerConstants.KIE_SERVER_STATE_REPO, "./target");
    }

    @AfterClass
    public static void cleanupOnce() {
        System.clearProperty(KieServerConstants.KIE_SERVER_STATE_REPO);
    }

    @After
    public void cleanup() {
        System.clearProperty(KieServerConstants.CFG_PERSISTANCE_DIALECT);
        System.clearProperty(KieServerConstants.CFG_PERSISTANCE_DS);
        System.clearProperty(KieServerConstants.CFG_PERSISTANCE_TM);
    }

    @Test
    public void testLoadKieServerState() {
        KieServerStateFileRepository repository = new KieServerStateFileRepository();

        String serverId = UUID.randomUUID().toString();

        KieServerState state = repository.load(serverId);
        Assert.assertNotNull(state);

        KieServerConfig config = state.getConfiguration();
        Assert.assertNotNull(config);
        Assert.assertNull(config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT));
        Assert.assertNull(config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DS));
        Assert.assertNull(config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_TM));

        System.setProperty(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_DS, "jdbc/jbpm");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_TM, "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");

        repository.store(serverId, state);

        repository = new KieServerStateFileRepository();
        state = repository.load(serverId);
        Assert.assertNotNull(state);

        config = state.getConfiguration();
        Assert.assertNotNull(config);
        Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT));
        Assert.assertEquals("jdbc/jbpm", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DS));
        Assert.assertEquals("org.hibernate.service.jta.platform.internal.BitronixJtaPlatform", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_TM));
    }

    @Test
    public void testLoadKieServerStateWithProperties() {
        KieServerStateFileRepository repository = new KieServerStateFileRepository();

        System.setProperty(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_DS, "jdbc/jbpm");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_TM, "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");

        String serverId = UUID.randomUUID().toString();

        KieServerState state = repository.load(serverId);
        Assert.assertNotNull(state);

        KieServerConfig config = state.getConfiguration();
        Assert.assertNotNull(config);

        Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT));
        Assert.assertEquals("jdbc/jbpm", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DS));
        Assert.assertEquals("org.hibernate.service.jta.platform.internal.BitronixJtaPlatform", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_TM));
    }
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.controller;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.controller.DefaultRestControllerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.common.Assert;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KieControllerValidationIntegrationTest extends KieControllerManagementBaseTest {

    protected static Logger logger = LoggerFactory.getLogger(KieControllerValidationIntegrationTest.class);

    private KieServerStateRepository dummyKieServerStateRepository;

    private KieServerRegistry registry = new KieServerRegistryImpl();

    @Before
    public void init() {

        dummyKieServerStateRepository = new KieServerStateRepository() {

            @Override
            public void store(String serverId, KieServerState kieServerState) {
            }

            @Override
            public KieServerState load(String serverId) {
                KieServerState kieServerState = new KieServerState();
                kieServerState.setControllers(Collections.singleton(TestConfig.getControllerHttpUrl()));
                kieServerState.setConfiguration(new KieServerConfig());

                if (TestConfig.isLocalServer()) {
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_USER, "", null));
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "", null));
                } else {
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_USER, TestConfig.getUsername(), null));
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, TestConfig.getPassword(), null));
                }
                return kieServerState;
            }
        };
        registry = new KieServerRegistryImpl();
        registry.registerStateRepository(dummyKieServerStateRepository);
    }

    @Test
    public void testBadRegistered() throws Exception {
        final String SERVER_TEMPLATE_ID = "test.mode.bad";
        final String SERVER_NAME = "server-name";
        KieServerEnvironment.setServerId(SERVER_TEMPLATE_ID);

        ServerTemplate serverTemplate = buildServerTemplate(SERVER_TEMPLATE_ID,
                                                            SERVER_NAME,
                                                            null,
                                                            KieServerMode.DEVELOPMENT,
                                                            Collections.singletonList(Capability.PROCESS.name()));
        controllerClient.saveServerTemplate(serverTemplate);

        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.getServerTemplates().length);

        // Register kie server in controller.
        KieServerInfo kieServerInfo = new KieServerInfo(SERVER_TEMPLATE_ID, "1.0.0");
        kieServerInfo.setLocation("http://127.0.0.1:20000");
        kieServerInfo.setMode(KieServerMode.PRODUCTION);
        kieServerInfo.setName(SERVER_NAME);

        KieServerController controller = new DefaultRestControllerImpl(registry);
        assertThatThrownBy(() -> controller.connect(kieServerInfo)).isInstanceOf(KieControllerNotConnectedException.class);

        // Check that kie server is not registered.
        ServerInstanceKeyList list = controllerClient.getServerInstances(SERVER_TEMPLATE_ID);
        logger.info("list {}", (Object[]) list.getServerInstanceKeys());
        assertTrue(list.getServerInstanceKeys() == null || list.getServerInstanceKeys().length == 0);

        // clear up
        controller.disconnect(kieServerInfo);
        controllerClient.deleteServerTemplate(SERVER_TEMPLATE_ID);
    }

    @Test
    public void testGoodRegistered() throws Exception {
        final String SERVER_TEMPLATE_ID = "test.mode.ok";
        final String SERVER_NAME = "server-name";
        KieServerEnvironment.setServerId(SERVER_TEMPLATE_ID);

        ServerTemplate serverTemplate = buildServerTemplate(SERVER_TEMPLATE_ID,
                                                            SERVER_NAME,
                                                            null,
                                                            KieServerMode.DEVELOPMENT,
                                                            Collections.singletonList(Capability.PROCESS.name()));
        controllerClient.saveServerTemplate(serverTemplate);

        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.getServerTemplates().length);

        // Register kie server in controller.
        KieServerInfo kieServerInfo = new KieServerInfo(SERVER_TEMPLATE_ID, "1.0.0");
        kieServerInfo.setLocation("http://127.0.0.1:20000");
        kieServerInfo.setMode(KieServerMode.DEVELOPMENT);
        kieServerInfo.setCapabilities(Collections.singletonList(KieServerConstants.CAPABILITY_BPM));
        kieServerInfo.setName(SERVER_NAME);

        KieServerRegistry registry = new KieServerRegistryImpl();

        registry.registerStateRepository(dummyKieServerStateRepository);
        KieServerController controller = new DefaultRestControllerImpl(registry);
        KieServerSetup setup = controller.connect(kieServerInfo);
        Assert.assertTrue(setup.hasNoErrors());

        // Check that kie server is registered.
        ServerInstanceKeyList list = controllerClient.getServerInstances(SERVER_TEMPLATE_ID);

        // Sometimes the controller healthcheck deletes server instance sooner than we retrieve it back, in such case register the instance again
        if (list == null || list.getServerInstanceKeys() == null || list.getServerInstanceKeys().length == 0) {
            setup = controller.connect(kieServerInfo);
            Assert.assertTrue(setup.hasNoErrors());
            list = controllerClient.getServerInstances(SERVER_TEMPLATE_ID);
        }

        assertNotNull(list.getServerInstanceKeys());
        assertEquals(1, list.getServerInstanceKeys().length);

        // clear up
        controller.disconnect(kieServerInfo);
        controllerClient.deleteServerTemplate(SERVER_TEMPLATE_ID);
    }
}

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.events.ContainerSpecUpdated;
import org.kie.server.controller.api.model.events.ServerInstanceConnected;
import org.kie.server.controller.api.model.events.ServerInstanceDeleted;
import org.kie.server.controller.api.model.events.ServerInstanceDisconnected;
import org.kie.server.controller.api.model.events.ServerInstanceUpdated;
import org.kie.server.controller.api.model.events.ServerTemplateDeleted;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.controller.client.event.EventHandler;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.controller.DefaultRestControllerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KieControllerCrashIntegrationTest extends KieControllerManagementBaseTest {

    private CountDownLatch serverUp;

    private CountDownLatch serverDown;
    @Override
    protected KieServicesClient createDefaultClient() {
        // we don't need this
        return null;
    }

    @Before
    @Override
    public void createControllerClient() {
        EventHandler eventHandler = new EventHandler () {
            @Override
            public void onServerInstanceConnected(ServerInstanceConnected serverInstanceConnected) {
                serverUp.countDown();
            }

            @Override
            public void onServerInstanceDeleted(ServerInstanceDeleted serverInstanceDeleted) {}

            @Override
            public void onServerInstanceDisconnected(ServerInstanceDisconnected serverInstanceDisconnected) {
                serverDown.countDown();
            }

            @Override
            public void onServerTemplateDeleted(ServerTemplateDeleted serverTemplateDeleted) {}

            @Override
            public void onServerTemplateUpdated(ServerTemplateUpdated serverTemplateUpdated) {}

            @Override
            public void onServerInstanceUpdated(ServerInstanceUpdated serverInstanceUpdated) {}

            @Override
            public void onContainerSpecUpdated(ContainerSpecUpdated containerSpecUpdated) {}

        };
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                              (String) null,
                                                                              (String) null, 
                                                                              eventHandler);
        } else {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                              TestConfig.getUsername(),
                                                                              TestConfig.getPassword(), 
                                                                              eventHandler);
        }
    }
    
    @Before
    @Override
    public void setup() throws Exception {
        // simulated kie server
        super.setup();
        serverUp = new CountDownLatch(1);
        serverDown = new CountDownLatch(1);
    }

    @After
    public void cleanupEmbeddedKieServer() {
        // simulated test kie-server
    }

    @Test
    public void testCrashAfterRegistered() throws Exception {
        final String SERVER_TEMPLATE_ID = "template-id";
        final String SERVER_NAME = "server-name";
        KieServerEnvironment.setServerId(SERVER_TEMPLATE_ID);

        ServerTemplate serverTemplate = new ServerTemplate(SERVER_TEMPLATE_ID, SERVER_NAME);
        controllerClient.saveServerTemplate(serverTemplate);

        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.getServerTemplates().length);

        // Register kie server in controller.
        KieServerInfo kieServerInfo = new KieServerInfo(SERVER_TEMPLATE_ID, "1.0.0");
        kieServerInfo.setLocation("http://127.0.0.1:20000");
        kieServerInfo.setMode(KieServerMode.PRODUCTION);
        kieServerInfo.setName(SERVER_NAME);

        KieServerRegistry registry = new KieServerRegistryImpl();
        KieServerStateRepository dummyKieServerStateRepository = new KieServerStateRepository() {

            @Override
            public void store(String serverId, KieServerState kieServerState) {}

            @Override
            public KieServerState load(String serverId) {
                KieServerState kieServerState = new KieServerState();
                kieServerState.setControllers(Collections.singleton(TestConfig.getControllerHttpUrl()));
                kieServerState.setConfiguration(new KieServerConfig());

                if(TestConfig.isLocalServer()) {
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_USER, "", null));
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "", null));
                } else {
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_USER, TestConfig.getUsername(), null));
                    kieServerState.getConfiguration().addConfigItem(new KieServerConfigItem(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, TestConfig.getPassword(), null));
                }
                return kieServerState;
            }

        };
        registry.registerStateRepository(dummyKieServerStateRepository);
        KieServerController controller =  new DefaultRestControllerImpl(registry);
        controller.connect(kieServerInfo);
        // Check that kie server is registered.
        serverUp.await(5, TimeUnit.SECONDS);
        ServerInstanceKeyList list = controllerClient.getServerInstances(instanceList.getServerTemplates()[0].getId());
        assertNotNull(list.getServerInstanceKeys());
        assertEquals(1, list.getServerInstanceKeys().length);

        // Check that kie server is deregistered automatically.
        serverDown.await(5, TimeUnit.SECONDS);
        list = controllerClient.getServerInstances(instanceList.getServerTemplates()[0].getId());
        assertNotNull(list.getServerInstanceKeys());
        assertEquals(0, list.getServerInstanceKeys().length);

    }

}

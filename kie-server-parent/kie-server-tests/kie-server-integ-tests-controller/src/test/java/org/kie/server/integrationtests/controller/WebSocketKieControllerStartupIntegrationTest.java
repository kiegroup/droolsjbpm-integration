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

package org.kie.server.integrationtests.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerExecutor;
import org.kie.server.integrationtests.shared.KieServerSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebSocketKieControllerStartupIntegrationTest extends KieControllerManagementBaseTest {

    public static final Logger logger = LoggerFactory.getLogger(WebSocketKieControllerStartupIntegrationTest.class);

    private String origControllerUrl;
    // Need to allocate different port for WebSocket test as using same Kie server URL for both REST and WebSocket tests cause issues (due to client retrieval logic in controller).
    private int kieServerAllocatedPort = allocatePort();

    @Rule
    public TestName testName= new TestName();

    @Override
    protected KieServicesClient createDefaultClient() {
        // For these tests we use embedded kie server as we need to control turning server off and on.
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration("http://localhost:" + kieServerAllocatedPort + "/server", null, null);
        config.setMarshallingFormat(marshallingFormat);
        return KieServicesFactory.newKieServicesClient(config);
    }


    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/stateless-session-kjar");
    }

    @Before
    @Override
    public void setup() throws Exception {
        logger.info("********** Test name " + testName.getMethodName() + " ***********");
        
        // Start embedded kie server to be correctly initialized and cleaned before tests.
        if (!TestConfig.isLocalServer()) {
            server = new KieServerExecutor(kieServerAllocatedPort) {
                
                @Override
                protected void setKieServerProperties(boolean syncWithController) {
                    super.setKieServerProperties(syncWithController);
                    URL controllerUrl;
                    try {
                        origControllerUrl = System.getProperty(KieServerConstants.KIE_SERVER_CONTROLLER);
                        controllerUrl = new URL(origControllerUrl);
                        String controllerContext = TestConfig.getKieServerControllerContext();
                        String wsControllerUrl = "ws://" + controllerUrl.getHost() + ":" + controllerUrl.getPort() + "/" + controllerContext + "/websocket/controller";
                        System.setProperty(KieServerConstants.KIE_SERVER_CONTROLLER, wsControllerUrl);
                       
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid URL for http controller url " + origControllerUrl, e);
                    }
                    
                }
            };
            server.startKieServer();
        }
        super.setup();
    }

    @After
    public void cleanupEmbeddedKieServer() {
        // Turn off embedded kie server if running in container, turn on if running local tests.
        try {
            if (TestConfig.isLocalServer()) {
                server.startKieServer();
            } else {
                server.stopKieServer();
            }
        } catch (Exception e) {
            // Exception thrown if there is already kie server started or stopped respectively.
            // Don't need to do anything in such case.
        }
        if (origControllerUrl != null) {
            System.setProperty(KieServerConstants.KIE_SERVER_CONTROLLER, origControllerUrl);
        }
    }

    @Test(timeout = 60 * 1000)
    public void testRegisterKieServerAfterStartup() throws Exception {
        // Turn off embedded kie server.
        server.stopKieServer();

        // Check that there are no kie servers deployed in controller.
        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertNotNull(instanceList);
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!", instanceList.getServerTemplates());

        // Turn on new kie server.
        server.startKieServer();
        KieServerSynchronization.waitForServerTemplateSynchronization(controllerClient, 1);

        // Check that kie server is registered in controller.
        instanceList = controllerClient.listServerTemplates();
        assertNotNull(instanceList);
        assertEquals(1, instanceList.getServerTemplates().length);

        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        assertNotNull(reply);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        assertNotNull(reply.getResult());

        ServerTemplate deployedServerInstance = instanceList.getServerTemplates()[0];
        assertNotNull(deployedServerInstance);
        assertEquals(reply.getResult().getServerId(), deployedServerInstance.getId());
    }

    @Test(timeout = 60 * 1000)
    public void testTurnOffKieServerAfterShutdown() throws Exception {
        // Register kie server in controller.
        ServiceResponse<KieServerInfo> kieServerInfo = client.getServerInfo();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, kieServerInfo.getType());
        assertNotNull(kieServerInfo.getResult());

        ServerTemplate serverTemplate = new ServerTemplate(kieServerInfo.getResult().getServerId(), kieServerInfo.getResult().getName());
        controllerClient.saveServerTemplate(serverTemplate);

        // Check that kie server is registered.
        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.getServerTemplates().length);
        assertEquals(kieServerInfo.getResult().getServerId(), instanceList.getServerTemplates()[0].getId()); //maybe change to avoid next -> null

        // Turn off embedded kie server.
        server.stopKieServer();
        KieServerSynchronization.waitForServerInstanceSynchronization(controllerClient, serverTemplate.getId(), 0);

        // Check that kie server is down in controller.
        instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.getServerTemplates().length);
        assertEquals(kieServerInfo.getResult().getServerId(), instanceList.getServerTemplates()[0].getId()); //maybe change to avoid next -> null
    }

    @Test(timeout = 60 * 1000)
    public void testContainerCreatedAfterStartup() throws Exception {
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> kieServerInfo = client.getServerInfo();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, kieServerInfo.getType());
        assertNotNull(kieServerInfo.getResult());

        // Check that there are no containers in kie server.
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        KieServerAssert.assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());

        // Check that there are no kie servers deployed in controller.
        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!", instanceList.getServerTemplates());

        // Turn kie server off, add embedded kie server to controller, create container and start kie server again.
        server.stopKieServer();

        ServerTemplate serverTemplate = new ServerTemplate(kieServerInfo.getResult().getServerId(), kieServerInfo.getResult().getName());
        controllerClient.saveServerTemplate(serverTemplate);

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_ID, serverTemplate, RELEASE_ID, KieContainerStatus.STOPPED, new HashMap<Capability, ContainerConfig>());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        ContainerSpec deployedContainer = controllerClient.getContainerInfo(kieServerInfo.getResult().getServerId(), CONTAINER_ID);

        assertNotNull(deployedContainer);
        assertEquals(CONTAINER_ID, deployedContainer.getId());
        assertEquals(RELEASE_ID, deployedContainer.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, deployedContainer.getStatus());

        controllerClient.startContainer(containerSpec);

        server.startKieServer(true);

        // Check that container is deployed on kie server.
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertNotNull(containerInfo.getResult());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
    }

    private static int allocatePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            return 9786;
        }
    }
}

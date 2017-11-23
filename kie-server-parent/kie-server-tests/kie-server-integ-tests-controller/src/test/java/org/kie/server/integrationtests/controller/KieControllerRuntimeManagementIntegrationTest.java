/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.server.integrationtests.controller;

import org.assertj.core.api.SoftAssertions;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class KieControllerRuntimeManagementIntegrationTest extends KieControllerManagementBaseTest {

    private KieServerInfo kieServerInfo;

    protected void assertNotFoundException(KieServerControllerHTTPClientException e) {
        assertEquals(404,
                e.getResponseCode());
        assertNotNull(e.getMessage());
    }

    protected void assertBadRequestException(KieServerControllerHTTPClientException e) {
        assertEquals(400, e.getResponseCode());
        assertNotNull(e.getMessage());
    }

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(RELEASE_ID);

    }

    @Before
    public void getKieServerInfo() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        KieServerAssert.assertSuccess(reply);
        kieServerInfo = reply.getResult();
    }

    @Test
    public void testGetServerInstances() {
        // Create kie server template connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        ServerInstanceKeyList serverInstances = controllerClient.getServerInstances(serverTemplate.getId());
        assertNotNull(serverInstances);
        assertEquals(1, serverInstances.getServerInstanceKeys().length);
        ServerInstanceKey serverInstance = serverInstances.getServerInstanceKeys()[0];
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(serverInstance.getServerName()).isEqualTo(kieServerInfo.getName());
            softly.assertThat(serverInstance.getServerTemplateId()).isEqualTo(serverTemplate.getId());
            softly.assertThat(serverInstance.getUrl()).isEqualTo(kieServerInfo.getLocation());
        });
    }

    @Test
    public void testGetServerInstancesFromNotExistingServerTemplate() {
        try {
            controllerClient.getServerInstances("not-existing");
            fail("Should throw exception about server template not existing.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testGetContainers() throws Exception {
        // Create kie server template connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server template.
        createContainerSpec(serverTemplate, RELEASE_ID, KieContainerStatus.STARTED);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        ServerInstanceKeyList serverInstances = controllerClient.getServerInstances(serverTemplate.getId());
        ServerInstanceKey serverInstance = serverInstances.getServerInstanceKeys()[0];

        ContainerList containers = controllerClient.getContainers(serverInstance);
        assertNotNull(containers);
        assertEquals(1, containers.getContainers().length);
        Container container = containers.getContainers()[0];
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(container.getContainerSpecId()).isEqualTo(CONTAINER_ID);
            softly.assertThat(container.getContainerName()).isEqualTo(CONTAINER_NAME);
            softly.assertThat(container.getResolvedReleasedId()).isEqualTo(RELEASE_ID);
            softly.assertThat(container.getServerInstanceId()).isEqualTo(serverInstance.getServerInstanceId());
            softly.assertThat(container.getServerTemplateId()).isEqualTo(serverTemplate.getId());
            softly.assertThat(container.getStatus()).isEqualTo(KieContainerStatus.STARTED);
        });
    }

    @Test
    public void testGetContainersFromNotExistingServerInstance() {
        ServerInstanceKey serverInstance = new ServerInstanceKey("not-existing", "not-existing", "not-existing", "not-existing");
        try {
            controllerClient.getContainers(serverInstance);
            fail("Should throw exception about the server instance not existing.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);

        }
    }

    protected ServerTemplate createServerTemplate() {
        return createServerTemplate(kieServerInfo.getServerId(), kieServerInfo.getName(), kieServerInfo.getLocation());
    }

}

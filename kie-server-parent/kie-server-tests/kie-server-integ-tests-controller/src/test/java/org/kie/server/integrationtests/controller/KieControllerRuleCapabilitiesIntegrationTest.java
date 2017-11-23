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
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class KieControllerRuleCapabilitiesIntegrationTest extends KieControllerManagementBaseTest {

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

    @After
    public void removeNewContainer() {
        KieServerDeployer.removeLocalArtifact(RELEASE_ID_101);
    }

    @Test
    public void testScanNow() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = startContainerWithLatestVersion(serverTemplate);

        checkKieContainerResourceLatest(RELEASE_ID);

        KieServerDeployer.createAndDeployKJar(RELEASE_ID_101);
        controllerClient.scanNow(container);

        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        checkKieContainerResourceLatest(RELEASE_ID_101);

    }

    @Test
    public void testScanNowNotExistingContainer() {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID);
        try {
            controllerClient.scanNow(container);
            fail("Should throw exception about container not found.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testStartAndStopScanner() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = startContainerWithLatestVersion(serverTemplate);

        checkKieContainerResourceLatest(RELEASE_ID);

        controllerClient.startScanner(container, 200L);
        KieServerDeployer.createAndDeployKJar(RELEASE_ID_101);
        Thread.sleep(500);
        controllerClient.stopScanner(container);

        checkKieContainerResourceLatest(RELEASE_ID_101);
    }

    @Test
    public void testStartScannerWithNegativeInterval() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = startContainerWithLatestVersion(serverTemplate);

        checkKieContainerResourceLatest(RELEASE_ID);

        KieServerDeployer.createAndDeployKJar(RELEASE_ID_101);
        try {
            controllerClient.startScanner(container, -200L);
            fail("Should throw exception about wrong parameter.");
        } catch (KieServerControllerClientException e) {
            assertBadRequestException((KieServerControllerHTTPClientException) e);
        }

        // Check that contianer is not upgraded by scanner
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        checkKieContainerResourceLatest(RELEASE_ID);
    }

    @Test
    public void testStartScannerNotExisitngContainer() {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID_LATEST);
        try {
            controllerClient.startScanner(container, 1000L);
            fail("Should throw exception about container not found.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testStopScannerNotExisitngContainer() {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID_LATEST);
        try {
            controllerClient.stopScanner(container);
            fail("Should throw exception about container not found.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testStopNotRunningScanner() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = startContainerWithLatestVersion(serverTemplate);

        checkKieContainerResourceLatest(RELEASE_ID);

        try {
            controllerClient.stopScanner(container);
            fail("Should throw exception about wrong operation.");
        } catch (KieServerControllerClientException e) {
            assertBadRequestException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testUpgradeContainer() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID, KieContainerStatus.STARTED);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        checkKieContainerResource(RELEASE_ID, RELEASE_ID);

        controllerClient.upgradeContainer(container, RELEASE_ID_101);

        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        checkKieContainerResource(RELEASE_ID_101, RELEASE_ID_101);
    }

    @Test
    public void testUpgradeNotExisitngContainer() {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID_LATEST);
        try {
            controllerClient.upgradeContainer(container, RELEASE_ID_101);
            fail("Should throw exception about container not found.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((KieServerControllerHTTPClientException) e);
        }
    }

    @Test
    public void testUpgradeContainerWithNotExistingReleaseId() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID, KieContainerStatus.STARTED);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        checkKieContainerResource(RELEASE_ID, RELEASE_ID);

        ReleaseId notExistingVersion = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "6.6.6");
        controllerClient.upgradeContainer(container, notExistingVersion);

        ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerResponse.getType());
        KieContainerResource containerResource = containerResponse.getResult();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(containerResource.getContainerId()).isEqualTo(CONTAINER_ID);
            softly.assertThat(containerResource.getStatus()).isEqualTo(KieContainerStatus.FAILED);
            softly.assertThat(containerResource.getReleaseId()).isEqualTo(notExistingVersion);
        });
    }

    @Test
    public void testUpgradeContainerToLatestVersion() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID, KieContainerStatus.STARTED);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        checkKieContainerResource(RELEASE_ID, RELEASE_ID);

        controllerClient.upgradeContainer(container, RELEASE_ID_LATEST);

        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        checkKieContainerResourceLatest(RELEASE_ID_101);
    }

    private ContainerSpec startContainerWithLatestVersion(ServerTemplate serverTemplate) throws Exception {
        ContainerSpec container = createContainerSpec(serverTemplate, RELEASE_ID);
        controllerClient.startContainer(container);

        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        container.setReleasedId(RELEASE_ID_LATEST);
        controllerClient.updateContainerSpec(serverTemplate.getId(), CONTAINER_ID, container);

        return container;
    }

    protected ServerTemplate createServerTemplate() {
        return createServerTemplate(kieServerInfo.getServerId(), kieServerInfo.getName(), kieServerInfo.getLocation());
    }

    protected void checkKieContainerResourceLatest(ReleaseId expectedResolvedReleaseId) {
        checkKieContainerResource(RELEASE_ID_LATEST, expectedResolvedReleaseId);
    }

    protected void checkKieContainerResource(ReleaseId expectedReleaseId, ReleaseId expectedResolvedReleaseId) {
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        KieContainerResource container = containerInfo.getResult();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(container.getContainerId()).isEqualTo(CONTAINER_ID);
            softly.assertThat(container.getStatus()).isEqualTo(KieContainerStatus.STARTED);
            softly.assertThat(container.getReleaseId()).isEqualTo(expectedReleaseId);
            softly.assertThat(container.getResolvedReleaseId()).isEqualTo(expectedResolvedReleaseId);
        });
    }
}

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

package org.kie.server.services.impl;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class KieServerImplTest {

    private static final File REPOSITORY_DIR = new File("target/repository-dir");
    private static final String KIE_SERVER_ID = "kie-server-impl-test";
    private static final String GROUP_ID = "org.kie.server.test";
    private static final String DEFAULT_VERSION = "1.0.0.Final";

    private KieServerImpl kieServer;
    private org.kie.api.builder.ReleaseId releaseId;

    @Before
    public void setupKieServerImpl() throws Exception {
        System.setProperty("org.kie.server.id", KIE_SERVER_ID);
        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR));
    }

    @After
    public void cleanUp() {
        if (kieServer != null) {
            kieServer.destroy();
        }
    }

    @Test
    // https://issues.jboss.org/browse/RHBPMS-4087
    public void testPersistScannerState() {
        String containerId = "persist-scanner-state";
        createEmptyKjar(containerId);
        // create the container and update the scanner
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, new ReleaseId(releaseId));
        kieServer.createContainer(containerId, kieContainerResource);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieServer.updateScanner(containerId, kieScannerResource);

        KieServerStateRepository stateRepository = new KieServerStateFileRepository(REPOSITORY_DIR);
        KieServerState state = stateRepository.load(KIE_SERVER_ID);
        Set<KieContainerResource> containers = state.getContainers();
        Assertions.assertThat(containers).hasSize(1);
        KieContainerResource container = containers.iterator().next();
        Assertions.assertThat(container.getScanner()).isEqualTo(kieScannerResource);

        KieScannerResource updatedKieScannerResource = new KieScannerResource(KieScannerStatus.DISPOSED);
        kieServer.updateScanner(containerId, updatedKieScannerResource);

        // create new state repository instance to avoid caching via 'knownStates'
        // this simulates the server restart (since the status is loaded from filesystem after restart)
        stateRepository = new KieServerStateFileRepository(REPOSITORY_DIR);
        state = stateRepository.load(KIE_SERVER_ID);
        containers = state.getContainers();
        Assertions.assertThat(containers).hasSize(1);
        container = containers.iterator().next();
        Assertions.assertThat(container.getScanner()).isEqualTo(updatedKieScannerResource);
    }

    @Test
    // https://issues.jboss.org/browse/JBPM-5288
    public void testCreateScannerWhenCreatingContainer() {
        String containerId = "scanner-state-when-creating-container";
        createEmptyKjar(containerId);

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, new ReleaseId(releaseId));
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieContainerResource.setScanner(kieScannerResource);
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, kieContainerResource);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);
        Assertions.assertThat(createResponse.getResult().getScanner()).isEqualTo(kieScannerResource);

        ServiceResponse<KieContainerResource> getResponse = kieServer.getContainerInfo(containerId);
        Assertions.assertThat(getResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);
        Assertions.assertThat(getResponse.getResult().getScanner()).isEqualTo(kieScannerResource);
    }

    @Test
    public void testReturnRefreshedResolvedReleaseIdAfterUpdateFromScanner() throws Exception {
        // first create a simple container with fixed kjar version
        String artifactId = "refreshed-resource-test";
        String version1 = "1.0.0.Final";
        createEmptyKjar(artifactId, version1);
        ReleaseId releaseId1 = new ReleaseId(GROUP_ID, artifactId, version1);
        String containerId = "refreshed-resource-container";
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, new KieContainerResource(containerId, releaseId1));
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);
        Assertions.assertThat(createResponse.getResult().getReleaseId()).isEqualTo(releaseId1);
        Assertions.assertThat(createResponse.getResult().getResolvedReleaseId()).isEqualTo(releaseId1);
        // now update the version to LATEST and start the scanner
        ReleaseId latestReleaseId = new ReleaseId(GROUP_ID, artifactId, "LATEST");
        kieServer.updateContainerReleaseId(containerId, latestReleaseId);
        kieServer.updateScanner(containerId, new KieScannerResource(KieScannerStatus.STARTED, 200L));
        // deploy newer version of the kjar; it should get picked up by the scanner
        String version2 = "1.0.1.Final";
        createEmptyKjar(artifactId, version2);
        // the scanner operation is async, so it will take some time until the releaseId gets updated
        assertReleaseIds(containerId, latestReleaseId, new ReleaseId(GROUP_ID, artifactId, version2), 10000L);
    }

    @Test
    public void testExecutorPropertiesInStateRepository() {
        KieServerStateFileRepository stateRepository = new KieServerStateFileRepository(REPOSITORY_DIR);
        KieServerState state = stateRepository.load(KIE_SERVER_ID);

        String executorInterval = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_INTERVAL);
        String executorRetries = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_RETRIES);
        String executorPool = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_POOL);
        String executorTimeUnit = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_TIME_UNIT);
        String executorJMSQueue = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_JMS_QUEUE);
        String executorDisabled = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_DISABLED);

        assertNull(executorInterval);
        assertNull(executorRetries);
        assertNull(executorPool);
        assertNull(executorTimeUnit);
        assertNull(executorJMSQueue);
        assertNull(executorDisabled);
        try {
            System.setProperty(KieServerConstants.CFG_EXECUTOR_INTERVAL, "4");
            System.setProperty(KieServerConstants.CFG_EXECUTOR_RETRIES, "7");
            System.setProperty(KieServerConstants.CFG_EXECUTOR_POOL, "11");
            System.setProperty(KieServerConstants.CFG_EXECUTOR_TIME_UNIT, "HOURS");
            System.setProperty(KieServerConstants.CFG_EXECUTOR_JMS_QUEUE, "queue/MY.OWN.QUEUE");
            System.setProperty(KieServerConstants.CFG_EXECUTOR_DISABLED, "true");

            stateRepository.clearCache();

            state = stateRepository.load(KIE_SERVER_ID);

            executorInterval = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_INTERVAL);
            executorRetries = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_RETRIES);
            executorPool = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_POOL);
            executorTimeUnit = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_TIME_UNIT);
            executorJMSQueue = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_JMS_QUEUE);
            executorDisabled = state.getConfiguration().getConfigItemValue(KieServerConstants.CFG_EXECUTOR_DISABLED);

            assertNotNull(executorInterval);
            assertNotNull(executorRetries);
            assertNotNull(executorPool);
            assertNotNull(executorTimeUnit);
            assertNotNull(executorJMSQueue);
            assertNotNull(executorDisabled);

            assertEquals("4", executorInterval);
            assertEquals("7", executorRetries);
            assertEquals("11", executorPool);
            assertEquals("HOURS", executorTimeUnit);
            assertEquals("queue/MY.OWN.QUEUE", executorJMSQueue);
            assertEquals("true", executorDisabled);
        } finally {
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_INTERVAL);
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_RETRIES);
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_POOL);
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_TIME_UNIT);
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_JMS_QUEUE);
            System.clearProperty(KieServerConstants.CFG_EXECUTOR_DISABLED);
        }
    }

    private void assertReleaseIds(String containerId, ReleaseId configuredReleaseId, ReleaseId resolvedReleaseId, long timeoutMillis) throws InterruptedException {
        long timeSpentWaiting = 0;
        while (timeSpentWaiting < timeoutMillis) {
            ServiceResponse<KieContainerResourceList> listResponse = kieServer.listContainers(KieContainerResourceFilter.ACCEPT_ALL);
            Assertions.assertThat(listResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);
            List<KieContainerResource> containers = listResponse.getResult().getContainers();
            for (KieContainerResource container : containers) {
                if (configuredReleaseId.equals(container.getReleaseId())
                        && resolvedReleaseId.equals(container.getResolvedReleaseId())) {
                    return;
                }
            }
            Thread.sleep(200);
            timeSpentWaiting += 200L;
        }
        Assertions.fail("Waiting too long for container " + containerId + " to have expected releaseIds updated! " +
                "expected: releaseId=" + configuredReleaseId + ", resolvedReleaseId=" + resolvedReleaseId);
    }


    private void createEmptyKjar(String artifactId) {
        createEmptyKjar(artifactId, DEFAULT_VERSION);
    }
    private void createEmptyKjar(String artifactId, String version) {
        // create empty kjar; content does not matter
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        releaseId = kieServices.newReleaseId(GROUP_ID, artifactId, version);
        KieModule kieModule = kieServices.newKieBuilder( kfs ).buildAll().getKieModule();
        MavenRepository.getMavenRepository().installArtifact(releaseId, (InternalKieModule)kieModule, createPomFile(artifactId, version));
        kieServices.getRepository().addKieModule(kieModule);
    }

    private File createPomFile(String artifactId, String version) {
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>org.kie.server.test</groupId>\n" +
                "  <artifactId>" + artifactId + "</artifactId>\n" +
                "  <version>" + version + "</version>\n" +
                "  <packaging>pom</packaging>\n" +
                "</project>";
        try {
            File file = new File("target/" + artifactId + "-1.0.0.Final.pom");
            FileUtils.write(file, pomContent);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

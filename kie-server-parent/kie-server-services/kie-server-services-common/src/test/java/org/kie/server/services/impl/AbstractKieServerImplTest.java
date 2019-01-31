/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.scanner.KieMavenRepository;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.Severity;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.controller.DefaultRestControllerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractKieServerImplTest {

    static final File REPOSITORY_DIR = new File("target/repository-dir");
    static final String KIE_SERVER_ID = "kie-server-impl-test";
    static final String GROUP_ID = "org.kie.server.test";
    static final String REGULAR_MODE_VERSION = "1.0.0.Final";
    static final String DEVELOPMENT_MODE_VERSION = "1.0.0-SNAPSHOT";

    protected KieServerMode mode;
    protected String testVersion;

    protected KieServerImpl kieServer;
    protected org.kie.api.builder.ReleaseId releaseId;
    protected String origServerId = null;

    protected List<KieServerExtension> extensions;

    abstract KieServerMode getTestMode();

    @Before
    public void setupKieServerImpl() throws Exception {
        extensions = new ArrayList<>();
        mode = getTestMode();

        testVersion = getVersion(mode);

        origServerId = KieServerEnvironment.getServerId();
        System.setProperty(KieServerConstants.KIE_SERVER_MODE, mode.name());
        System.setProperty(KieServerConstants.KIE_SERVER_ID, KIE_SERVER_ID);
        KieServerEnvironment.setServerId(KIE_SERVER_ID);


        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {
            @Override
            public List<KieServerExtension> getServerExtensions() {
                return extensions;
            }
        };
        kieServer.init();
    }

    private String getVersion(KieServerMode mode) {
        return mode.equals(KieServerMode.DEVELOPMENT) ? DEVELOPMENT_MODE_VERSION : REGULAR_MODE_VERSION;
    }

    @After
    public void cleanUp() {
        if (kieServer != null) {
            kieServer.destroy();
        }
        KieServerEnvironment.setServerId(origServerId);
    }

    @Test
    public void testCheckMode() {
        assertSame(mode, kieServer.getInfo().getResult().getMode());
    }

    @Test
    public void testReadinessCheck() {
        assertTrue(kieServer.isKieServerReady());
    }

    @Test(timeout=10000)
    public void testReadinessCheckDelayedStart() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch startedlatch = new CountDownLatch(1);
        kieServer.destroy();
        kieServer = delayedKieServer(latch, startedlatch);

        assertFalse(kieServer.isKieServerReady());
        latch.countDown();

        startedlatch.await();
        assertTrue(kieServer.isKieServerReady());
    }

    @Test
    public void testHealthCheck() {

        List<Message> healthMessages = kieServer.healthCheck(false);

        assertEquals(healthMessages.size(), 0);
    }

    @Test
    public void testHealthCheckWithReport() {

        List<Message> healthMessages = kieServer.healthCheck(true);

        assertEquals(healthMessages.size(), 2);
        Message header = healthMessages.get(0);
        assertEquals(Severity.INFO, header.getSeverity());
        assertEquals(2, header.getMessages().size());

        Message footer = healthMessages.get(1);
        assertEquals(Severity.INFO, footer.getSeverity());
        assertEquals(1, footer.getMessages().size());
    }

    @Test(timeout=10000)
    public void testHealthCheckDelayedStart() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch startedlatch = new CountDownLatch(1);
        kieServer.destroy();
        kieServer = delayedKieServer(latch, startedlatch);

        assertFalse(kieServer.isKieServerReady());

        List<Message> healthMessages = kieServer.healthCheck(false);
        assertEquals(healthMessages.size(), 1);

        Message notReady = healthMessages.get(0);
        assertEquals(Severity.ERROR, notReady.getSeverity());
        assertEquals(1, notReady.getMessages().size());

        latch.countDown();
        startedlatch.await();
        assertTrue(kieServer.isKieServerReady());

        healthMessages = kieServer.healthCheck(false);

        assertEquals(healthMessages.size(), 0);
    }

    @Test
    public void testHealthCheckFailedContainer() {
        kieServer.destroy();
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {

            @Override
            protected List<KieContainerInstanceImpl> getContainers() {
                List<KieContainerInstanceImpl> containers = new ArrayList<>();
                KieContainerInstanceImpl container = new KieContainerInstanceImpl("test", KieContainerStatus.FAILED);
                containers.add(container);
                return containers;
            }

        };
        kieServer.init();
        List<Message> healthMessages = kieServer.healthCheck(false);

        assertEquals(healthMessages.size(), 1);
        Message failedContainer = healthMessages.get(0);
        assertEquals(Severity.ERROR, failedContainer.getSeverity());
        assertEquals(1, failedContainer.getMessages().size());
        assertEquals("KIE Container 'test' is in FAILED state", failedContainer.getMessages().iterator().next());
    }

    @Test
    public void testHealthCheckFailedExtension() {
        extensions.add(new KieServerExtension() {

            @Override
            public List<Message> healthCheck(boolean report) {
                List<Message> messages = KieServerExtension.super.healthCheck(report);
                messages.add(new Message(Severity.ERROR, "TEST extension is unhealthy"));
                return messages;
            }

            @Override
            public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
            }

            @Override
            public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
                return false;
            }

            @Override
            public boolean isInitialized() {
                return true;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public void init(KieServerImpl kieServer, KieServerRegistry registry) {
            }

            @Override
            public Integer getStartOrder() {
                return 10;
            }

            @Override
            public List<Object> getServices() {
                return null;
            }

            @Override
            public String getImplementedCapability() {
                return "TEST";
            }

            @Override
            public String getExtensionName() {
                return "TEST";
            }

            @Override
            public <T> T getAppComponents(Class<T> serviceType) {
                return null;
            }

            @Override
            public List<Object> getAppComponents(SupportedTransports type) {
                return null;
            }

            @Override
            public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
            }

            @Override
            public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
            }

            @Override
            public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
            }
        });

        kieServer.init();
        List<Message> healthMessages = kieServer.healthCheck(false);

        assertEquals(healthMessages.size(), 1);
        Message failedContainer = healthMessages.get(0);
        assertEquals(Severity.ERROR, failedContainer.getSeverity());
        assertEquals(1, failedContainer.getMessages().size());
        assertEquals("TEST extension is unhealthy", failedContainer.getMessages().iterator().next());
    }

    @Test
    public void testManagementDisabledDefault() {

        assertNull(kieServer.checkAccessability());
    }

    @Test
    public void testManagementDisabledConfigured() {
        System.setProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED, "true");
        try {
            kieServer.destroy();
            kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR));
            kieServer.init();
            ServiceResponse<?> forbidden = kieServer.checkAccessability();
            assertForbiddenResponse(forbidden);
        } finally {
            System.clearProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED);
        }
    }

    @Test
    public void testManagementDisabledConfiguredViaCommandService() {
        System.setProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED, "true");
        try {
            kieServer.destroy();
            kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR));
            kieServer.init();

            KieContainerCommandServiceImpl commandService = new KieContainerCommandServiceImpl(kieServer, kieServer.getServerRegistry());
            List<KieServerCommand> commands = new ArrayList<>();

            commands.add(new CreateContainerCommand());
            commands.add(new DisposeContainerCommand());
            commands.add(new UpdateScannerCommand());
            commands.add(new UpdateReleaseIdCommand());

            CommandScript commandScript = new CommandScript(commands);
            ServiceResponsesList responseList = commandService.executeScript(commandScript, MarshallingFormat.JAXB, null);
            assertNotNull(responseList);

            List<ServiceResponse<?>> responses = responseList.getResponses();
            assertEquals(4, responses.size());

            for (ServiceResponse<?> forbidden : responses) {

                assertForbiddenResponse(forbidden);
            }

        } finally {
            System.clearProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED);
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
        kieServer.disposeContainer(containerId);
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
        kieServer.disposeContainer(containerId);
    }

    @Test
    public void testCreateContainerValidationNullContainer() {
        String containerId = "container-to-create";

        createEmptyKjar(containerId);

        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, null);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
    }

    @Test
    public void testCreateContainerValidationNullRelease() {
        String containerId = "container-to-create";

        createEmptyKjar(containerId);

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, null);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieContainerResource.setScanner(kieScannerResource);
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, kieContainerResource);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
    }

    @Test
    public void testCreateContainerValidationModeConflict() {
        String containerId = "container-to-create";

        createEmptyKjar(containerId);

        ReleaseId testReleaseId = new ReleaseId(GROUP_ID, containerId, getVersion(mode.equals(KieServerMode.DEVELOPMENT) ? KieServerMode.REGULAR : KieServerMode.DEVELOPMENT));

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, testReleaseId);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieContainerResource.setScanner(kieScannerResource);
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, kieContainerResource);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
    }

    @Test
    public void testUpdateContainer() {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(true);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId);

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, new ReleaseId(releaseId), true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        verify(extension).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }

    @Test
    public void testUpdateContainerWithExtensionNotAllowing() {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(false);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId);

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, new ReleaseId(this.releaseId), true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);

        verify(extension).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension, never()).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }

    @Test
    public void testUpdateContainerWithModeConflict() {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(false);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId);

        ReleaseId updateReleaseId = new ReleaseId(GROUP_ID, containerId, getVersion(mode.equals(KieServerMode.DEVELOPMENT) ? KieServerMode.REGULAR : KieServerMode.DEVELOPMENT));

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, updateReleaseId, true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);

        verify(extension, never()).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension, never()).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }

    @Test
    public void testUpdateContainerWithNullReleaseID() {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(false);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId);

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, null, true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);

        verify(extension, never()).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension, never()).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }

    private void startContainerToUpdate(String containerId) {
        createEmptyKjar(containerId);

        ReleaseId releaseId = new ReleaseId(this.releaseId);

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, releaseId);
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

    protected KieServerImpl delayedKieServer(CountDownLatch latch, CountDownLatch startedlatch) {
        KieServerImpl server = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {

            @Override
            public void markAsReady() {
                super.markAsReady();
                startedlatch.countDown();
            }

            @Override
            protected KieServerController getController() {
                return new DefaultRestControllerImpl(getServerRegistry()) {
                    @Override
                    public KieServerSetup connect(KieServerInfo serverInfo) {
                        try {
                            if (latch.await(10, TimeUnit.MILLISECONDS)) {
                                return new KieServerSetup();
                            }
                            throw new KieControllerNotConnectedException("Unable to connect to any controller");
                        } catch (InterruptedException e) {
                            throw new KieControllerNotConnectedException("Unable to connect to any controller");
                        }
                    }

                };
            }

        };
        server.init();
        return server;
    }

    protected void assertForbiddenResponse(ServiceResponse<?> forbidden) {
        assertNotNull(forbidden);

        assertEquals(KieServiceResponse.ResponseType.FAILURE, forbidden.getType());
        assertEquals("KIE Server management api is disabled", forbidden.getMsg());
    }

    protected void assertReleaseIds(String containerId, ReleaseId configuredReleaseId, ReleaseId resolvedReleaseId, long timeoutMillis) throws InterruptedException {
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


    protected void createEmptyKjar(String artifactId) {
        createEmptyKjar(artifactId, testVersion);
    }

    protected void createEmptyKjar(String artifactId, String version) {
        // create empty kjar; content does not matter
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        releaseId = kieServices.newReleaseId(GROUP_ID, artifactId, version);
        KieModule kieModule = kieServices.newKieBuilder(kfs ).buildAll().getKieModule();
        KieMavenRepository.getKieMavenRepository().installArtifact(releaseId, (InternalKieModule)kieModule, createPomFile(artifactId, version ) );
        kieServices.getRepository().addKieModule(kieModule);
    }

    protected File createPomFile(String artifactId, String version) {
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

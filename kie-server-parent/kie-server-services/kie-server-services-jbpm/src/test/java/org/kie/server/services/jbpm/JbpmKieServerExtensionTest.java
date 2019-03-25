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

package org.kie.server.services.jbpm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.appformer.maven.support.DependencyFilter;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.drools.core.impl.InternalKieContainer;
import org.jbpm.kie.services.impl.DeployedUnitImpl;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.scanner.KieMavenRepository;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JbpmKieServerExtensionTest {

    private static final String RESOURCES = "src/main/resources/";

    private static final String CONTAINER_ID = "my-container";

    private static final String GROUP_ID = "org.kie.server.test";
    private static final String ARTIFACT_ID = "my-test-artifact";
    private static final String VERSION = "1.0.0.Final";
    private static final String VERSION_SNAPSHOT = "1.0.0-SNAPSHOT";

    private boolean deployed = false;

    @Captor
    private ArgumentCaptor<Function<DeploymentUnit, Boolean>> beforeUndeployCaptor;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private RuntimeDataService runtimeDataService;

    @Mock
    private QueryService queryService;

    @Mock
    private KieServerImpl kieServer;

    @Mock
    private KieServerInfo kieServerInfo;

    private KieServerMode mode;

    private List<ProcessInstanceDesc> activeProcessInstances = new ArrayList<>();

    private KieServicesImpl kieServices;

    private KieServerRegistry context;

    private JbpmKieServerExtension extension;

    private InternalKieContainer kieContainer;

    private DeploymentUnit deploymentUnit;

    private DeployedUnitImpl deployedUnit;

    private RuntimeManager runimeManager;
    private RuntimeEngine engine;
    private KieSession session;

    @Before
    public void init() {
        KieServerEnvironment.setServerId(UUID.randomUUID().toString());

        context = new KieServerRegistryImpl();
        context.registerStateRepository(new KieServerStateFileRepository(new File("target")));

        kieServices = (KieServicesImpl) KieServices.Factory.get();

        when(kieServerInfo.getMode()).thenAnswer(invocationOnMock -> mode);

        when(kieServer.getInfo()).thenReturn(new ServiceResponse<KieServerInfo>(KieServiceResponse.ResponseType.SUCCESS, "", kieServerInfo));

        extension = new JbpmKieServerExtension() {
            {
                this.deploymentService = JbpmKieServerExtensionTest.this.deploymentService;
                this.runtimeDataService = JbpmKieServerExtensionTest.this.runtimeDataService;
                this.kieServer = JbpmKieServerExtensionTest.this.kieServer;
            }
        };

        when(deploymentService.isDeployed(anyString())).thenAnswer((Answer<Boolean>) invocation -> deployed);
        doAnswer(invocation -> {
            deployed = false;
            return null;
        }).when(deploymentService).undeploy(any(), any());
        doAnswer(invocation -> {
            deployed = false;
            return null;
        }).when(deploymentService).undeploy(any());
        doAnswer((Answer<Void>) invocation -> {
            deploymentUnit = (DeploymentUnit) invocation.getArguments()[0];
            deployed = true;
            return null;
        }).when(deploymentService).deploy(any());

        when(deploymentService.getDeployedUnit(anyString())).thenAnswer((Answer<DeployedUnit>) invocation -> {
            deployedUnit = new DeployedUnitImpl(deploymentUnit);
            runimeManager = mock(RuntimeManager.class);
            engine = mock(RuntimeEngine.class);
            when(runimeManager.getRuntimeEngine(any())).thenReturn(engine);
            session = mock(KieSession.class);
            when(engine.getKieSession()).thenReturn(session);
            deployedUnit.setRuntimeManager(runimeManager);
            return deployedUnit;
        });
        extension.setQueryService(queryService);
        extension.setContext(context);
        when(runtimeDataService.getProcessInstancesByDeploymentId(anyString(), anyList(), any())).thenReturn(activeProcessInstances);
    }

    @After
    public void clear() {
        kieServices.nullAllContainerIds();
    }

    @Test
    public void testCreateContainer() throws IOException {
        testDeployContainer(VERSION);
    }

    @Test
    public void testCreateSNAPSHOTContainer() throws IOException {
        testDeployContainer(VERSION_SNAPSHOT);
    }

    @Test
    public void testDisposePRODUCTIONContainer() throws IOException {
        testDispose(KieServerMode.PRODUCTION);
    }

    @Test
    public void testDisposeSNAPSHOTContainer() throws IOException {
        testDispose(KieServerMode.DEVELOPMENT);
    }

    private void testDispose(KieServerMode mode) throws IOException {
        this.mode = mode;

        String version;

        if(mode.equals(KieServerMode.DEVELOPMENT)) {
            version = VERSION_SNAPSHOT;
        } else {
            version = VERSION;
        }

        testDeployContainer(version);

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(new ReleaseId(GROUP_ID, ARTIFACT_ID, version), DependencyFilter.COMPILE_FILTER);
        List<Message> messages = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA, metaData);
        params.put(KieServerConstants.KIE_SERVER_PARAM_MESSAGES, messages);
        params.put(KieServerConstants.KIE_SERVER_PARAM_RESET_BEFORE_UPDATE, Boolean.FALSE);

        extension.disposeContainer(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), params);

        if(mode.equals(KieServerMode.DEVELOPMENT)) {
            verify(deploymentService).undeploy(any(), beforeUndeployCaptor.capture());

            Function<DeploymentUnit, Boolean> function = beforeUndeployCaptor.getValue();

            function.apply(deploymentUnit);

            verify(runtimeDataService).getProcessInstancesByDeploymentId(eq(CONTAINER_ID), anyList(), any());
            verify(runimeManager, times(activeProcessInstances.size())).getRuntimeEngine(any());
            verify(engine, times(activeProcessInstances.size())).getKieSession();
            verify(session, times(activeProcessInstances.size())).abortProcessInstance(eq(new Long(1)));
            verify(runimeManager, times(activeProcessInstances.size())).disposeRuntimeEngine(any());
        } else {
            verify(deploymentService).undeploy(any());
        }
    }

    @Test
    public void testIsUpdateAllowedDevModeSNAPSHOT() throws IOException {
        testDeployContainer(VERSION_SNAPSHOT);

        mode = KieServerMode.DEVELOPMENT;

        assertTrue(extension.isUpdateContainerAllowed(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), new HashMap<>()));
    }

    @Test
    public void testIsUpdateAllowedDevModeNonSNAPSHOT() throws IOException {
        testDeployContainer(VERSION);

        mode = KieServerMode.DEVELOPMENT;

        assertTrue(extension.isUpdateContainerAllowed(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), new HashMap<>()));
    }

    @Test
    public void testIsUpdateAllowedProductionModeWithoutProcessInstances() throws IOException {
        testIsUpdateAllowed(false);
    }

    @Test
    public void testIsUpdateAllowedProductionModeWithProcessInstances() throws IOException {
        testIsUpdateAllowed(true);
    }

    @Test
    public void testLoadDefaultQueryDefinitions() {
        extension.registerDefaultQueryDefinitions();

        verify(queryService, times(10)).replaceQuery(any());
    }

    @Test
    public void testUpdateContainerProductionMode() throws IOException {
        testUpdateContainer(KieServerMode.PRODUCTION, VERSION, false, false);
    }

    @Test
    public void testUpdateContainerProductionModeWithForcedCleanup() throws IOException {
        testUpdateContainer(KieServerMode.PRODUCTION, VERSION, true, false);
    }

    @Test
    public void testUpdateDevModeSNAPSHOTContainer() throws IOException {
        testUpdateContainer(KieServerMode.DEVELOPMENT, VERSION_SNAPSHOT, false, false);
    }

    @Test
    public void testUpdateDevModeSNAPSHOTContainerWithForcedCeanup() throws IOException {
        activeProcessInstances.add(mockProcessInstance());
        activeProcessInstances.add(mockProcessInstance());
        activeProcessInstances.add(mockProcessInstance());

        testUpdateContainer(KieServerMode.DEVELOPMENT, VERSION_SNAPSHOT, true, true);
    }

    @Test
    public void testUpdateDevModeNonSNAPSHOTContainer() throws IOException {
        testUpdateContainer(KieServerMode.DEVELOPMENT, VERSION, false, false);
    }

    @Test
    public void testUpdateDevModeNonSNAPSHOTContainerWithForcedCeanup() throws IOException {
        activeProcessInstances.add(mockProcessInstance());
        activeProcessInstances.add(mockProcessInstance());
        activeProcessInstances.add(mockProcessInstance());

        testUpdateContainer(KieServerMode.DEVELOPMENT, VERSION, true, true);
    }

    private ProcessInstanceDesc mockProcessInstance() {
        ProcessInstanceDesc instance = mock(ProcessInstanceDesc.class);
        when(instance.getId()).thenReturn(new Long(1));
        return instance;
    }

    private void testUpdateContainer(KieServerMode mode, String version, boolean cleanup, boolean expectedCleanup) throws IOException {
        this.mode = mode;

        testDeployContainer(version);

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(new ReleaseId(GROUP_ID, ARTIFACT_ID, version), DependencyFilter.COMPILE_FILTER);
        List<Message> messages = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA, metaData);
        params.put(KieServerConstants.KIE_SERVER_PARAM_MESSAGES, messages);
        params.put(KieServerConstants.KIE_SERVER_PARAM_RESET_BEFORE_UPDATE, cleanup);

        extension.updateContainer(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), params);

        if (mode.equals(KieServerMode.PRODUCTION)) {
            verify(deploymentService).undeploy(any());
        } else {
            verify(deploymentService).undeploy(any(), beforeUndeployCaptor.capture());

            Function<DeploymentUnit, Boolean> function = beforeUndeployCaptor.getValue();

            assertNotNull(function);

            assertTrue(function.apply(deploymentUnit));

            if (expectedCleanup) {
                verify(runtimeDataService).getProcessInstancesByDeploymentId(eq(CONTAINER_ID), anyList(), any());
                verify(runimeManager, times(activeProcessInstances.size())).getRuntimeEngine(any());
                verify(engine, times(activeProcessInstances.size())).getKieSession();
                verify(session, times(activeProcessInstances.size())).abortProcessInstance(eq(new Long(1)));
                verify(runimeManager, times(activeProcessInstances.size())).disposeRuntimeEngine(any());
            } else {
                verify(runtimeDataService, never()).getProcessInstancesByDeploymentId(eq(CONTAINER_ID), anyList(), any());
                verify(runimeManager, never()).getRuntimeEngine(any());
                verify(engine, never()).getKieSession();
                verify(session, never()).abortProcessInstance(eq(new Long(1)));
                verify(runimeManager, never()).disposeRuntimeEngine(any());
            }
        }

        verify(deploymentService, times(2)).deploy(any());
    }

    private void testDeployContainer(String version) throws IOException {
        createEmptyKjar(GROUP_ID, ARTIFACT_ID, version);

        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, version);

        kieContainer = (InternalKieContainer) kieServices.newKieContainer(CONTAINER_ID, releaseId);
        KieContainerInstanceImpl containerInstance = new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer);

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(releaseId, DependencyFilter.COMPILE_FILTER);

        List<Message> messages = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA, metaData);
        params.put(KieServerConstants.KIE_SERVER_PARAM_MESSAGES, messages);

        extension.createContainer(CONTAINER_ID, containerInstance, params);

        verify(deploymentService).deploy(any());
    }

    private void testIsUpdateAllowed(boolean existingInstances) throws IOException {
        mode = KieServerMode.PRODUCTION;

        List<ProcessInstanceDesc> activeProcesses = new ArrayList<>();
        if (existingInstances) {
            activeProcesses.add(mock(ProcessInstanceDesc.class));
            activeProcesses.add(mock(ProcessInstanceDesc.class));
        }

        when(runtimeDataService.getProcessInstancesByDeploymentId(anyString(), anyList(), any())).thenReturn(activeProcesses);

        testDeployContainer(VERSION);

        if (existingInstances) {
            assertFalse(extension.isUpdateContainerAllowed(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), new HashMap<>()));
        } else {
            assertTrue(extension.isUpdateContainerAllowed(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), new HashMap<>()));
        }
    }

    private void createEmptyKjar(String groupId, String artifactId, String version) throws IOException {
        // create empty kjar; content does not matter
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        org.kie.api.builder.ReleaseId releaseId = kieServices.newReleaseId(groupId, artifactId, version);

        kfs.generateAndWritePomXML(releaseId);

        String processContent = IOUtils.toString(this.getClass().getResourceAsStream("/processes/hiring.bpmn2"), Charset.defaultCharset());

        kfs.write(RESOURCES + "hiring.bpmn2", processContent);

        KieModule kieModule = kieServices.newKieBuilder(kfs).buildAll().getKieModule();
        byte[] pom = kfs.read("pom.xml");
        byte[] jar = ((InternalKieModule) kieModule).getBytes();
        KieMavenRepository.getKieMavenRepository().installArtifact(releaseId, jar, pom);
    }
}

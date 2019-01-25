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
import org.kie.scanner.KieMavenRepository;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
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

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private RuntimeDataService runtimeDataService;

    @Mock
    private QueryService queryService;

    @Mock
    private KieServerImpl kieServer;

    private KieServicesImpl kieServices;

    private KieServerRegistry context;

    private JbpmKieServerExtension extension;

    private InternalKieContainer kieContainer;

    private DeploymentUnit deploymentUnit;

    private DeployedUnit deployedUnit;

    @Before
    public void init() {
        KieServerEnvironment.setServerId(UUID.randomUUID().toString());

        context = new KieServerRegistryImpl();
        context.registerStateRepository(new KieServerStateFileRepository(new File("target")));

        kieServices = (KieServicesImpl) KieServices.Factory.get();

        extension = new JbpmKieServerExtension() {
            {
                this.deploymentService = JbpmKieServerExtensionTest.this.deploymentService;
                this.runtimeDataService = JbpmKieServerExtensionTest.this.runtimeDataService;
            }
        };

        when(deploymentService.isDeployed(anyString())).thenAnswer((Answer<Boolean>) invocation -> deployed);
        doAnswer(invocation -> {
            deployed = false;
            return null;
        }).when(deploymentService).undeploy(any(), anyBoolean());
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
            return deployedUnit;
        });
        extension.setQueryService(queryService);
        extension.setContext(context);
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
    public void testIsUpdateAllowedSNAPSHOT() throws IOException {
        testDeployContainer(VERSION_SNAPSHOT);

        assertTrue(extension.isUpdateContainerAllowed(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), new HashMap<>()));
    }

    @Test
    public void testIsUpdateAllowedWithoutProcessInstances() throws IOException {
        testIsUpdateAllowed(false);
    }

    @Test
    public void testIsUpdateAllowedWithProcessInstances() throws IOException {
        testIsUpdateAllowed(true);
    }

    @Test
    public void testLoadDefaultQueryDefinitions() {
        extension.registerDefaultQueryDefinitions();

        verify(queryService, times(10)).replaceQuery(any());
    }

    @Test
    public void testUpdateContainer() throws IOException {
        testUpdateContainer(VERSION, false, false);
    }

    @Test
    public void testUpdateContainerWithForcedCleanup() throws IOException {
        testUpdateContainer(VERSION, true, false);
    }

    @Test
    public void testUpdateSNAPSHOTContainer() throws IOException {
        testUpdateContainer(VERSION_SNAPSHOT, false, false);
    }

    @Test
    public void testUpdateSNAPSHOTContainerWithForcedCeanup() throws IOException {
        testUpdateContainer(VERSION_SNAPSHOT, true, true);
    }

    private void testUpdateContainer(String version, boolean cleanup, boolean expectedCleanup) throws IOException {
        testDeployContainer(version);

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(new ReleaseId(GROUP_ID, ARTIFACT_ID, version), DependencyFilter.COMPILE_FILTER);
        List<Message> messages = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA, metaData);
        params.put(KieServerConstants.KIE_SERVER_PARAM_MESSAGES, messages);
        params.put(KieServerConstants.KIE_SERVER_PARAM_RESET_BEFORE_UPDATE, cleanup);

        extension.updateContainer(CONTAINER_ID, new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer), params);

        verify(deploymentService).undeploy(any(), eq(expectedCleanup));
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

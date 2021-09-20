/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.springboot.samples;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.appformer.maven.integration.MavenRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.prometheus.PrometheusKieServerExtension;
import org.kie.server.services.scenariosimulation.ScenarioSimulationKieServerExtension;
import org.kie.server.springboot.jbpm.ContainerAliasResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Arrays.asList;
import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode= AFTER_CLASS)
public class KieServerTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerTest.class);

    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";

    @LocalServerPort
    private int port;

    private String user = "john";
    private String password = "john@pwd1";

    private String containerAlias = "eval";
    private String containerId = "evaluation";
    private String processId = "evaluation";

    private KieServicesClient kieServicesClient;

    @Autowired
    private KieServer kieServer;

    @Autowired
    private ContainerAliasResolver aliasResolver;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeClass
    public static void generalSetup() {
        System.setProperty(KieServerConstants.KIE_SERVER_MODE, KieServerMode.PRODUCTION.name());
        KieServices ks = KieServices.Factory.get();
        org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/evaluation/jbpm-module.jar");
        File pom = new File("../kjars/evaluation/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
    }

    @AfterClass
    public static void generalCleanup() {
        System.clearProperty(KieServerConstants.KIE_SERVER_MODE);
    }

    @Before
    public void setup() {
        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        String serverUrl = "http://localhost:" + port + "/rest/server";
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, user, password);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        this.kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);

        KieContainerResource resource = new KieContainerResource(containerId, releaseId);
        resource.setContainerAlias(containerAlias);
        kieServicesClient.createContainer(containerId, resource);
    }

    @After
    public void cleanup() {
        if (kieServicesClient != null) {
            ServiceResponse<Void> response = kieServicesClient.disposeContainer(containerId);
            logger.info("Container {} disposed with response - {}", containerId, response.getMsg());
        }
    }

    @Test
    public void testProcessStartAndAbort() {

        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processes = queryClient.findProcesses(0, 10);
        assertEquals(1, processes.size());

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        // get details of process definition
        ProcessDefinition definition = processClient.getProcessDefinition(containerId, processId);
        assertNotNull(definition);
        assertEquals(processId, definition.getId());

        // start process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john");
        params.put("reason", "test on spring boot");
        Long processInstanceId = processClient.startProcess(containerId, processId, params);
        assertNotNull(processInstanceId);

        // find active process instances
        List<ProcessInstance> instances = queryClient.findProcessInstances(0, 10);
        assertEquals(1, instances.size());

        // at the end abort process instance
        processClient.abortProcessInstance(containerId, processInstanceId);

        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(3, processInstance.getState().intValue());
    }

    @Test
    public void testProcessStartAndWorkOnUserTask() {

        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processes = queryClient.findProcesses(0, 10);
        assertEquals(1, processes.size());

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        // get details of process definition
        ProcessDefinition definition = processClient.getProcessDefinition(containerId, processId);
        assertNotNull(definition);
        assertEquals(processId, definition.getId());

        // start process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john");
        params.put("reason", "test on spring boot");
        Long processInstanceId = processClient.startProcess(containerId, processId, params);
        assertNotNull(processInstanceId);

        UserTaskServicesClient taskClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
        // find available tasks
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(user, 0, 10);
        assertEquals(1, tasks.size());

        // complete task
        Long taskId = tasks.get(0).getId();

        taskClient.startTask(containerId, taskId, user);
        taskClient.completeTask(containerId, taskId, user, null);

        // find active process instances
        List<ProcessInstance> instances = queryClient.findProcessInstances(0, 10);
        assertEquals(1, instances.size());

        // at the end abort process instance
        processClient.abortProcessInstance(containerId, processInstanceId);

        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(3, processInstance.getState().intValue());
    }

    @Test
    public void testProcessStartAndAbortUsingAlias() {

        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processes = queryClient.findProcesses(0, 10);
        assertEquals(1, processes.size());

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        // get details of process definition
        ProcessDefinition definition = processClient.getProcessDefinition(containerId, processId);
        assertNotNull(definition);
        assertEquals(processId, definition.getId());

        String resolvedContainerId = aliasResolver.latest(containerAlias);

        // start process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john");
        params.put("reason", "test on spring boot");
        Long processInstanceId = processClient.startProcess(resolvedContainerId, processId, params);
        assertNotNull(processInstanceId);

        resolvedContainerId = aliasResolver.forProcessInstance(containerAlias, processInstanceId);

        // find active process instances
        List<ProcessInstance> instances = queryClient.findProcessInstances(0, 10);
        assertEquals(1, instances.size());

        // at the end abort process instance
        processClient.abortProcessInstance(resolvedContainerId, processInstanceId);

        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(3, processInstance.getState().intValue());
    }

    @Test
    public void testPrometheusEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/rest/metrics", String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertThat(response.getBody()).contains("# TYPE kie_server_start_time gauge");
    }

    @Test
    public void testCommandServiceSetup() {
        List<String> extensionsWithoutCommandSupport = asList(
                PrometheusKieServerExtension.EXTENSION_NAME,
                ScenarioSimulationKieServerExtension.EXTENSION_NAME);
        for (KieServerExtension extension : ((KieServerImpl) kieServer).getServerExtensions()) {
            KieContainerCommandService<?> kieContainerCommandService =
                    extension.getAppComponents(KieContainerCommandService.class);
            String extensionName = extension.getExtensionName();
            Objects.requireNonNull(extensionName, "extension.getExtensionName() should not be null");
            if (extensionsWithoutCommandSupport.contains(extensionName)) {
                assertNull(kieContainerCommandService);
            } else {
                assertNotNull(kieContainerCommandService);
            }
        }
    }
}

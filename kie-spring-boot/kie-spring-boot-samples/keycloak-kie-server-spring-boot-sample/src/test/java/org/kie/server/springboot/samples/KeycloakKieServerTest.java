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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.BARTLET;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.BARTLET_PW;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.JOHN;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.JOHN_PW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.springboot.samples.utils.KeycloakContainer;
import org.kie.server.springboot.samples.utils.KeycloakFixture;
import org.kie.server.springboot.samples.utils.KieJarBuildHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.DockerClientFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class, KeycloakIdentityProvider.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
public class KeycloakKieServerTest {

    static final String ARTIFACT_ID = "keycloak-sample";
    static final String GROUP_ID = "org.kie.server.testing";
    static final String VERSION = "1.0.0";
    
    static final String PATH = "src/test/resources/kjars/";
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakKieServerTest.class);
    
    @Rule
    public TestRule watcher = new TestWatcher() {
       protected void starting(Description description) {
          logger.info(">>> Starting test: " + description.getMethodName());
       }
    };
    
    @Autowired
    protected DeploymentService deploymentService;
    
    @LocalServerPort
    private int port;    
   
    private String containerId = "org.kie.server.testing:keycloak-sample:1.0.0";
    private String processId = "evaluation";
    private String restrictedVarProcessId = "HumanTaskWithRestrictedVar";
    
    private KModuleDeploymentUnit unit;
    private KieServicesClient kieServicesClient;
    
    private static KeycloakContainer keycloak = new KeycloakContainer();
    
    @BeforeClass
    public static void startTestContainers() {
        assumeTrue(isDockerAvailable());
        keycloak.start();
        KeycloakFixture.setup(keycloak.getAuthServerUrl());
    }

    @DynamicPropertySource
    public static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.auth-server-url", keycloak::getAuthServerUrl);
    }

    @AfterClass
    public static void generalCleanup() {
        keycloak.stop();
        System.clearProperty(KieServerConstants.KIE_SERVER_MODE);
    }

    @Before
    public void setup() {
        KieJarBuildHelper.createKieJar(PATH + ARTIFACT_ID);
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
    }
    
    @After
    public void cleanup() {
        if (deploymentService!=null) {
            deploymentService.undeploy(unit);
        }
        if (kieServicesClient != null) {
            kieServicesClient.disposeContainer(containerId);
        }
    }
    
    @Test
    public void testProcessStartAndAbort() {

        setupClient(JOHN, JOHN_PW);
        
        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processes = queryClient.findProcesses(0, 10);
        assertEquals(2, processes.size());

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

        setupClient(JOHN, JOHN_PW);
        
        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processes = queryClient.findProcesses(0, 10);
        assertEquals(2, processes.size());

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        // get details of process definition
        ProcessDefinition definition = processClient.getProcessDefinition(containerId, processId);
        assertNotNull(definition);
        assertEquals(processId, definition.getId());

        // start process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", JOHN);
        params.put("reason", "test on spring boot");
        Long processInstanceId = processClient.startProcess(containerId, processId, params);
        assertNotNull(processInstanceId);
       
        UserTaskServicesClient taskClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
        // find available tasks
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(JOHN, 0, 10);
        assertEquals(1, tasks.size());

        // complete task
        Long taskId = tasks.get(0).getId();

        taskClient.startTask(containerId, taskId, JOHN);
        taskClient.completeTask(containerId, taskId, JOHN, null);

        // find active process instances
        List<ProcessInstance> instances = queryClient.findProcessInstances(0, 10);
        assertEquals(1, instances.size());
        
        tasks = taskClient.findTasksAssignedAsPotentialOwner(JOHN, 0, 10);
        assertEquals(1, tasks.size());

        // at the end abort process instance
        processClient.abortProcessInstance(containerId, processInstanceId);

        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(3, processInstance.getState().intValue());        
    }
    
    @Test
    public void testAuthorizedUserOnRestrictedVar() {

        setupClient(BARTLET, BARTLET_PW);

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        
        // authorized user can start process instance and update the restricted variable
        Long processInstanceId = processClient.startProcess(containerId, restrictedVarProcessId, singletonMap("press", Boolean.TRUE));
        assertNotNull(processInstanceId);
       
        abortProcess(processClient, processInstanceId);
    }
    
    @Test
    public void testNoRestrictedVarViolation() {

        setupClient(JOHN, JOHN_PW);

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        
        // any unauthorized user can start that process if restricted variable value is not updated
        Long processInstanceId = processClient.startProcess(containerId, restrictedVarProcessId);
        assertNotNull(processInstanceId);
       
        abortProcess(processClient, processInstanceId);
    }

    
    @Test
    public void testRestrictedVarViolationByUnauthorizedUser() {

        setupClient(JOHN, JOHN_PW);

        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        
        Map<String, Object> map = singletonMap("press", Boolean.TRUE);
        // an unauthorized user cannot start process instance when trying to update the restricted variable
        assertThatExceptionOfType(KieServicesHttpException.class)
              .isThrownBy(() -> processClient.startProcess(containerId, restrictedVarProcessId, map));
    }
    
    private static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
    
    private void setupClient(String user, String password) {
        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        String serverUrl = "http://localhost:" + port + "/rest/server";
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, user, password);
        
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration);
        
        KieContainerResource resource = new KieContainerResource(containerId, releaseId);
        resource.setContainerAlias(containerId);
        kieServicesClient.createContainer(containerId, resource);
    }
    
    private void abortProcess(ProcessServicesClient processClient, Long processInstanceId) {
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        
        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(1, processInstance.getState().intValue());
        processClient.abortProcessInstance(containerId, processInstanceId);
    }
}



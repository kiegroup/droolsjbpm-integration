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

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appformer.maven.integration.MavenRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
public class KieServerTest {

    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";
    
    @LocalServerPort
    private int port;    
   
    private String user = "john";
    private String password = "john1";

    private String containerId = "evaluation";
    private String processId = "evaluation";
    
    private KieServicesClient kieServicesClient;
    
    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/evaluation/jbpm-module.jar");
        File pom = new File("../kjars/evaluation/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);

    }
    
    @Before
    public void setup() {
        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        String serverUrl = "http://localhost:" + port + "/rest/server";
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, user, password);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        this.kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration);
        
        KieContainerResource resource = new KieContainerResource(containerId, releaseId);
        kieServicesClient.createContainer(containerId, resource);
    }
    
    @After
    public void cleanup() {
        kieServicesClient.disposeContainer(containerId);        
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
}

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
import java.util.Map;

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
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= AFTER_CLASS)
public class KieServerMigrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServerMigrationTest.class);

    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";
    
    @LocalServerPort
    private int port;    
   
    private String user = "john";
    private String password = "john@pwd1";

    private String containerAlias = "eval";
    private String containerId = "evaluation";
    private String containerId2 = "evaluation2";
    private String processId = "evaluation";
    
    private KieServicesClient kieServicesClient;
   
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
        this.kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration);
        
        KieContainerResource resource = new KieContainerResource(containerId, releaseId);
        resource.setContainerAlias(containerAlias);
        KieServerConfigItem configItem = new KieServerConfigItem();
        configItem.setName("RuntimeStrategy");
        configItem.setValue("PER_PROCESS_INSTANCE");
        configItem.setType("BPM");
        resource.addConfigItem(configItem);
        kieServicesClient.createContainer(containerId, resource);
        
        KieContainerResource resource2 = new KieContainerResource(containerId2, releaseId);
        resource2.setContainerAlias(containerAlias);
        resource2.addConfigItem(configItem);
        kieServicesClient.createContainer(containerId2, resource2);
    }
    
    @After
    public void cleanup() {
        if (kieServicesClient != null) {
            ServiceResponse<Void> response = kieServicesClient.disposeContainer(containerId);
            logger.info("Container {} disposed with response - {}", containerId, response.getMsg());
            response = kieServicesClient.disposeContainer(containerId2);
            logger.info("Container {} disposed with response - {}", containerId2, response.getMsg());
        }
    }
    
    @Test
    public void testProcessStartAndAbort() {

        // query for all available process definitions
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        ProcessAdminServicesClient processAdminClient = kieServicesClient.getServicesClient(ProcessAdminServicesClient.class);
 
        // start process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john");
        params.put("reason", "test on spring boot");
        Long processInstanceId = processClient.startProcess(containerId, processId, params);
        assertNotNull(processInstanceId);
       
        ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(1, processInstance.getState().intValue());
        assertEquals(containerId, processInstance.getContainerId());
        
        try {
            MigrationReportInstance report = processAdminClient.migrateProcessInstance(containerId, processInstanceId, containerId2, processId);
            assertTrue(report.isSuccessful());
            processInstance = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(processInstance);
            assertEquals(1, processInstance.getState().intValue());
            assertEquals(containerId2, processInstance.getContainerId());
        }
        catch (Exception ex ) {
            // clean up if failure
            try {
                processClient.abortProcessInstance(containerId, processInstanceId);
            } catch (Exception abortEx) {
                logger.warn("Error aborting process instance over container "+containerId, abortEx);
            }
            fail("Migration failed: " + ex);
            throw ex;
        }
        //abort process instance (outside try/catch), to not hide exception that will certainly occur if  migration fails 
        processClient.abortProcessInstance(containerId2, processInstanceId);
        processInstance = queryClient.findProcessInstanceById(processInstanceId);
        assertNotNull(processInstance);
        assertEquals(3, processInstance.getState().intValue());
    }

   
}

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.appformer.maven.integration.MavenRepository;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.io.impl.ClassPathResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.springboot.samples.listeners.SampleAgendaEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-rules.properties")
public class RulesOnlyKieServerTest {
    
    static final String ARTIFACT_ID = "rules";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";

    @LocalServerPort
    private int port;    
   
    private String user = "john";
    private String password = "john@pwd1";
    
    private String containerId = "rules";
    
    private KieServicesClient kieServicesClient;
    
    @Autowired
    private SampleAgendaEventListener listener;
    
    @BeforeClass
    public static void generalSetup() {
        System.setProperty(KieServerConstants.KIE_SERVER_MODE, KieServerMode.REGULAR.name());
        createKJar();
    }

    @AfterClass
    public static void generalCleanup() {
        System.clearProperty(KieServerConstants.KIE_SERVER_MODE);
    }

    @Before
    public void setup() {
        listener.clear();
        org.kie.server.api.model.ReleaseId releaseId = new org.kie.server.api.model.ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        
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
    public void testInvokeRulesOnStateless() {
        testInvokeRulesOn("defaultStatelessKieSession");
    }
    
    @Test
    public void testInvokeRulesOnStatefull() {
        testInvokeRulesOn("defaultKieSession");
    }
    
    private void testInvokeRulesOn(String ksessionName) {

        List<Command<?>> commands = new ArrayList<Command<?>>();
        KieCommands cmdFactory = KieServices.Factory.get().getCommands();
        
        commands.add(cmdFactory.newInsert("John"));
        
        commands.add(cmdFactory.newFireAllRules("fire-identifier"));
        RuleServicesClient rulesClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
        ServiceResponse<ExecutionResults> reply = rulesClient.executeCommandsWithResults("rules", cmdFactory.newBatchExecution(commands, ksessionName));
        if (!KieServiceResponse.ResponseType.SUCCESS.equals(reply.getType())){
            throw new RuntimeException("executeRule failed with message: " + reply.getMsg());
        }
        ExecutionResults result = reply.getResult();
        
        assertNotNull(result);
        
        assertEquals(1, listener.getFired().size());
        assertEquals("Your First Rule",listener.getFired().get(0));
    }
    
    
    private static void createKJar() {
        KieServices kieServices = KieServices.get();
        
        ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.generateAndWritePomXML(releaseId);
        
        byte[] pom = kfs.read("pom.xml");
        
        kfs.write("src/main/resources/sample-rule.drl", new ClassPathResource("sample-rules.drl"));
        
        KieBuilder kb = kieServices.newKieBuilder(kfs).buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            for (Message result : kb.getResults().getMessages()) {
                System.out.println("Error: " + result.getText());
            }
            throw new RuntimeException("Unable to create KJar for requested conditions resources");
        }
        InternalKieModule kieModule = (InternalKieModule) kieServices.getRepository().getKieModule(releaseId);
        byte[] kjar = kieModule.getBytes();
                
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
    }
}

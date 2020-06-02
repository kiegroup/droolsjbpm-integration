/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples;

import java.io.File;
import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.springboot.samples.listeners.CountDownLatchOnlyAfterNodeEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { JBPMApplication.class,
        TestAutoConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ExceptionHandlingTest {

    private static final String GROUP_ID = "com.myspace";
    private static final String ARTIFACT_ID = "exception-handling";
    private static final String VERSION = "1.0";

    private static final String PROCESS_ID = "exception-handling.MainProcess";

    @Autowired
    private ProcessService processService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private CountDownLatchOnlyAfterNodeEventListener countDownListener;
    
    private KModuleDeploymentUnit unit = null;

    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/exception-handling/exception-handling-1.0.jar");
        File pom = new File("../kjars/exception-handling/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
    }

    @Before
    public void setup() {
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
        countDownListener.configure(PROCESS_ID, 1);
    }

    @After
    public void cleanup() {
        deploymentService.undeploy(unit);
    }

    @Test(timeout = 10000)
    public void whenProcessStartedThenSubprocessExceptionIsCaught() throws Exception {
        countDownListener.configureNode(PROCESS_ID, "Task", 1);
        long processInstanceId = processService.startProcess(unit.getIdentifier(), PROCESS_ID);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        
        countDownListener.getCountDown().await();
    }





}

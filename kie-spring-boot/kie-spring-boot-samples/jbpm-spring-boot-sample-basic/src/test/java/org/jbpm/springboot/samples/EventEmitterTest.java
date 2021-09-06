/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
import java.util.Collections;
import java.util.List;
import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.springboot.samples.events.emitters.CountDownLatchEmitter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class EventEmitterTest {

    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";
    static final String USER_JOHN = "john";
    static final String USER_YODA = "yoda";

    private KModuleDeploymentUnit unit = null;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private UserTaskAdminService userTaskAdminService;

    @Autowired
    private CountDownLatchEmitter countDownLatchEmitter;

    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/evaluation/jbpm-module.jar");
        File pom = new File("../kjars/evaluation/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);

        EntityManagerFactoryManager.get().clear();
    }


    @Before
    public void setup() {
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
    }

    @After
    public void cleanup() {

        deploymentService.undeploy(unit);
    }

    @Test(timeout = 10000)
    public void testProcessEventListenerRegistration() throws Exception {
        countDownLatchEmitter.configure(6);

        assertNotNull(unit);
        assertNotNull(countDownLatchEmitter.getProcessService());

        Long processInstanceId = processService.startProcess(unit.getIdentifier(), "evaluation");

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        // "newCollection", "apply" and "deliver" methods should've been called
        assertLatchEmitterIs(3);

        abortProcessAndCheckLatchEmitter(processInstanceId);
    }
    
    @Test(timeout = 10000)
    public void testEmitterBusinessAdminOperations() throws Exception {
        countDownLatchEmitter.configure(12);
        
        Long processInstanceId = processService.startProcess(unit.getIdentifier(), "evaluation", Collections.singletonMap("employee", USER_JOHN));

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        
        // "newCollection", "apply" and "deliver" methods should've been called for start
        assertLatchEmitterIs(9);
        
        List<TaskSummary> tasks = runtimeDataService.getTasksOwned(USER_JOHN, new QueryFilter());
        assertThat(tasks.size()).isEqualTo(1);
        
        userTaskAdminService.addBusinessAdmins(tasks.get(0).getId(), false, new UserImpl(USER_YODA));
        // "newCollection", "apply" and "deliver" methods should've been called for addBusinessAdmins operation
        assertLatchEmitterIs(6);
        
        userTaskAdminService.removeBusinessAdmins(tasks.get(0).getId(), new UserImpl(USER_YODA));
        // "newCollection", "apply" and "deliver" methods should've been called for removeBusinessAdmins operation
        assertLatchEmitterIs(3);
        
        abortProcessAndCheckLatchEmitter(processInstanceId);
    }


    protected void assertLatchEmitterIs(int count) {
        assertThat(countDownLatchEmitter.getCountDownLatch().getCount()).isEqualTo(count);
    }


    protected void abortProcessAndCheckLatchEmitter(Long processInstanceId) throws InterruptedException {
        processService.abortProcessInstance(processInstanceId);
        countDownLatchEmitter.getCountDownLatch().await();

        assertLatchEmitterIs(0);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
}

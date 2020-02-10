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

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.migration.MigrationManager;
import org.jbpm.runtime.manager.impl.migration.MigrationSpec;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.springboot.samples.listeners.CountDownLatchEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { JBPMApplication.class,
        TestAutoConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-quartz.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TimerInProcessMigrationTest {

    private static final Logger logger = LoggerFactory.getLogger(TimerInProcessMigrationTest.class);

    private static final String GROUP_ID = "org.jbpm";
    private static final String ARTIFACT_ID = "timer-in-process-migration";
    private static final String VERSION = "1.0";
    private static final String VERSION_2 = "2.0";

    private static final String USERTASK_BOUNDARY_TIMER_ID_V1 = "UserTaskBoundary-v1";
    private static final String USERTASK_BOUNDARY_TIMER_ID_V2 = "UserTaskBoundary-v2";
    private static final String USER_JOHN = "john";
    private static final String USER_MARY= "mary";
    
    private static final String DEPLOYMENT_ID_V1 = "org.jbpm:timer-in-process-migration:1.0";
    private static final String DEPLOYMENT_ID_V2 = "org.jbpm:timer-in-process-migration:2.0";
    
    private KModuleDeploymentUnit unit = null;
    private KModuleDeploymentUnit unitV2 = null;
    private RuntimeEngine runtime;
    private long pid1;
    
    @Autowired
    private ProcessService processService;
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private CountDownLatchEventListener countDownListener;
    
    private RuntimeManager managerV1;
    private RuntimeManager managerV2;
    
    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/timer-in-process-migration/v1/timer-in-process-migration-1.0.jar");
        File pom = new File("../kjars/timer-in-process-migration/v1/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
        
        ReleaseId releaseId2 = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION_2);
        File kjar2 = new File("../kjars/timer-in-process-migration/v2/timer-in-process-migration-2.0.jar");
        File pom2 = new File("../kjars/timer-in-process-migration/v2/pom.xml");
        
        repository.installArtifact(releaseId2, kjar2, pom2);
        
    }

    @Before
    public void setup() {
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
        managerV1 = RuntimeManagerRegistry.get().getManager(unit.getIdentifier());
        countDownListener.configure(DEPLOYMENT_ID_V1, 1);
        
        unitV2 = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION_2);
        deploymentService.deploy(unitV2);
        managerV2 = RuntimeManagerRegistry.get().getManager(unitV2.getIdentifier());
    }

    @After
    public void cleanup() {
        deploymentService.undeploy(unit);
        deploymentService.undeploy(unitV2);
        
        if (managerV1 != null) {
            managerV1.close();
        }
        if (managerV2 != null) {
            managerV2.close();
        }
    }
    
    

    @Test(timeout=10000)
    public void testMigrateUserTaskCompletedBoundaryTimerProcessInstance() throws Exception {
        countDownListener.configureNode(USERTASK_BOUNDARY_TIMER_ID_V1, "Goodbye v2", 1);

        pid1 = startProcessTillBoundaryTimer();

        completeUserTask(managerV1, USER_JOHN);

        logger.debug("No boundary timer triggered...");

        migrateProcessUserTaskBoundary();

        //Only needs to complete Mary's task after migration
        completeUserTask(managerV2, USER_MARY);

        checkProcessCompleted();
    }
    
    @Test(timeout=10000)
    public void testMigrateUserTaskNotCompletedBoundaryTimerProcessInstance() throws Exception {
        countDownListener.configureNode(USERTASK_BOUNDARY_TIMER_ID_V1, "Script Task 1", 1);
        pid1 = startProcessTillBoundaryTimer();

        countDownListener.getCountDown().await();
        logger.info("Boundary timer triggered without completing user task...");
        migrateProcessUserTaskBoundary();

        //Needs to complete both user tasks after migration
        completeUserTask(managerV2, USER_JOHN);
        completeUserTask(managerV2, USER_MARY);

        checkProcessCompleted();
    }
    
    private long startProcessTillBoundaryTimer() {
        long pid = processService.startProcess(unit.getIdentifier(), USERTASK_BOUNDARY_TIMER_ID_V1);
        
        assertNotNull(pid);
        assertTrue(pid > 0);
        
        ProcessInstance pi1 = processService.getProcessInstance(pid);
        assertEquals(ProcessInstance.STATE_ACTIVE, pi1.getState()); 
        return pid;
    }

    private void migrateProcessUserTaskBoundary() {
        managerV1.disposeRuntimeEngine(runtime);

        MigrationSpec migrationSpec = new MigrationSpec(DEPLOYMENT_ID_V1, pid1, DEPLOYMENT_ID_V2, USERTASK_BOUNDARY_TIMER_ID_V2);

        MigrationManager migrationManager = new MigrationManager(migrationSpec);
        migrationManager.migrate();
        
        ProcessInstance pi1 = processService.getProcessInstance(pid1);
        assertEquals(ProcessInstance.STATE_ACTIVE, pi1.getState()); 
    }

    private void completeUserTask(RuntimeManager manager, String user) {
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        TaskService taskService = runtime.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK"); 
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        TaskSummary task = tasks.get(0);
        assertNotNull(task);

        tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        taskService.start(task.getId(), user);
        taskService.complete(task.getId(), user, null);
    }

    private void checkProcessCompleted() throws InterruptedException{
        countDownListener.getCountDown().await();
        ProcessInstance pi = processService.getProcessInstance(pid1);
        assertNull(pi);
    }
    
}

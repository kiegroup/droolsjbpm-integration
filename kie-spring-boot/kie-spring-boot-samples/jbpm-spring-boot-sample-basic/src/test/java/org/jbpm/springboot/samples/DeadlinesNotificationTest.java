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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.deadlines.notifications.impl.NotificationListenerManager;
import org.jbpm.springboot.samples.events.listeners.CountDownLatchNotificationListener;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DeadlinesNotificationTest {

    static final String ARTIFACT_ID = "notification";
    static final String GROUP_ID = "org.jbpm";
    static final String VERSION = "1.0";
    static final String USER_YODA = "yoda";

    private KModuleDeploymentUnit unit = null;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ProcessService processService;
    
    @Autowired
    private CountDownLatchNotificationListener countDownListener;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private UserTaskService userTaskService;

    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/notification/notification.jar");
        File pom = new File("../kjars/notification/pom.xml");
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
        NotificationListenerManager.get().reset();
        deploymentService.undeploy(unit);
    }

    @Test(timeout = 10000)
    public void testSaveContentAfterDeadlines() throws Exception {
        countDownListener.configure(1);
        countDownListener.setSaveContent(true);

        assertNotNull(unit);

        Long processInstanceId = processService.startProcess(unit.getIdentifier(), "notification");

        assertTrue(processInstanceId > 0);

        countDownListener.getCountDown().await();
        
        assertNotNull(countDownListener.getContentId());

        processService.abortProcessInstance(processInstanceId);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    @Test(timeout = 10000)
    public void testRepeatedNotificationsAfterDeadlinesWhenNotStarted() throws Exception {
        assertNotNull(unit);
        Long processInstanceId = processService.startProcess(unit.getIdentifier(), "repeated-notifications");

        assertTrue(processInstanceId > 0);

        assertNotificationEventReceived(4000, 3);
        assertNotificationEventReceived(3000, 2); // 1 "Not started" repeated notification after 3 sec
                                                                          // +
                                                                          // 1 "Not completed" repeated notification after 5 secs

        processService.abortProcessInstance(processInstanceId);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    @Test(timeout = 20000)
    public void testRepeatedNotificationsAfterDeadlinesWhenNotCompleted() throws Exception {
        assertNotNull(unit);
        Long processInstanceId = processService.startProcess(unit.getIdentifier(), "repeated-notifications");

        assertTrue(processInstanceId > 0);

        List<TaskSummary> task = runtimeDataService.getTasksOwned(USER_YODA, new QueryFilter());
        assertEquals(1, task.size());
        userTaskService.start(task.get(0).getId(), USER_YODA);

        assertNotificationEventReceived(6000, 1); // 1 "Not completed" repeated notification after 5 secs

        userTaskService.complete(task.get(0).getId(), USER_YODA, new HashMap<>());
        assertNotificationEventReceived(6000, 0); // No notifications received

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    private void assertNotificationEventReceived(long millisToWait, int totalNotifications) throws InterruptedException {
        countDownListener.reset();
        countDownListener.configure(totalNotifications + 1); // We need to make sure we don't get more notifications than expected
        assertFalse(countDownListener.getCountDown().await(millisToWait, TimeUnit.MILLISECONDS));
        assertEquals(totalNotifications, countDownListener.getEventsReceived().size());
    }

}

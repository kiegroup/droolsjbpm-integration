/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

import org.appformer.maven.integration.MavenRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.server.api.KieServerConstants.CFG_BYPASS_AUTH_USER;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-case.properties")
@DirtiesContext(classMode= AFTER_CLASS)
public class CaseRuntimeDataServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CaseRuntimeDataServiceTest.class);

    private static final String ACTOR_ID = "ActorId";
    private static final String COMMENT = "Comment";
    private static final String GROUP = "GroupId";
    private static final String BUSINESS_ADMINISTRATOR_ID = "BusinessAdministratorId";
    private static final String BUSINESS_ADMINISTRATOR_GROUP_ID = "BusinessAdministratorGroupId";
    private static final String TASK_STAKEHOLDER_ID = "TaskStakeholderId";

    private static final String ARTIFACT_ID = "case-insurance";
    private static final String GROUP_ID = "org.kie.server.testing";
    private static final String VERSION = "1.0.0";

    private static final String CONTAINER_ID = "insurance";

    @LocalServerPort
    private int port;

    private KieServicesClient kieServicesClient;
    private CaseServicesClient caseClient;
    private UserTaskServicesClient taskClient;

    private static final String USER_YODA = "yoda";
    private static final String USER_JOHN = "john";
    private static final String USER_ADMINISTRATOR = "administrator";
    private static final String USER_MARY = "mary";
    private static final String PASSWORD = "usetheforce123@";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";
    private static final String CASE_PARTICIPANT_ROLE = "participant";
    private static final String CASE_HR_GROUP = "HR";
    private static final String CASE_ADMIN_GROUP = "Administrators";

    private static final String CASE_HR_ID_PREFIX = "HR";
    private static final String CASE_HR_DEF_ID = "UserTaskCase";

    @BeforeClass
    public static void generalSetup() {
        System.setProperty(CFG_BYPASS_AUTH_USER, "true");
        KieServices ks = KieServices.Factory.get();
        org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/case-insurance/case-insurance.jar");
        File pom = new File("..//kjars/case-insurance/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);
    }

    @AfterClass
    public static void generalCleanup() {
        System.clearProperty(CFG_BYPASS_AUTH_USER);
    }

    @Before
    public void setup() {
        login(USER_YODA);
        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        kieServicesClient.createContainer(CONTAINER_ID, resource);
    }

    @After
    public void cleanup() {
        if (kieServicesClient != null) {
            ServiceResponse<Void> response = kieServicesClient.disposeContainer(CONTAINER_ID);
            logger.info("Container {} disposed with response - {}", CONTAINER_ID, response.getMsg());
        }
    }

    @Test
    public void testFindCaseTasksAssignedAsPotentialOwnerByPassAuth() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertTaskListAsPotentialOwner(caseId, USER_YODA, USER_JOHN, true);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        login(USER_JOHN);

        caseId = startUserTaskCase(USER_JOHN, USER_YODA);
        assertNotNull(caseId);
        assertFalse(caseId.isEmpty());

        assertTaskListAsPotentialOwner(caseId, USER_JOHN, USER_YODA, false);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testFindCaseTasksAssignedAsBusinessAdminByPassAuth() throws Exception {
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .addUserAssignments(CASE_CONTACT_ROLE, USER_JOHN)
                .addGroupAssignments(CASE_PARTICIPANT_ROLE, CASE_ADMIN_GROUP)
                .build();
        String caseId = startUserTaskCase(caseFile);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));
        assertTaskListAsBusinessAdmin(caseId, USER_YODA, USER_YODA);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        caseId = startUserTaskCase(caseFile);
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertTaskListAsBusinessAdmin(caseId, USER_YODA, USER_ADMINISTRATOR);
        login(USER_YODA);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testFindCaseTasksAssignedAsStakeHolderByPassAuth() throws Exception {
        String caseId = startUserTaskCase(CaseFile.builder()
                                                  .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                                                  .addUserAssignments(CASE_CONTACT_ROLE, USER_JOHN)
                                                  .addUserAssignments(CASE_PARTICIPANT_ROLE, USER_MARY)
                                                  .build());

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));
        assertTaskListAsStakeHolder(caseId, USER_MARY);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        login(USER_JOHN);
        caseId = startUserTaskCase(CaseFile.builder()
                                           .addUserAssignments(CASE_OWNER_ROLE, USER_JOHN)
                                           .addUserAssignments(CASE_CONTACT_ROLE, USER_YODA)
                                           .addGroupAssignments(CASE_PARTICIPANT_ROLE, CASE_HR_GROUP)
                                           .build());
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertTaskListAsStakeHolder(caseId, USER_MARY);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    private void assertTaskListAsPotentialOwner(String caseId, String caseOwner, String potOwner, boolean isInHR) {
        List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, caseOwner, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Hello1", tasks.get(0).getName());
        assertEquals(caseOwner, tasks.get(0).getActualOwner());
        assertEquals(Reserved, Status.valueOf(tasks.get(0).getStatus()));

        Map<String, Object> taskInput = new HashMap<>();
        taskInput.put(ACTOR_ID, potOwner);
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);
        tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, potOwner, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Hello2", tasks.get(0).getName());
        assertEquals(potOwner, tasks.get(0).getActualOwner());
        assertEquals(Reserved, Status.valueOf(tasks.get(0).getStatus()));
        assertEquals("User's comment", tasks.get(0).getDescription());
        taskClient.startTask(CONTAINER_ID, tasks.get(0).getId(), potOwner);
        taskClient.completeTask(CONTAINER_ID, tasks.get(0).getId(), potOwner, null);

        taskInput = new HashMap<>();
        taskInput.put(GROUP, CASE_HR_GROUP);
        taskInput.put(COMMENT, "HR's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);
        tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, potOwner, 0, 10);
        assertNotNull(tasks);
        if (isInHR) {
            assertEquals(1, tasks.size());
            assertEquals("Hello2", tasks.get(0).getName());
            assertNull(tasks.get(0).getActualOwner());
            assertEquals(Ready, Status.valueOf(tasks.get(0).getStatus()));
            assertEquals("HR's comment", tasks.get(0).getDescription());
            taskClient.claimTask(CONTAINER_ID, tasks.get(0).getId(), potOwner);
            taskClient.startTask(CONTAINER_ID, tasks.get(0).getId(), potOwner);
            taskClient.completeTask(CONTAINER_ID, tasks.get(0).getId(), potOwner, null);
        } else {
            assertEquals(0, tasks.size());
        }
    }

    private void assertTaskListAsBusinessAdmin(String caseId, String caseOwner, String lookupUser) throws Exception {
        List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        Map<String, Object> taskInput = new HashMap<>();

        login(caseOwner);
        taskInput.put(BUSINESS_ADMINISTRATOR_ID, "contact");
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);

        login(lookupUser);
        tasks = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(tasks);
        assertEquals(2, tasks.size()); // 1 for Yoda and 1 for John
        assertTaskByActualOwner(tasks, USER_YODA, "Hello1", Reserved, "Simple description");
        assertTaskByActualOwner(tasks, USER_JOHN, "Hello2", Reserved, "User's comment");

        login(caseOwner);
        taskInput.put(BUSINESS_ADMINISTRATOR_GROUP_ID, CASE_PARTICIPANT_ROLE);
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);

        login(lookupUser);
        tasks = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(tasks);
        assertEquals(3, tasks.size()); // 1 for Yoda and 2 ad-hoc tasks for John
        assertEquals(2, tasks.stream()
                .filter(taskSummary -> USER_JOHN.equals(taskSummary.getActualOwner()))
                .count());
        assertTaskByActualOwner(tasks, USER_YODA, "Hello1", Reserved, "Simple description");
        assertTaskByActualOwner(tasks, USER_JOHN, "Hello2", Reserved, "User's comment");
    }

    private void assertTaskListAsStakeHolder(String caseId, String stakeHolder) {
        List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsStakeholder(caseId, stakeHolder, 0, 10);
        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        Map<String, Object> taskInput = new HashMap<>();

        taskInput.put(TASK_STAKEHOLDER_ID, CASE_PARTICIPANT_ROLE);
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);

        tasks = caseClient.findCaseTasksAssignedAsStakeholder(caseId, stakeHolder, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Hello2", tasks.get(0).getName());
        assertEquals(Reserved, Status.valueOf(tasks.get(0).getStatus()));
        assertEquals(USER_JOHN, tasks.get(0).getActualOwner());
        assertEquals("User's comment", tasks.get(0).getDescription());
    }

    private void assertTaskByActualOwner(List<TaskSummary> tasks, String expectedActualOwner,
                                         String expectedName, Status expectedStatus, String expectedDescription) {
        tasks.stream()
                .filter(taskSummary -> expectedActualOwner.equals(taskSummary.getActualOwner()))
                .forEach(task -> {
                    assertEquals(expectedName, task.getName());
                    assertEquals(expectedStatus, Status.valueOf(task.getStatus()));
                    assertEquals(expectedDescription, task.getDescription());
                });
    }

    private String startUserTaskCase(String owner, String contact) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, owner)
                .addUserAssignments(CASE_CONTACT_ROLE, contact)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_HR_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }

    private String startUserTaskCase(CaseFile caseFile) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        caseFile.setData(data);

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_HR_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }

    private void login(String username) {
        String serverUrl = "http://localhost:" + port + "/rest/server";
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, username, PASSWORD);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        this.kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        caseClient = kieServicesClient.getServicesClient(CaseServicesClient.class);
        taskClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
    }
}

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.jbpm.cases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseAdHocFragment;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseFileDataItem;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.category.WildflyOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;

public class CaseRuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String ACTOR_ID = "ActorId";
    private static final String GROUP_ID = "GroupId";
    private static final String COMMENT = "Comment";
    private static final String BUSINESS_ADMINISTRATOR_ID = "BusinessAdministratorId";
    private static final String BUSINESS_ADMINISTRATOR_GROUP_ID = "BusinessAdministratorGroupId";
    private static final String TASK_STAKEHOLDER_ID = "TaskStakeholderId";

    private static final String CONTAINER_ID = "insurance";
    private static final String CONTAINER_ID2 = "insurance-second";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";
    private static final String CASE_PARTICIPANT_ROLE = "participant";
    private static final String CASE_HR_GROUP = "HR";
    private static final String CASE_ADMIN_GROUP = "Administrators";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";
    private static final String CASE_ASSESSOR_ROLE = "assessor";

    private static final String CLAIM_CASE_ID_PREFIX = "CAR_INS";
    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    private static final String CLAIM_CASE_DESRIPTION = "CarInsuranceClaimCase";
    private static final String CLAIM_CASE_NAME = "CarInsuranceClaimCase";
    private static final String CLAIM_CASE_VERSION = "1.0";

    private static final String CASE_HR_ID_PREFIX = "HR";
    private static final String CASE_HR_DEF_ID = "UserTaskCase";
    private static final String CASE_HR_DESRIPTION = "Case first case started";
    private static final String CASE_HR_NAME = "Simple Case with User Tasks";
    private static final String CASE_HR_VERSION = "1.0";

    private static final String DATA_VERIFICATION_DEF_ID = "DataVerification";
    private static final String USER_TASK_DEF_ID = "UserTask";

    private static final String HELLO_1_TASK = "Hello1";
    private static final String HELLO_2_TASK = "Hello2";

    private static final String SUBMIT_POLICE_REPORT_TASK = "Submit police report";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/case-insurance");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(CLAIM_REPORT_CLASS_NAME, Class.forName(CLAIM_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(PROPERTY_DAMAGE_REPORT_CLASS_NAME, Class.forName(PROPERTY_DAMAGE_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @After
    public void resetUser() throws Exception {
        changeUser(TestConfig.getUsername());
    }

    @Test
    public void testGetCaseDefinitionsByNotExistingContainer() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitionsByContainer("not-existing-container", 0, 10);
        assertNotNull(definitions);
        assertEquals(0, definitions.size());
    }

    @Test
    public void testGetCaseDefinitionsByContainer() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 0, 10);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());

        List<String> mappedDefinitions = definitions.stream().map(CaseDefinition::getIdentifier).collect(Collectors.toList());
        assertTrue(mappedDefinitions.contains(CASE_HR_DEF_ID));
        assertTrue(mappedDefinitions.contains(CLAIM_CASE_DEF_ID));

        definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 0, 1);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 1, 1);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
    }

    @Test
    public void testGetCaseDefinitionsByContainerSorting() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 0, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 1, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, false);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
        assertHrCaseDefinition(definitions.get(1));
    }

    @Test
    public void testGetCaseDefinitionNotExistingContainer() {
        try {
            caseClient.getCaseDefinition("not-existing-container", CASE_HR_DEF_ID);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseDefinitionNotExistingCase() {
        try {
            caseClient.getCaseDefinition(CONTAINER_ID, "not-existing-case");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseDefinition() {
        CaseDefinition hrCase = caseClient.getCaseDefinition(CONTAINER_ID, CASE_HR_DEF_ID);
        assertHrCaseDefinition(hrCase);
    }

    @Test
    public void testGetCaseDefinitions() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitions(0, 10);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());

        List<String> mappedDefinitions = definitions.stream().map(CaseDefinition::getIdentifier).collect(Collectors.toList());
        assertTrue(mappedDefinitions.contains(CASE_HR_DEF_ID));
        assertTrue(mappedDefinitions.contains(CLAIM_CASE_DEF_ID));

        definitions = caseClient.getCaseDefinitions(0, 1);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions(1, 1);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
    }

    @Test
    public void testGetCaseDefinitionsSorting() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitions(0, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions(1, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions(0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, false);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
        assertHrCaseDefinition(definitions.get(1));
    }

    @Test
    public void testGetCaseDefinitionsSortingByCaseName() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitions(0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, true);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
        assertHrCaseDefinition(definitions.get(1));
    }

    @Test
    public void testGetCaseDefinitionsSortingByDeploymentId() {
        createContainer(CONTAINER_ID2, releaseId);

        List<CaseDefinition> definitions = caseClient.getCaseDefinitions(0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_DEPLOYMENT_ID, true);
        assertNotNull(definitions);
        assertEquals(4, definitions.size());
        assertEquals(definitions.get(0).getContainerId(), CONTAINER_ID);
        assertEquals(definitions.get(1).getContainerId(), CONTAINER_ID);
        assertEquals(definitions.get(2).getContainerId(), CONTAINER_ID2);
        assertEquals(definitions.get(3).getContainerId(), CONTAINER_ID2);

        KieServerAssert.assertSuccess(client.disposeContainer(CONTAINER_ID2));
    }

    @Test
    public void testGetCaseDefinitionsWithFilter() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitions("User", 0, 10);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions("User", 1, 10);
        assertNotNull(definitions);
        assertEquals(0, definitions.size());
    }

    @Test
    public void testGetCaseDefinitionsWithFilterSorting() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitions("Case", 0, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertHrCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions("Case", 1, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));

        definitions = caseClient.getCaseDefinitions("Case", 0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_ID, false);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        assertCarInsuranceCaseDefinition(definitions.get(0));
        assertHrCaseDefinition(definitions.get(1));
    }

    @Test
    public void testGetCaseInstanceNotExistingContainer() {
        try {
            caseClient.getCaseInstance("not-existing-container", CASE_HR_DEF_ID);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseInstanceNotExistingCase() {
        try {
            caseClient.getCaseInstance(CONTAINER_ID, "not-existing-case");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseInstance() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertHrCaseInstance(caseInstance, caseId, USER_YODA);
        assertNull(caseInstance.getCaseFile());
        assertNull(caseInstance.getRoleAssignments());
        assertNull(caseInstance.getMilestones());
        assertNull(caseInstance.getStages());
    }

    @Test
    public void testGetCaseInstanceUserTaskCaseWithData() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Milestone1", null);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId, true, true, true, true);
        assertHrCaseInstance(caseInstance, caseId, USER_YODA);

        KieServerAssert.assertNullOrEmpty("Stages should be empty.", caseInstance.getStages());

        // Assert case file
        assertNotNull(caseInstance.getCaseFile());
        assertEquals("first case started", caseInstance.getCaseFile().getData().get("s"));

        // Assert role assignments
        assertNotNull(caseInstance.getRoleAssignments());
        assertEquals(3, caseInstance.getRoleAssignments().size());

        CaseRoleAssignment ownerRole = caseInstance.getRoleAssignments().get(0);
        assertEquals("owner", ownerRole.getName());
        assertEquals(1, ownerRole.getUsers().size());
        assertEquals(USER_YODA, ownerRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", ownerRole.getGroups());

        CaseRoleAssignment contactRole = caseInstance.getRoleAssignments().get(1);
        assertEquals("contact", contactRole.getName());
        assertEquals(1, contactRole.getUsers().size());
        assertEquals(USER_JOHN, contactRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", contactRole.getGroups());

        CaseRoleAssignment participantRole = caseInstance.getRoleAssignments().get(2);
        assertEquals("participant", participantRole.getName());
        KieServerAssert.assertNullOrEmpty("Users should be empty.", participantRole.getUsers());
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", participantRole.getGroups());

        // Assert milestones
        assertNotNull(caseInstance.getMilestones());
        assertEquals(2, caseInstance.getMilestones().size());

        CaseMilestone milestone = caseInstance.getMilestones().get(0);
        assertNotNull(milestone.getIdentifier());
        assertEquals("Milestone2", milestone.getName());
        assertEquals("Available", milestone.getStatus());
        assertNull(milestone.getAchievedAt());
        assertFalse(milestone.isAchieved());

        milestone = caseInstance.getMilestones().get(1);
        assertEquals("2", milestone.getIdentifier());
        assertEquals("Milestone1", milestone.getName());
        assertEquals("Completed", milestone.getStatus());
        assertNotNull(milestone.getAchievedAt());
        assertTrue(milestone.isAchieved());


    }

    @Test
    public void testGetCaseInstanceCarInsuranceClaimCaseWithData() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId, true, true, true, true);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        KieServerAssert.assertNullOrEmpty("Milestones should be empty.", caseInstance.getMilestones());

        // Assert case file
        assertNotNull(caseInstance.getCaseFile());
        assertEquals("first case started", caseInstance.getCaseFile().getData().get("s"));

        // Assert role assignments
        assertNotNull(caseInstance.getRoleAssignments());
        assertEquals(4, caseInstance.getRoleAssignments().size());

        CaseRoleAssignment ownerRole = caseInstance.getRoleAssignments().get(0);
        assertEquals("owner", ownerRole.getName());
        assertEquals(1, ownerRole.getUsers().size());
        assertEquals(USER_YODA, ownerRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", ownerRole.getGroups());

        CaseRoleAssignment insuredRole = caseInstance.getRoleAssignments().get(1);
        assertEquals("insured", insuredRole.getName());
        assertEquals(1, insuredRole.getUsers().size());
        assertEquals(USER_YODA, insuredRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", insuredRole.getGroups());

        CaseRoleAssignment assessorRole = caseInstance.getRoleAssignments().get(2);
        assertEquals("assessor", assessorRole.getName());
        assertEquals(USER_YODA, assessorRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", assessorRole.getGroups());

        CaseRoleAssignment insuranceRepresentativeRole = caseInstance.getRoleAssignments().get(3);
        assertEquals("insuranceRepresentative", insuranceRepresentativeRole.getName());
        assertEquals(1, insuranceRepresentativeRole.getUsers().size());
        assertEquals(USER_JOHN, insuranceRepresentativeRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", insuranceRepresentativeRole.getGroups());

        // Assert stages
        assertNotNull(caseInstance.getStages());
        assertEquals(1, caseInstance.getStages().size());

        CaseStage stage = caseInstance.getStages().get(0);
        assertNotNull(stage.getIdentifier());
        assertEquals("Build claim report", stage.getName());
        assertEquals("Active", stage.getStatus());

        List<NodeInstance> activeNodes = stage.getActiveNodes();
        assertEquals(1, activeNodes.size());
        assertEquals("Provide accident information", activeNodes.get(0).getName());
        assertEquals("HumanTaskNode", activeNodes.get(0).getNodeType());

        assertEquals(2, stage.getAdHocFragments().size());
    }

    @Test
    public void testGetCaseInstances() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotEquals(caseId, caseId2);

        caseInstances = caseClient.getCaseInstances(0, 10);
        assertEquals(2, caseInstances.size());

        List<String> mappedInstances = caseInstances.stream().map(CaseInstance::getCaseId).collect(Collectors.toList());
        assertTrue(mappedInstances.contains(caseId));
        assertTrue(mappedInstances.contains(caseId2));

        caseInstances = caseClient.getCaseInstances(0, 1);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstances(1, 1);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);
    }

    @Test
    public void testGetCaseInstancesSorting() {
        String hrCaseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String claimCaseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotEquals(hrCaseId, claimCaseId);

        List<CaseInstance> caseInstances = caseClient.getCaseInstances(0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(claimCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertEquals(2, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());
        assertEquals(claimCaseId, caseInstances.get(1).getCaseId());
    }

    @Test
    public void testGetCaseInstancesByStatus() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.CANCELLED.getName()), 0, 1000);
        assertNotNull(caseInstances);
        int abortedCaseInstanceCount = caseInstances.size();

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.CANCELLED.getName()), 0, 1000);
        assertNotNull(caseInstances);
        assertEquals(abortedCaseInstanceCount + 1, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByStatusSorting() {
        String hrCaseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String claimCaseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotEquals(hrCaseId, claimCaseId);

        List<CaseInstance> caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.OPEN.getName()), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(claimCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.OPEN.getName()), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(Arrays.asList(CaseStatus.OPEN.getName()), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertEquals(2, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());
        assertEquals(claimCaseId, caseInstances.get(1).getCaseId());
    }
    
    @Test
    public void testGetCaseInstancesWithData() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        List<String> statuses = Collections.singletonList(CaseStatus.OPEN.getName());
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(statuses, 0, 1, "", false, false);
        assertEquals(1, caseInstances.size());
        CaseInstance instance = caseInstances.get(0);
        assertEquals(caseId, instance.getCaseId());
        assertNull(instance.getCaseFile());
        caseInstances = caseClient.getCaseInstances(statuses, 0, 1, "", false, true);
        instance = caseInstances.get(0);
        assertEquals(caseId, instance.getCaseId());
        assertNotNull (instance.getCaseFile());
        assertEquals("first case started", instance.getCaseFile().getData().get("s"));
    }

    @Test
    public void testByPassAuthCaseTasksAssignedAsPotentialOwnerWithEmptyUser() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));
        try {
            List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertEquals("Hello1", tasks.get(0).getName());

            tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, "", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertEquals("Hello1", tasks.get(0).getName());

            tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_JOHN, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            changeUser(USER_JOHN);

            tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, "", 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

        } finally {
            changeUser(USER_YODA);
            caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
        }

    }

    @Test
    @Category({JEEOnly.class, WildflyOnly.class})
    public void testFindCaseTasksAssignedAsPotentialOwnerByPassAuth() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertTaskListAsPotentialOwner(caseId, USER_YODA, USER_JOHN, true);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        changeUser(USER_JOHN);

        caseId = startUserTaskCase(USER_JOHN, USER_YODA);
        assertNotNull(caseId);
        assertFalse(caseId.isEmpty());

        assertTaskListAsPotentialOwner(caseId, USER_JOHN, USER_YODA, false);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    @Category({JEEOnly.class, WildflyOnly.class})
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
        changeUser(USER_YODA);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    @Category({JEEOnly.class, WildflyOnly.class})
    public void testFindCaseTasksAssignedAsStakeHolderByPassAuth() throws Exception {
        assertEquals(USER_YODA, configuration.getUserName()); // Check current authenticated user is yoda
        String caseId = startUserTaskCase(CaseFile.builder()
                                                  .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                                                  .addUserAssignments(CASE_CONTACT_ROLE, USER_JOHN)
                                                  .addUserAssignments(CASE_PARTICIPANT_ROLE, USER_MARY)
                                                  .build());

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));
        assertTaskListAsStakeHolder(caseId, USER_MARY);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        changeUser(USER_JOHN);
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
    
    @Test
    public void testGetCaseOwnedWithData() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        List<String> statuses = Collections.singletonList(CaseStatus.OPEN.getName());
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA,statuses, 0, 1, "", false, false);
        assertEquals(1, caseInstances.size());
        CaseInstance instance = caseInstances.get(0);
        assertEquals(caseId, instance.getCaseId());
        assertNull(instance.getCaseFile());
        caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA,statuses, 0, 1, "", false, true);
        instance = caseInstances.get(0);
        assertEquals(caseId, instance.getCaseId());
        assertNotNull (instance.getCaseFile());
        assertEquals("first case started", instance.getCaseFile().getData().get("s"));
    }

    @Test
    public void testGetCaseInstancesByNotExistingContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer("not-existing-container", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);
    }

    @Test
    public void testGetCaseInstancesByContainerSorting() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false, true);
        assertNotNull(caseInstances);
        assertEquals(2, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);
        assertNotNull(caseInstances.get(0).getCaseFile());
        assertCarInsuranceCaseInstance(caseInstances.get(1), caseId2, USER_YODA);
        assertNotNull(caseInstances.get(1).getCaseFile());
    }

    @Test
    public void testGetCaseInstancesByDefinitionNotExistingContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition("not-existing-container", CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByNotExistingDefinition() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, "not-existing-case", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByDefinition() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByDefinitionSorting() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startUserTaskCase(USER_YODA, USER_JOHN);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false, true);
        assertNotNull(caseInstances);
        assertEquals(2, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId2, USER_YODA);
        assertNotNull(caseInstances.get(0).getCaseFile());
        assertHrCaseInstance(caseInstances.get(1), caseId, USER_YODA);
        assertNotNull(caseInstances.get(1).getCaseFile());
    }

    @Test
    public void testGetCaseInstancesOwnedByNotExistingUser() throws Exception {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy("not-existing-user", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesOwnedBy() throws Exception {
        // Test is using user authentication, isn't available for local execution(which has mocked authentication info).
        Assume.assumeFalse(TestConfig.isLocalServer());

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        try {
            changeUser(USER_YODA);
            String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
            changeUser(USER_JOHN);
            String caseId2 = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

            changeUser(USER_YODA);
            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1);
            assertNotNull(caseInstances);
            assertEquals(1, caseInstances.size());
            assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1);
            assertNotNull(caseInstances);
            assertEquals(0, caseInstances.size());

            changeUser(USER_JOHN);
            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_JOHN, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
            assertNotNull(caseInstances);
            assertEquals(1, caseInstances.size());
            assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_JOHN);
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testGetCaseInstancesOwnedBySorting() throws Exception {
        String hrCaseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String claimCaseId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), claimCaseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), hrCaseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertNotNull(caseInstances);
        assertEquals(2, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), hrCaseId, USER_YODA);
        assertCarInsuranceCaseInstance(caseInstances.get(1), claimCaseId, USER_YODA);
    }

    @Test
    public void testAdHocFragments() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        List<CaseAdHocFragment> caseAdHocFragments = caseClient.getAdHocFragments(CONTAINER_ID, caseId);
        assertEquals(3, caseAdHocFragments.size());

        Map<String, CaseAdHocFragment> mappedAdHocFragments = caseAdHocFragments.stream().collect(Collectors.toMap(CaseAdHocFragment::getName, d -> d));
        assertTrue(mappedAdHocFragments.containsKey("Milestone1"));
        assertTrue(mappedAdHocFragments.containsKey("Milestone2"));
        assertTrue(mappedAdHocFragments.containsKey("Hello2"));
        assertEquals("MilestoneNode", mappedAdHocFragments.get("Milestone1").getType());
        assertEquals("MilestoneNode", mappedAdHocFragments.get("Milestone2").getType());
        assertEquals("HumanTaskNode", mappedAdHocFragments.get("Hello2").getType());
    }

    @Test
    public void testAdHocFragmentsNotExistingCase() {
        try {
            caseClient.getAdHocFragments(CONTAINER_ID, "not-existing-case");
            fail("Should have failed because of non existing case Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testAdHocFragmentsNotExistingContainer() {
        try {
            caseClient.getAdHocFragments("not-existing-container", CASE_HR_DEF_ID);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetProcessInstances() {
        String userTaskCase = startUserTaskCase(USER_YODA, USER_JOHN);
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        List<ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(STATE_ACTIVE), 0, 1);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertHrProcessInstance(processInstances.get(0), userTaskCase);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(STATE_ACTIVE), 1, 1);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertCarInsuranceProcessInstance(processInstances.get(0), carInsuranceClaimCase);

        caseClient.cancelCaseInstance(CONTAINER_ID, userTaskCase);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(STATE_ABORTED), 0, 10);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertHrProcessInstance(processInstances.get(0), userTaskCase, STATE_ABORTED);
    }

    @Test
    public void testGetProcessInstancesNotExistingCase() {
        List<ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, "not-existing-case", Arrays.asList(STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetProcessInstancesNotExistingContainer() {
        List<ProcessInstance> processInstances = caseClient.getProcessInstances("not-existing-container", CASE_HR_DEF_ID, Arrays.asList(STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetProcessInstancesSorting() {
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);
        caseClient.addDynamicSubProcess(CONTAINER_ID, carInsuranceClaimCase, DATA_VERIFICATION_DEF_ID, new HashMap<>());

        List<ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(STATE_ACTIVE, STATE_COMPLETED), 0, 1, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), CLAIM_CASE_DEF_ID);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(STATE_ACTIVE, STATE_COMPLETED), 1, 1, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), DATA_VERIFICATION_DEF_ID);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(STATE_ACTIVE, STATE_COMPLETED), 0, 10, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, false);
        assertNotNull(processInstances);
        assertEquals(2, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), DATA_VERIFICATION_DEF_ID);
        assertEquals(processInstances.get(1).getProcessId(), CLAIM_CASE_DEF_ID);
    }

    @Test
    public void testGetActiveProcessInstances() {
        String userTaskCase = startUserTaskCase(USER_YODA, USER_JOHN);
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, userTaskCase, 0, 1);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertHrProcessInstance(processInstances.get(0), userTaskCase);

        processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, userTaskCase, 1, 1);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());

        processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, carInsuranceClaimCase, 0, 10);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertCarInsuranceProcessInstance(processInstances.get(0), carInsuranceClaimCase);

        caseClient.cancelCaseInstance(CONTAINER_ID, userTaskCase);

        processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, userTaskCase, 0, 1);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetActiveProcessInstancesNotExistingCase() {
        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, "not-existing-case", 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetActiveProcessInstancesNotExistingContainer() {
        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances("not-existing-container", CASE_HR_DEF_ID, 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetActiveProcessInstancesSorting() {
        String caseId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);
        caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, USER_TASK_DEF_ID, Collections.emptyMap());

        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10,
                CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, true);
        Assertions.assertThat(processInstances).isNotNull().hasSize(2);
        Assertions.assertThat(processInstances).extracting(ProcessInstance::getProcessId)
                .containsExactly(CLAIM_CASE_DEF_ID, USER_TASK_DEF_ID);

        processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10,
                CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, false);
        assertNotNull(processInstances);
        Assertions.assertThat(processInstances).isNotNull().hasSize(2);
        Assertions.assertThat(processInstances).extracting(ProcessInstance::getProcessId)
                .containsExactly(USER_TASK_DEF_ID, CLAIM_CASE_DEF_ID);

        processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10,
                CaseServicesClient.SORT_BY_PROCESS_NAME, true);
        Assertions.assertThat(processInstances).isNotNull().hasSize(2);
        Assertions.assertThat(processInstances).extracting(ProcessInstance::getProcessId)
                .containsExactly(CLAIM_CASE_DEF_ID, USER_TASK_DEF_ID);
    }

    @Test
    public void testGetActiveProcessInstancesSortingNotExistingField() {
        String caseId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        Assertions.assertThatThrownBy(() -> caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10,
                "xyz", true)).isInstanceOf(KieServicesException.class);
    }

    @Test
    public void testGetActiveProcessInstancesSortingNotExistingCase() {
        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, "not-existing-case", 0, 10,
                CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetActiveProcessInstancesSortingNotExistingContainer() {
        List<ProcessInstance> processInstances = caseClient.getActiveProcessInstances("not-existing-container",
                CASE_HR_DEF_ID, 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testCreateCaseWithCaseFileAndTriggerMilestones() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertHrCaseInstance(caseInstance, caseId, USER_YODA);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, null, 0, 10);
        assertEquals(1, caseInstances.size());

        List<CaseMilestone> milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
        assertNotNull(milestones);
        assertEquals(0, milestones.size());

        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Milestone1", null);

        milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
        assertNotNull(milestones);
        assertEquals(1, milestones.size());

        CaseMilestone milestone = milestones.get(0);
        assertNotNull(milestone);
        assertEquals("Milestone1", milestone.getName());
        assertEquals(true, milestone.isAchieved());
        assertEquals("2", milestone.getIdentifier());
        assertNotNull(milestone.getAchievedAt());

        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Milestone2", null);
        milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
        assertNotNull(milestones);
        assertEquals(1, milestones.size());

        milestones = caseClient.getMilestones(CONTAINER_ID, caseId, false, 0, 10);
        assertNotNull(milestones);
        assertEquals(2, milestones.size());

        Map<String, CaseMilestone> mappedMilestones = milestones.stream().collect(Collectors.toMap(CaseMilestone::getName, d -> d));
        assertTrue(mappedMilestones.containsKey("Milestone1"));
        assertTrue(mappedMilestones.containsKey("Milestone2"));

        assertTrue(mappedMilestones.get("Milestone1").isAchieved());
        assertFalse(mappedMilestones.get("Milestone2").isAchieved());

        caseInstances = caseClient.getCaseInstances(0, 10);
        assertEquals(1, caseInstances.size());

        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        // now auto complete milestone by inserting data
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "dataComplete", true);
        milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
        assertNotNull(milestones);
        assertEquals(2, milestones.size());
    }

    @Test
    public void testGetCaseMilestonesNotExistingContainer() {
        try {
            caseClient.getMilestones("not-existing-container", CASE_HR_DEF_ID, false, 0, 10);
            fail("Should have failed because of not existing case Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseMilestonesNotExistingCase() {
        try {
            caseClient.getMilestones(CONTAINER_ID, "not-existing-case", false, 0, 10);
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testCreateCaseWithCaseFileAndDynamicActivities() {
        String caseId = startUserTaskCase(USER_JOHN, USER_YODA);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        List<NodeInstance> activeNodes = caseClient.getActiveNodes(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());

        NodeInstance activeNode = activeNodes.get(0);
        assertNotNull(activeNode);
        assertEquals("Hello1", activeNode.getName());

        List<NodeInstance> completedNodes = caseClient.getCompletedNodes(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(completedNodes);
        assertEquals(0, completedNodes.size());

        List<ProcessInstance> instances = caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        ProcessInstance pi = instances.get(0);
        assertEquals(CASE_HR_DEF_ID, pi.getProcessId());
        assertEquals(caseId, pi.getCorrelationKey());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", "text data");

        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "dynamic task", "simple description", USER_YODA, null, parameters);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("dynamic task", task.getName());
        assertEquals("simple description", task.getDescription());
        assertEquals(Status.Reserved.toString(), task.getStatus());
        assertEquals(USER_YODA, task.getActualOwner());

        activeNodes = caseClient.getActiveNodes(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(activeNodes);
        assertEquals(2, activeNodes.size());

        List<String> nodeNames = activeNodes.stream().map(n -> n.getName()).collect(toList());
        assertTrue(nodeNames.contains("[Dynamic] dynamic task"));
        assertTrue(nodeNames.contains("Hello1"));

        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, null);

        completedNodes = caseClient.getCompletedNodes(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(completedNodes);
        assertEquals(1, completedNodes.size());

        NodeInstance completedNode = completedNodes.get(0);
        assertNotNull(completedNode);
        assertEquals("[Dynamic] dynamic task", completedNode.getName());

        caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, "DataVerification", parameters);

        instances = caseClient.getProcessInstances(CONTAINER_ID, caseId, Arrays.asList(1, 2, 3), 0, 10);
        assertNotNull(instances);
        assertEquals(2, instances.size());
    }

    @Test
    public void testCreateCaseWithCaseFileWithComments() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        List<CaseComment> comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());

        String commentId = caseClient.addComment(CONTAINER_ID, caseId, USER_YODA, "first comment");
        assertNotNull(commentId);
        
        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        CaseComment comment = comments.get(0);
        assertNotNull(comment);
        assertEquals(USER_YODA, comment.getAuthor());
        assertEquals("first comment", comment.getText());
        assertNotNull(comment.getAddedAt());
        assertNotNull(comment.getId());
        
        assertEquals(commentId, comment.getId());

        caseClient.updateComment(CONTAINER_ID, caseId, comment.getId(), USER_YODA, "updated comment");
        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        comment = comments.get(0);
        assertNotNull(comment);
        assertEquals(USER_YODA, comment.getAuthor());
        assertEquals("updated comment", comment.getText());
        assertNotNull(comment.getAddedAt());
        assertNotNull(comment.getId());

        caseClient.removeComment(CONTAINER_ID, caseId, comment.getId());

        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    public void testGetCommentPagination() {
        int pageSize = 20;

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        for (int i = 0; i < 55; i++) {
            caseClient.addComment(CONTAINER_ID, caseId, USER_YODA, "comment" + i);
        }

        List<CaseComment> firstPage = caseClient.getComments(CONTAINER_ID, caseId, 0, pageSize);
        assertNotNull(firstPage);
        assertEquals(20, firstPage.size());
        Iterator<CaseComment> firstPageIter = firstPage.iterator();
        for (int i = 0; firstPageIter.hasNext(); i++) {
            assertComment(firstPageIter.next(), USER_YODA, "comment" + i);
        }

        List<CaseComment> secondPage = caseClient.getComments(CONTAINER_ID, caseId, 1, pageSize);
        assertNotNull(secondPage);
        assertEquals(20, secondPage.size());
        Iterator<CaseComment> secondPageIter = secondPage.iterator();
        for (int i = 20; secondPageIter.hasNext(); i++) {
            assertComment(secondPageIter.next(), USER_YODA, "comment" + i);
        }

        List<CaseComment> thirdPage = caseClient.getComments(CONTAINER_ID, caseId, 2, pageSize);
        assertNotNull(thirdPage);
        assertEquals(15, thirdPage.size());
        Iterator<CaseComment> thirdPageIter = thirdPage.iterator();
        for (int i = 40; thirdPageIter.hasNext(); i++) {
            assertComment(thirdPageIter.next(), USER_YODA, "comment" + i);
        }
    }

    @Test
    public void testGetCaseCommentsNotExistingContainer() {
        try {
            caseClient.getComments("not-existing-container", CASE_HR_DEF_ID, 0, 10);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseCommentsNotExistingCase() {
        try {
            caseClient.getComments(CONTAINER_ID, "not-existing-case", 0, 10);
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testAddCaseCommentNotExistingContainer() {
        try {
            caseClient.addComment("not-existing-container", CASE_HR_DEF_ID, USER_YODA, "Random comment.");
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testAddCaseCommentNotExistingCase() {
        try {
            caseClient.addComment(CONTAINER_ID, "not-existing-case", USER_YODA, "Random comment.");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testUpdateCaseCommentNotExistingContainer() {
        try {
            caseClient.updateComment("not-existing-container", CASE_HR_DEF_ID, "random-id", USER_YODA, "Random comment.");
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testUpdateCaseCommentNotExistingCase() {
        try {
            caseClient.updateComment(CONTAINER_ID, "not-existing-case", "random-id", USER_YODA, "Random comment.");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testUpdateNotExistingCaseComment() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        try {
            caseClient.updateComment(CONTAINER_ID, caseId, "not-existing-id", USER_YODA, "Random comment.");
            fail("Should have failed because of not existing comment Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testRemoveCaseCommentNotExistingContainer() {
        try {
            caseClient.removeComment("not-existing-container", CASE_HR_DEF_ID, "random-id");
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testRemoveCaseCommentNotExistingCase() {
        try {
            caseClient.removeComment(CONTAINER_ID, "not-existing-case", "random-id");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testRemoveNotExistingCaseComment() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        try {
            caseClient.removeComment(CONTAINER_ID, caseId, "not-existing-id");
            fail("Should have failed because of not existing comment Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testCreateDifferentTypesCases() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        String caseClaimId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA, USER_YODA);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertNotNull(caseClaimId);
        assertTrue(caseClaimId.startsWith(CLAIM_CASE_ID_PREFIX));

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(2, caseInstances.size());

        List<String> caseDefs = caseInstances.stream().map(c -> c.getCaseDefinitionId()).collect(toList());
        assertTrue(caseDefs.contains(CASE_HR_DEF_ID));
        assertTrue(caseDefs.contains(CLAIM_CASE_DEF_ID));

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(1, caseInstances.size());

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        CaseStage caseStage = stages.get(0);
        assertEquals("Build claim report", caseStage.getName());
        assertEquals(2, caseStage.getAdHocFragments().size());

        List<CaseRoleAssignment> roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(4, roles.size());

        Map<String, CaseRoleAssignment> mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));
        assertTrue(mappedRoles.containsKey(CASE_OWNER_ROLE));
        assertTrue(mappedRoles.containsKey(CASE_INSURED_ROLE));
        assertTrue(mappedRoles.containsKey(CASE_INS_REP_ROLE));
        assertTrue(mappedRoles.containsKey(CASE_ASSESSOR_ROLE));

        CaseRoleAssignment ownerRole = mappedRoles.get(CASE_OWNER_ROLE);
        assertTrue(ownerRole.getUsers().contains(USER_YODA));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", ownerRole.getGroups());

        CaseRoleAssignment insuredRole = mappedRoles.get(CASE_INSURED_ROLE);
        assertTrue(insuredRole.getUsers().contains(USER_JOHN));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", insuredRole.getGroups());

        CaseRoleAssignment insRepRole = mappedRoles.get(CASE_INS_REP_ROLE);
        assertTrue(insRepRole.getUsers().contains(USER_YODA));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", insRepRole.getGroups());

        CaseRoleAssignment assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        assertTrue(assessorRole.getUsers().contains(USER_YODA));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", assessorRole.getGroups());

        caseClient.assignUserToRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, USER_MARY);
        caseClient.assignGroupToRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, "managers");

        roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(4, roles.size());
        mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));

        assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        assertTrue(assessorRole.getUsers().contains(USER_MARY));
        assertTrue(assessorRole.getGroups().contains("managers"));

        caseClient.removeUserFromRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, USER_MARY);
        caseClient.removeUserFromRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, USER_YODA);
        caseClient.removeGroupFromRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, "managers");

        roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(4, roles.size());
        mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));

        assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        KieServerAssert.assertNullOrEmpty("Users should be empty", assessorRole.getUsers());
        KieServerAssert.assertNullOrEmpty("Groups should be empty", assessorRole.getGroups());
    }

    @Test
    public void testAssignUserToRoleNotExistingCase() {
        assertClientException(
                () -> caseClient.assignUserToRole(CONTAINER_ID, "not-existing-case", CASE_ASSESSOR_ROLE, USER_YODA),
                404,
                "Could not find case instance \"not-existing-case\"",
                "Case with id not-existing-case not found");
    }

    @Test
    public void testAssignGroupToRoleNotExistingCase() {
        assertClientException(
                () -> caseClient.assignGroupToRole(CONTAINER_ID, "not-existing-case", CASE_ASSESSOR_ROLE, "managers"),
                404,
                "Could not find case instance \"not-existing-case\"",
                "Case with id not-existing-case not found");
    }

    @Test
    public void testRemoveUserFromRoleNotExistingCase() {
        assertClientException(
                () -> caseClient.removeUserFromRole(CONTAINER_ID, "not-existing-case", CASE_ASSESSOR_ROLE, USER_YODA),
                404,
                "Could not find case instance \"not-existing-case\"",
                "Case with id not-existing-case not found");
    }

    @Test
    public void testRemoveGroupFromRoleNotExistingCase() {
        assertClientException(
                () -> caseClient.removeGroupFromRole(CONTAINER_ID, "not-existing-case", CASE_ASSESSOR_ROLE, "managers"),
                404,
                "Could not find case instance \"not-existing-case\"",
                "Case with id not-existing-case not found");
    }

    @Test
    public void testCaseRolesCardinality() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .data(data)
                .addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);
        
        // Try to add second user to insured role with cardinality 1
        assertClientException(
                () -> caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_INSURED_ROLE, USER_YODA),
                400,
                "Cannot add more users for role " + CASE_INSURED_ROLE);
    }

    @Test
    public void testGetCaseStages() {
        String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseClaimId);

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, "claimReportDone", Boolean.TRUE);

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, false, 0, 10);
        assertEquals(3, stages.size());
        assertBuildClaimReportCaseStage(stages.get(0), "Completed");
        assertClaimAssesmentCaseStage(stages.get(1), "Active");
        assertEscalateRejectedClaimCaseStage(stages.get(2), "Available");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertClaimAssesmentCaseStage(stages.iterator().next(), "Active");
    }

    @Test
    public void testCompleteCaseStageAndAbort() {
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());

        String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseClaimId);

        caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, "claimReportDone", Boolean.TRUE);

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        caseClient.cancelCaseInstance(CONTAINER_ID, caseClaimId);

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());
    }

    @Test
    public void testGetCaseStagesNotExistingContainer() {
        try {
            caseClient.getStages("not-existing-container", CASE_HR_DEF_ID, false, 0, 10);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseStagesNotExistingCase() {
        try {
            caseClient.getStages(CONTAINER_ID, "not-existing-case", false, 0, 10);
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetCaseTasksAsPotOwner() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10, "t.name", true);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        String caseId2 = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId2);
        assertTrue(caseId2.startsWith(CASE_HR_ID_PREFIX));

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId2, USER_YODA, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10, "t.name", true);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId2);
    }

    @Test
    public void testFindCaseTasksAssignedAsPotentialOwnerNotExistingCase() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        Assertions.assertThat(caseId).isNotNull().startsWith(CASE_HR_ID_PREFIX);

        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsPotentialOwner("not-existing-case",
                USER_YODA, 0, 10);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner("not-existing-case", USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner("not-existing-case", USER_YODA, 0, 10,
                "t.id", true);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner("not-existing-case", USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10,
                "t.name", false);
        Assertions.assertThat(instances).isNotNull().isEmpty();
    }

    @Test
    public void testFindCaseTasksAssignedAsPotentialOwnerNoTasks() {
        String caseId = startUserTaskCase(USER_JOHN, USER_MARY);
        Assertions.assertThat(caseId).isNotNull().startsWith(CASE_HR_ID_PREFIX);

        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10, "t.id", true);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10, "t.name", false);
        Assertions.assertThat(instances).isNotNull().isEmpty();
    }

    @Test
    public void testFindCaseTasksAssignedAsPotentialOwnerPaging() {
        String caseId = startUserTaskCase(USER_JOHN, USER_YODA);
        Assertions.assertThat(caseId).isNotNull().startsWith(CASE_HR_ID_PREFIX);

        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskA", null, USER_YODA, null, Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskB", null, USER_YODA, null, Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskC", null, USER_YODA, null, Collections.emptyMap());

        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 3);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskA", "TaskB", "TaskC");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 1, 3);
        Assertions.assertThat(instances).isNotNull().isEmpty();

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 2, "t.id", true);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskA", "TaskB");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 1, 2, "t.id", true);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskC");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 1, "t.name", false);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskC");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 1, 1, "t.name", false);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskB");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 2, 1, "t.name", false);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsOnly("TaskA");
    }

    @Test
    public void testFindCaseTasksAssignedAsPotentialOwnerSorting() {
        String caseId = startUserTaskCase(USER_JOHN, USER_YODA);
        Assertions.assertThat(caseId).isNotNull().startsWith(CASE_HR_ID_PREFIX);

        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskA", null, USER_YODA, null, Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskB", null, USER_YODA, null, Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskC", null, USER_YODA, null, Collections.emptyMap());

        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10, "t.id", true);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsExactly("TaskA", "TaskB", "TaskC");

        instances = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA,
                Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10, "t.name", false);
        Assertions.assertThat(instances).isNotNull()
                .extracting(TaskSummary::getName).containsExactly("TaskC", "TaskB", "TaskA");
    }

    @Test
    public void testGetCaseTasksAsBusinessAdmin() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        changeUser(USER_ADMINISTRATOR);
        List<TaskSummary> instances = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        changeUser(USER_YODA);
        String caseId2 = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId2);
        assertTrue(caseId2.startsWith(CASE_HR_ID_PREFIX));

        changeUser(USER_ADMINISTRATOR);
        instances = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        instances = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId2, USER_ADMINISTRATOR, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10, "t.name", true);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        changeUser(USER_YODA);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId2);
    }

    @Test
    public void testGetCaseTasksAsStakeholder() throws Exception {
        // Test is using user authentication, isn't available for local execution(which has mocked authentication info).
        Assume.assumeFalse(TestConfig.isLocalServer());
        String caseId = startUserTaskCase(USER_JOHN, USER_YODA);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", "text data");
        parameters.put(TASK_STAKEHOLDER_ID, USER_YODA);

        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "dynamic task", "simple description", USER_JOHN, null, parameters);

        List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsStakeholder(caseId, USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("dynamic task", task.getName());
        assertEquals("simple description", task.getDescription());
        assertEquals(Status.Reserved.toString(), task.getStatus());
        assertEquals(USER_JOHN, task.getActualOwner());

        // start another case
        String caseId2 = startUserTaskCase(USER_JOHN, USER_YODA);
        assertNotNull(caseId2);
        assertTrue(caseId2.startsWith(CASE_HR_ID_PREFIX));
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId2, "dynamic task", "simple description", USER_JOHN, null, parameters);

        tasks = caseClient.findCaseTasksAssignedAsStakeholder(caseId, USER_YODA, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        tasks = caseClient.findCaseTasksAssignedAsStakeholder(caseId2, USER_YODA, Arrays.asList(Ready.toString(), Status.Reserved.toString()), 0, 10, "t.name", true);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        changeUser(USER_JOHN);
        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testGetTriggerTask() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        changeUser(USER_JOHN);
        List<TaskSummary> caseTasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_JOHN, 0, 10);
        assertNotNull(caseTasks);
        assertEquals(0, caseTasks.size());

        changeUser(USER_YODA);
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, HELLO_2_TASK, Collections.EMPTY_MAP);

        changeUser(USER_JOHN);
        caseTasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_JOHN, 0, 10);
        assertNotNull(caseTasks);
        assertEquals(1, caseTasks.size());

        TaskSummary task = caseTasks.get(0);
        assertNotNull(task);
        assertEquals(HELLO_2_TASK, task.getName());
        assertEquals(USER_JOHN, task.getActualOwner());

        changeUser(USER_YODA);
        caseTasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10);
        assertNotNull(caseTasks);
        assertEquals(1, caseTasks.size());

        task = caseTasks.get(0);
        assertNotNull(task);
        assertEquals(HELLO_1_TASK, task.getName());
        assertEquals(USER_YODA, task.getActualOwner());

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testTriggerTaskIntoStage() throws Exception {
        String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseClaimId);

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        CaseStage stage = stages.iterator().next();
        assertBuildClaimReportCaseStage(stage, "Active");

        List<TaskSummary> tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseClaimId, USER_YODA, 0, 10);
        assertNotNull(tasks);
        int countOfTaskBefore = tasks.size();

        assertNotNull(stage.getIdentifier());
        caseClient.triggerAdHocFragmentInStage(CONTAINER_ID, caseClaimId, stage.getIdentifier(), SUBMIT_POLICE_REPORT_TASK, Collections.EMPTY_MAP);

        tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseClaimId, USER_YODA, 0, 10);
        assertNotNull(tasks);
        assertEquals(countOfTaskBefore + 1, tasks.size());

        TaskSummary task = tasks.get(0);
        assertNotNull(task);
        assertEquals(SUBMIT_POLICE_REPORT_TASK, task.getName());

        caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, "claimReportDone", Boolean.TRUE);
        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, false, 0, 10);
        assertEquals(3, stages.size());
        assertBuildClaimReportCaseStage(stages.get(0), "Completed");
        assertClaimAssesmentCaseStage(stages.get(1), "Active");
        assertEscalateRejectedClaimCaseStage(stages.get(2), "Available");

        tasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseClaimId, USER_YODA, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        task = tasks.get(0);
        assertNotEquals(SUBMIT_POLICE_REPORT_TASK, task.getName());

        assertClientException(
                () -> caseClient.triggerAdHocFragmentInStage(CONTAINER_ID, caseClaimId, stage.getIdentifier(), SUBMIT_POLICE_REPORT_TASK, Collections.EMPTY_MAP),
                404,
                "No stage found with id " + stage.getIdentifier()
        );

        caseClient.destroyCaseInstance(CONTAINER_ID, caseClaimId);
    }

    @Test
    public void testTriggerTaskInCanceledCase() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        List<TaskSummary> caseTasks = caseClient.findCaseTasksAssignedAsPotentialOwner(caseId, USER_YODA, 0, 10);
        assertNotNull(caseTasks);
        assertEquals(1, caseTasks.size());

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        assertClientException(
                () -> caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, HELLO_2_TASK, Collections.EMPTY_MAP),
                404,
                "Could not find case instance \"" + caseId + "\"",
                "Case with id " + caseId + " was not found");

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testCaseInstanceAuthorization() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        changeUser(USER_JOHN);

        try {
            caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            fail("User john is not an owner so is not allowed to cancel case instance");
        } catch (KieServicesException e) {
            String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("User " + USER_JOHN + " is not authorized"));
        }
        try {
            caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
            fail("User john is not an owner so is not allowed to destroy case instance");
        } catch (KieServicesException e) {
            String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("User " + USER_JOHN + " is not authorized"));
        }

        changeUser(USER_YODA);

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);
    }

    @Test
    public void testGetProcessDefinitionsByContainer() {
        List<ProcessDefinition> definitions = caseClient.findProcessesByContainerId(CONTAINER_ID, 0, 10);
        assertNotNull(definitions);
        assertEquals(3, definitions.size());

        List<String> mappedDefinitions = definitions.stream().map(ProcessDefinition::getId).collect(Collectors.toList());
        assertTrue(mappedDefinitions.contains("DataVerification"));
        assertTrue(mappedDefinitions.contains("hiring"));

        definitions = caseClient.findProcessesByContainerId(CONTAINER_ID, 0, 1,
                CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals("DataVerification", definitions.get(0).getId());

        definitions = caseClient.findProcessesByContainerId(CONTAINER_ID, 1, 1,
                CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, true);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals("hiring", definitions.get(0).getId());

        definitions = caseClient.findProcessesByContainerId(CONTAINER_ID, 0, 1,
                CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, false);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals(USER_TASK_DEF_ID, definitions.get(0).getId());
    }

    @Test
    public void testGetProcessDefinitions() {
        List<ProcessDefinition> definitions = caseClient.findProcesses("hir", 0, 10);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals("hiring", definitions.get(0).getId());

        definitions = caseClient.findProcesses(1, 1, CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, false);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals("hiring", definitions.get(0).getId());

        definitions = caseClient.findProcesses(0, 10);
        assertNotNull(definitions);
        assertEquals(3, definitions.size());

        List<String> mappedDefinitions = definitions.stream().map(ProcessDefinition::getId).collect(Collectors.toList());
        assertTrue(mappedDefinitions.contains("DataVerification"));
        assertTrue(mappedDefinitions.contains("hiring"));
    }

    @Test
    public void testTriggerNotExistingAdHocFragments() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertHrCaseInstance(caseInstance, caseId, USER_YODA);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, null, 0, 10);
        assertEquals(1, caseInstances.size());

        List<CaseMilestone> milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
        assertNotNull(milestones);
        assertEquals(0, milestones.size());

        final String nonExistingAdHocFragment = "not existing";
        assertClientException(
                () -> caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, nonExistingAdHocFragment, Collections.EMPTY_MAP),
                404,
                "AdHoc fragment '" + nonExistingAdHocFragment + "' not found in case " + caseId
        );
    }

    @Test
    public void testGetCaseInstanceDataItems() {
        String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseClaimId);

        List<CaseFileDataItem> dataItems = caseClient.getCaseInstanceDataItems(caseClaimId, 0, 10);
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, "claimReportDone", Boolean.TRUE);

        dataItems = caseClient.getCaseInstanceDataItems(caseClaimId, 0, 10);
        assertNotNull(dataItems);
        assertEquals(2, dataItems.size());

        CaseFileDataItem dataItem = dataItems.stream()
                                             .filter(n -> "claimReportDone".equals(n.getName()))
                                             .findAny()
                                             .get();
        assertEquals(caseClaimId, dataItem.getCaseId());
        assertEquals("claimReportDone", dataItem.getName());
        assertEquals("true", dataItem.getValue());
        assertEquals(Boolean.class.getName(), dataItem.getType());
        assertEquals(USER_YODA, dataItem.getLastModifiedBy());

        dataItems = caseClient.getCaseInstanceDataItemsByType(caseClaimId, Arrays.asList(Boolean.class.getName()), 0, 10);
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        dataItems = caseClient.getCaseInstanceDataItemsByType(caseClaimId, Arrays.asList(String.class.getName()), 0, 10);
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        dataItems = caseClient.getCaseInstanceDataItemsByName(caseClaimId, Arrays.asList("claimReportDone"), 0, 10);
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        dataItems = caseClient.getCaseInstanceDataItemsByName(caseClaimId, Arrays.asList("notExisting"), 0, 10);
        assertNotNull(dataItems);
        assertEquals(0, dataItems.size());

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, false, 0, 10);
        assertEquals(3, stages.size());
        assertBuildClaimReportCaseStage(stages.get(0), "Completed");
        assertClaimAssesmentCaseStage(stages.get(1), "Active");
        assertEscalateRejectedClaimCaseStage(stages.get(2), "Available");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertClaimAssesmentCaseStage(stages.iterator().next(), "Active");
    }

    @Test
    public void testGetCaseInstanceByData() {
        String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseClaimId);

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertBuildClaimReportCaseStage(stages.iterator().next(), "Active");

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByData("claimReportDone", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(0, caseInstances.size());

        caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, "claimReportDone", Boolean.TRUE);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(1, caseInstances.size());

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone", "false", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(0, caseInstances.size());

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone", "true", Arrays.asList(CaseStatus.OPEN.getName()), 0, 10);
        assertEquals(1, caseInstances.size());

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, false, 0, 10);
        assertEquals(3, stages.size());
        assertBuildClaimReportCaseStage(stages.get(0), "Completed");
        assertClaimAssesmentCaseStage(stages.get(1), "Active");
        assertEscalateRejectedClaimCaseStage(stages.get(2), "Available");

        stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        assertClaimAssesmentCaseStage(stages.iterator().next(), "Active");
    }

    @Test
    public void testGetCaseInstancesByDataPaging() {
        String case1 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        Assertions.assertThat(case1).isNotNull().startsWith(CLAIM_CASE_ID_PREFIX);
        String case2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        Assertions.assertThat(case2).isNotNull().startsWith(CLAIM_CASE_ID_PREFIX);
        String case3 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        Assertions.assertThat(case3).isNotNull().startsWith(CLAIM_CASE_ID_PREFIX);

        caseClient.putCaseInstanceData(CONTAINER_ID, case1, "claimReportDone", Boolean.TRUE);
        caseClient.putCaseInstanceData(CONTAINER_ID, case2, "claimReportDone", Boolean.TRUE);
        caseClient.putCaseInstanceData(CONTAINER_ID, case3, "claimReportDone", Boolean.TRUE);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 0, 3);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case1, case2, case3);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 1, 3);
        Assertions.assertThat(caseInstances).isEmpty();

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 0, 2);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case1, case2);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 1, 2);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case3);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 2, 2);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).isEmpty();

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 0, 1);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case1);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 1, 1);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case2);

        caseInstances = caseClient.getCaseInstancesByData("claimReportDone",
                Collections.singletonList(CaseStatus.OPEN.getName()), 2, 1);
        Assertions.assertThat(caseInstances).extracting(CaseInstance::getCaseId).containsOnly(case3);
    }

    @Test
    public void testCreateCaseWithCaseFileWithCommentsSorted() throws Exception {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_CONTACT_ROLE, USER_MARY);

        List<CaseComment> comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());
        try {
            caseClient.addComment(CONTAINER_ID, caseId, USER_YODA, "yoda's comment");
            changeUser(USER_JOHN);
            caseClient.addComment(CONTAINER_ID, caseId, USER_JOHN, "john's comment");
            changeUser(USER_MARY);
            caseClient.addComment(CONTAINER_ID, caseId, USER_MARY, "mary's comment");

            comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
            assertNotNull(comments);
            assertEquals(3, comments.size());

            CaseComment comment = comments.get(0);
            assertNotNull(comment);
            assertEquals(USER_YODA, comment.getAuthor());
            assertEquals("yoda's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(1);
            assertNotNull(comment);
            assertEquals(USER_JOHN, comment.getAuthor());
            assertEquals("john's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(2);
            assertNotNull(comment);
            assertEquals(USER_MARY, comment.getAuthor());
            assertEquals("mary's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comments = caseClient.getComments(CONTAINER_ID, caseId, CaseServicesClient.COMMENT_SORT_BY_AUTHOR, 0, 10);
            assertNotNull(comments);
            assertEquals(3, comments.size());

            comment = comments.get(0);
            assertNotNull(comment);
            assertEquals(USER_JOHN, comment.getAuthor());
            assertEquals("john's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(1);
            assertNotNull(comment);
            assertEquals(USER_MARY, comment.getAuthor());
            assertEquals("mary's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(2);
            assertNotNull(comment);
            assertEquals(USER_YODA, comment.getAuthor());
            assertEquals("yoda's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comments = caseClient.getComments(CONTAINER_ID, caseId, CaseServicesClient.COMMENT_SORT_BY_DATE, 0, 10);
            assertNotNull(comments);
            assertEquals(3, comments.size());

            comment = comments.get(0);
            assertNotNull(comment);
            assertEquals(USER_YODA, comment.getAuthor());
            assertEquals("yoda's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(1);
            assertNotNull(comment);
            assertEquals(USER_JOHN, comment.getAuthor());
            assertEquals("john's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());

            comment = comments.get(2);
            assertNotNull(comment);
            assertEquals(USER_MARY, comment.getAuthor());
            assertEquals("mary's comment", comment.getText());
            assertNotNull(comment.getAddedAt());
            assertNotNull(comment.getId());
        } finally {
            changeUser(USER_YODA);
        }
    }

    @Test
    public void testAddDynamicProcessToCaseNotExistingCase() {
        String invalidCaseId = "not-existing-case-id";
        assertClientException(() -> caseClient.addDynamicSubProcess(CONTAINER_ID, invalidCaseId, CLAIM_CASE_DEF_ID, null),
                404,
                "Could not find case instance \"" + invalidCaseId + "\"",
                "Case with id " + invalidCaseId + " not found");
    }

    @Test
    public void testAddDynamicProcessToCaseNotExistingProcessDefinition() {
        String invalidProcessId = "not-existing-process-id";
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .data(data)
                .addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);

        assertClientException(() -> caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, invalidProcessId, null),
                404,
                "Could not find process definition \"" + invalidProcessId + "\" in container \"" + CONTAINER_ID + "\"",
                "No process definition found with id: " + invalidProcessId);
    }
    
    @Test
    public void testCreateCaseWithCaseFileWithCommentsWithRestrictions() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        // add a contact role to yoda so it can access a case once owner role is removed
        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_CONTACT_ROLE, USER_YODA);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        List<CaseComment> comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());

        List<String> restrictions = new ArrayList<>();
        restrictions.add(CASE_OWNER_ROLE);
        
        caseClient.addComment(CONTAINER_ID, caseId, USER_YODA, "first comment", restrictions);

        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        CaseComment comment = comments.get(0);
        assertNotNull(comment);
        assertEquals(USER_YODA, comment.getAuthor());
        assertEquals("first comment", comment.getText());
        assertNotNull(comment.getAddedAt());
        assertNotNull(comment.getId());
        
        // remove yoda from owner role to simulate lack of access
        caseClient.removeUserFromRole(CONTAINER_ID, caseId, CASE_OWNER_ROLE, USER_YODA);
        
        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());

        final String commentId = comment.getId();
        assertClientException(() -> caseClient.updateComment(CONTAINER_ID, caseId, commentId, USER_YODA, "updated comment"), 403, "");
        
        // add back yoda to owner role
        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_OWNER_ROLE, USER_YODA);
        
        caseClient.updateComment(CONTAINER_ID, caseId, comment.getId(), USER_YODA, "updated comment");
        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        comment = comments.get(0);
        assertNotNull(comment);
        assertEquals(USER_YODA, comment.getAuthor());
        assertEquals("updated comment", comment.getText());
        assertNotNull(comment.getAddedAt());
        assertNotNull(comment.getId());

        final String updatedCommentId = comment.getId();
        // remove yoda from owner role to simulate lack of access
        caseClient.removeUserFromRole(CONTAINER_ID, caseId, CASE_OWNER_ROLE, USER_YODA);
        
        assertClientException(() -> caseClient.removeComment(CONTAINER_ID, caseId, updatedCommentId), 403, "");

        // add back yoda to owner role
        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_OWNER_ROLE, USER_YODA);
        
        caseClient.removeComment(CONTAINER_ID, caseId, updatedCommentId);
        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    public void testProcessDefinitionsPagination() {
        PaginatingRecordsProvider<ProcessDefinition> paginatingProcessDefinitionsProvider =
                (page, pageSize)-> caseClient.findProcesses(page, pageSize);
        assertPagination(paginatingProcessDefinitionsProvider, 2);
    }

    @Test
    public void testActiveNodesOnNonExistingContainer() {
        assertSearchOperationOnNonExistingContainer(
                (containerId) -> caseClient.getActiveNodes(containerId, "someCaseId", 0, 10)
        );
    }

    @Test
    public void testCompletedNodesOnNonExistingContainer() {
        assertSearchOperationOnNonExistingContainer(
                (containerId) -> caseClient.getCompletedNodes(containerId, "someCaseId", 0, 10)
        );
    }

    @Test
    public void testActiveNodesOnNonExistingCaseId() {
        assertSearchOperationOnNonExistingCaseId(
                (caseId) -> caseClient.getActiveNodes(CONTAINER_ID, caseId, 0, 10)
        );
    }

    @Test
    public void testCompletedNodesOnNonExistingCaseId() {
        assertSearchOperationOnNonExistingCaseId(
                (caseId) -> caseClient.getCompletedNodes(CONTAINER_ID, caseId, 0, 10)
        );
    }

    @Test
    public void testGetCaseInstancesByDataPagination() {
        final String dataEntryName = "claimReportDone";
        final boolean dataEntryValue = Boolean.TRUE;
        final int numberOfItems = 3;

        for (int i = 0; i < numberOfItems; i++) {
            String caseClaimId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
            caseClient.putCaseInstanceData(CONTAINER_ID, caseClaimId, dataEntryName, dataEntryValue);
        }

        final List<String> statusFilter = Arrays.asList(CaseStatus.OPEN.getName());
        assertPagination(
                (page, pageSize) -> caseClient.getCaseInstancesByData(
                        dataEntryName,
                        String.valueOf(dataEntryValue),
                        statusFilter,
                        page,
                        pageSize
                ),
                2
        );
    }

    @Test
    public void testProcessDefinitionsSorting() {
        final List<ProcessDefinition> processesSortByNameDesc =
                caseClient.findProcesses(0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, false);
        final String [] expectedProcessNamesDesc = {"User Task", "Hiring a Developer", "DataVerification"};
        Assertions.assertThat(processesSortByNameDesc)
                .extracting(ProcessDefinition::getName)
                .containsExactly(expectedProcessNamesDesc);

        final List<ProcessDefinition> processesSortByNameAsc =
                caseClient.findProcesses(0, 10, CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, true);
        final String [] expectedProcessNamesAsc = {"DataVerification", "Hiring a Developer", "User Task"};
        Assertions.assertThat(processesSortByNameAsc)
                .extracting(ProcessDefinition::getName)
                .containsExactly(expectedProcessNamesAsc);

        //order should be preserved also with pagination
        final List<ProcessDefinition> processesSortByNameDescFirstPage =
                caseClient.findProcesses(0, 1,  CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, false);
        Assertions.assertThat(processesSortByNameDescFirstPage)
                .extracting(ProcessDefinition::getName)
                .containsExactly("User Task");

        final List<ProcessDefinition> processesSortByNameAscFirstPage =
                caseClient.findProcesses(0, 1,  CaseServicesClient.SORT_BY_CASE_DEFINITION_NAME, true);
        Assertions.assertThat(processesSortByNameAscFirstPage)
                .extracting(ProcessDefinition::getName)
                .containsExactly("DataVerification");
    }

    @Test
    public void testProcessDefinitionsByContainerPagination() {
        PaginatingRecordsProvider<ProcessDefinition> paginatingProcessDefinitionsByContainerProvider =
                (page, pageSize)-> caseClient.findProcesses(page, pageSize);
        assertPagination(paginatingProcessDefinitionsByContainerProvider, 2);
    }

    @Test
    public void testActiveProcessInstancesPagination() {
        final String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, DATA_VERIFICATION_DEF_ID, Collections.emptyMap());
        caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, USER_TASK_DEF_ID, Collections.emptyMap());

        PaginatingRecordsProvider<ProcessInstance> paginatingProcessInstancesProvider =
                (page, pageSize)-> caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, page, pageSize);
        assertPagination(paginatingProcessInstancesProvider, 2);
    }

    @Test
    public void testGetCaseInstanceDataItemsOnNonExistingCaseId() {
        assertSearchOperationOnNonExistingCaseId(
                (caseId) -> caseClient.getCaseInstanceDataItems(caseId, 0, 10)
        );
    }

    @Test
    public void testGetCaseInstanceDataItemsPagination() {
        final String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "first", "one");
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "second", "two");
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "third", "three");
        assertPagination((page, pageSize) -> caseClient.getCaseInstanceDataItems(caseId, page, pageSize), 2);
    }


    @Test
    public void testFindCaseTasksAssignedAsBusinessAdministratorOnNonExistingCaseId() {
        assertSearchOperationOnNonExistingCaseId(
                (caseId) -> caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_YODA, 0, 10)
        );
    }

    @Test
    public void testFindCaseTasksAssignedAsBusinessAdminPagination() throws Exception {
        final String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskOne", "desc", USER_YODA, "", Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskTwo", "desc", USER_YODA, "", Collections.emptyMap());
        caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "TaskThree", "desc", USER_YODA, "", Collections.emptyMap());
        changeUser(USER_ADMINISTRATOR);

        assertPagination(
                (page, pageSize) -> caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, page,  pageSize),
                2
        );
    }

    @Test
    public void testFindCaseTasksAssignedAsStakeholderOnNonExistingCaseId() {
        assertSearchOperationOnNonExistingCaseId(
                (caseId) -> caseClient.findCaseTasksAssignedAsStakeholder(caseId, USER_YODA, 0, 10)
        );
    }

    @Test
    public void testFindCaseTasksAssignedAsStakeholderPagination() {
        final String caseId = startUserTaskCase(USER_JOHN, USER_YODA);
        final int tasksToAdd = 3;
        for (int i = 0; i < tasksToAdd; i++) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input", "text data");
            parameters.put(TASK_STAKEHOLDER_ID, USER_YODA);
            caseClient.addDynamicUserTask(CONTAINER_ID, caseId, "Task" + (i + 1), "desc", USER_JOHN, "", parameters);
        }

        assertPagination((page, pageSize) -> caseClient.findCaseTasksAssignedAsStakeholder(caseId, USER_YODA, page, pageSize), 2);
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
        taskInput.put(GROUP_ID, CASE_HR_GROUP);
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

        changeUser(caseOwner);
        taskInput.put(BUSINESS_ADMINISTRATOR_ID, "contact");
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);

        changeUser(lookupUser);
        tasks = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(tasks);
        assertEquals(2, tasks.size()); // 1 for Yoda and 1 for John
        assertTaskByActualOwner(tasks, USER_YODA, "Hello1", Reserved, "Simple description");
        assertTaskByActualOwner(tasks, USER_JOHN, "Hello2", Reserved, "User's comment");

        changeUser(caseOwner);
        taskInput.put(BUSINESS_ADMINISTRATOR_GROUP_ID, CASE_PARTICIPANT_ROLE);
        taskInput.put(COMMENT, "User's comment");
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Hello2", taskInput);

        changeUser(lookupUser);
        tasks = caseClient.findCaseTasksAssignedAsBusinessAdministrator(caseId, USER_ADMINISTRATOR, 0, 10);
        assertNotNull(tasks);
        assertEquals(3, tasks.size()); // 1 for Yoda and 2 ad-hoc tasks for John
        assertEquals(2, tasks.stream()
                .filter(taskSummary -> USER_JOHN.equals(taskSummary.getActualOwner()))
                .count());
        assertTaskByActualOwner(tasks, USER_YODA, "Hello1", Reserved, "Simple description");
        assertTaskByActualOwner(tasks, USER_JOHN, "Hello2", Reserved, "User's comment");
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

    private <T> void assertSearchOperationOnNonExistingContainer(Function<String, Collection<T>> operation) {
        final String nonExistingContainerId = "NON_EXISTING_CONTAINER_ID";
        Assertions.assertThat(operation.apply(nonExistingContainerId)).isEmpty();
    }

    private <T> void assertSearchOperationOnNonExistingCaseId(Function<String, Collection<T>> operation) {
        final String nonExistingCaseId = "NON_EXISTING_CASE_ID";
        Assertions.assertThat(operation.apply(nonExistingCaseId)).isEmpty();
    }

    private <T> void assertPagination(final PaginatingRecordsProvider<T> paginatingProcessProvider, final int pageSize) {
        final List<T> all = paginatingProcessProvider.apply(0, Integer.MAX_VALUE);
        final int mod = all.size() % pageSize;
        final int lastPageIndex = mod == 0? (all.size() / pageSize) - 1 : all.size() / pageSize;
        final int firstPageExpectedCount = Math.min(all.size(), pageSize);
        final int lastPageExpectedCount = mod == 0 ? pageSize : mod;

        final List<T> firstPage = paginatingProcessProvider.apply(0, pageSize);
        final List<T> lastPage = paginatingProcessProvider.apply(lastPageIndex, pageSize);
        final List<T> nonExistingPage = paginatingProcessProvider.apply(lastPageIndex + 1, pageSize);

        Assertions.assertThat(nonExistingPage).isEmpty();
        Assertions.assertThat(firstPage).hasSize(firstPageExpectedCount).doesNotContainAnyElementsOf(lastPage);
        Assertions.assertThat(lastPage).hasSize(lastPageExpectedCount).doesNotContainAnyElementsOf(firstPage);

        // what if we use negative page number or page size?
        assertClientException(() -> paginatingProcessProvider.apply(-1, pageSize), 400, "-1");
        assertClientException(() -> paginatingProcessProvider.apply(0, -1), 400, "-1");
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

    private String startCarInsuranceClaimCase(String insured, String insuranceRep, String assessor) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, insured)
                .addUserAssignments(CASE_INS_REP_ROLE, insuranceRep)
                .addUserAssignments(CASE_ASSESSOR_ROLE, assessor)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }

    private void assertHrCaseInstance(CaseInstance caseInstance, String caseId, String owner) {
        assertNotNull(caseInstance);
        assertEquals(caseId, caseInstance.getCaseId());
        assertEquals(CASE_HR_DEF_ID, caseInstance.getCaseDefinitionId());
        assertEquals(CASE_HR_DESRIPTION, caseInstance.getCaseDescription());
        assertEquals(owner, caseInstance.getCaseOwner());
        assertEquals(CaseStatus.OPEN.getId(), caseInstance.getCaseStatus().intValue());
        assertNotNull(caseInstance.getStartedAt());
        assertNull(caseInstance.getCompletedAt());
        assertEquals("", caseInstance.getCompletionMessage());
        assertEquals(CONTAINER_ID, caseInstance.getContainerId());
    }

    private void assertCarInsuranceCaseInstance(CaseInstance caseInstance, String caseId, String owner) {
        assertNotNull(caseInstance);
        assertEquals(caseId, caseInstance.getCaseId());
        assertEquals(CLAIM_CASE_DEF_ID, caseInstance.getCaseDefinitionId());
        assertEquals(CLAIM_CASE_DESRIPTION, caseInstance.getCaseDescription());
        assertEquals(owner, caseInstance.getCaseOwner());
        assertEquals(CaseStatus.OPEN.getId(), caseInstance.getCaseStatus().intValue());
        assertNotNull(caseInstance.getStartedAt());
        assertNull(caseInstance.getCompletedAt());
        assertEquals("", caseInstance.getCompletionMessage());
        assertEquals(CONTAINER_ID, caseInstance.getContainerId());
    }

    private void assertHrCaseDefinition(CaseDefinition caseDefinition) {
        assertNotNull(caseDefinition);
        assertEquals(CASE_HR_DEF_ID, caseDefinition.getIdentifier());
        assertEquals(CASE_HR_NAME, caseDefinition.getName());
        assertEquals(CASE_HR_ID_PREFIX, caseDefinition.getCaseIdPrefix());
        assertEquals(CASE_HR_VERSION, caseDefinition.getVersion());
        assertEquals(3, caseDefinition.getAdHocFragments().size());
        KieServerAssert.assertNullOrEmpty("Stages should be empty", caseDefinition.getCaseStages());
        assertEquals(CONTAINER_ID, caseDefinition.getContainerId());

        // Milestones checks
        assertEquals(2, caseDefinition.getMilestones().size());
        assertEquals("Milestone1", caseDefinition.getMilestones().get(0).getName());
        assertEquals("_SomeID4", caseDefinition.getMilestones().get(0).getIdentifier());
        assertFalse("Case shouldn't be mandatory.", caseDefinition.getMilestones().get(0).isMandatory());
        assertEquals("Milestone2", caseDefinition.getMilestones().get(1).getName());
        assertEquals("_5", caseDefinition.getMilestones().get(1).getIdentifier());
        assertFalse("Case shouldn't be mandatory.", caseDefinition.getMilestones().get(1).isMandatory());

        // Roles check
        assertEquals(3, caseDefinition.getRoles().size());
        assertTrue("Role 'owner' is missing.", caseDefinition.getRoles().containsKey("owner"));
        assertTrue("Role 'contact' is missing.", caseDefinition.getRoles().containsKey("contact"));
        assertTrue("Role 'participant' is missing.", caseDefinition.getRoles().containsKey("participant"));
    }

    private void assertCarInsuranceCaseDefinition(CaseDefinition caseDefinition) {
        assertNotNull(caseDefinition);
        assertEquals(CLAIM_CASE_DEF_ID, caseDefinition.getIdentifier());
        assertEquals(CLAIM_CASE_NAME, caseDefinition.getName());
        assertEquals(CLAIM_CASE_ID_PREFIX, caseDefinition.getCaseIdPrefix());
        assertEquals(CLAIM_CASE_VERSION, caseDefinition.getVersion());
        assertEquals(1, caseDefinition.getAdHocFragments().size());
        KieServerAssert.assertNullOrEmpty("Milestones should be empty.", caseDefinition.getMilestones());
        assertEquals(CONTAINER_ID, caseDefinition.getContainerId());

        // Stages check
        assertEquals(3, caseDefinition.getCaseStages().size());
        assertEquals("Build claim report", caseDefinition.getCaseStages().get(0).getName());
        assertNotNull(caseDefinition.getCaseStages().get(0).getIdentifier());
        assertEquals("Claim assesment", caseDefinition.getCaseStages().get(1).getName());
        assertNotNull(caseDefinition.getCaseStages().get(1).getIdentifier());
        assertEquals("Escalate rejected claim", caseDefinition.getCaseStages().get(2).getName());
        assertNotNull(caseDefinition.getCaseStages().get(2).getIdentifier());

        List<CaseAdHocFragment> buildClaimFragments = caseDefinition.getCaseStages().get(0).getAdHocFragments();
        assertEquals(2, buildClaimFragments.size());
        assertEquals("Provide accident information", buildClaimFragments.get(0).getName());
        assertEquals("HumanTaskNode", buildClaimFragments.get(0).getType());
        assertEquals("Submit police report", buildClaimFragments.get(1).getName());
        assertEquals("HumanTaskNode", buildClaimFragments.get(1).getType());

        List<CaseAdHocFragment> claimAssesmentFragments = caseDefinition.getCaseStages().get(1).getAdHocFragments();
        assertEquals(2, claimAssesmentFragments.size());
        assertEquals("Classify claim", claimAssesmentFragments.get(0).getName());
        assertEquals("RuleSetNode", claimAssesmentFragments.get(0).getType());
        assertEquals("Calculate claim", claimAssesmentFragments.get(1).getName());
        assertEquals("WorkItemNode", claimAssesmentFragments.get(1).getType());

        List<CaseAdHocFragment> escalateRejectedClaimFragments = caseDefinition.getCaseStages().get(2).getAdHocFragments();
        assertEquals(1, escalateRejectedClaimFragments.size());
        assertEquals("Negotiation meeting", escalateRejectedClaimFragments.get(0).getName());
        assertEquals("HumanTaskNode", escalateRejectedClaimFragments.get(0).getType());

        // Roles check
        assertEquals(3, caseDefinition.getRoles().size());
        assertTrue("Role 'insured' is missing.", caseDefinition.getRoles().containsKey("insured"));
        assertTrue("Role 'insuranceRepresentative' is missing.", caseDefinition.getRoles().containsKey("insuranceRepresentative"));
        assertTrue("Role 'assessor' is missing.", caseDefinition.getRoles().containsKey("assessor"));
    }

    private void assertHrProcessInstance(ProcessInstance processInstance, String caseId) {
        assertHrProcessInstance(processInstance, caseId, STATE_ACTIVE);
    }

    private void assertHrProcessInstance(ProcessInstance processInstance, String caseId, long processInstanceState) {
        assertNotNull(processInstance);
        assertNotNull(processInstance.getId());
        assertEquals(caseId, processInstance.getCorrelationKey());
        assertEquals(processInstanceState, processInstance.getState().intValue());
        assertEquals(CASE_HR_DEF_ID, processInstance.getProcessId());
        assertEquals(CASE_HR_NAME, processInstance.getProcessName());
        assertEquals(CASE_HR_VERSION, processInstance.getProcessVersion());
        assertEquals(CONTAINER_ID, processInstance.getContainerId());
        assertEquals(CASE_HR_DESRIPTION, processInstance.getProcessInstanceDescription());
        assertEquals(USER_YODA, processInstance.getInitiator());
        assertEquals(-1L, processInstance.getParentId().longValue());
        assertNotNull(processInstance.getCorrelationKey());
        assertNotNull(processInstance.getDate());
    }

    private void assertCarInsuranceProcessInstance(ProcessInstance processInstance, String caseId) {
        assertNotNull(processInstance);
        assertNotNull(processInstance.getId());
        assertEquals(caseId, processInstance.getCorrelationKey());
        assertEquals(STATE_ACTIVE, processInstance.getState().intValue());
        assertEquals(CLAIM_CASE_DEF_ID, processInstance.getProcessId());
        assertEquals(CLAIM_CASE_NAME, processInstance.getProcessName());
        assertEquals(CLAIM_CASE_VERSION, processInstance.getProcessVersion());
        assertEquals(CONTAINER_ID, processInstance.getContainerId());
        assertEquals(CLAIM_CASE_DESRIPTION, processInstance.getProcessInstanceDescription());
        assertEquals(USER_YODA, processInstance.getInitiator());
        assertEquals(-1L, processInstance.getParentId().longValue());
        assertNotNull(processInstance.getCorrelationKey());
        assertNotNull(processInstance.getDate());
    }

    private void assertBuildClaimReportCaseStage(CaseStage stage, String status) {
        assertEquals("Build claim report", stage.getName());
        assertNotNull(stage.getIdentifier());
        assertEquals(status, stage.getStatus());

        if (status.endsWith("Active")) {
            List<NodeInstance> activeNodes = stage.getActiveNodes();
            assertEquals(1, activeNodes.size());
            assertEquals("Provide accident information", activeNodes.get(0).getName());
            assertEquals("HumanTaskNode", activeNodes.get(0).getNodeType());
        } else {
            KieServerAssert.assertNullOrEmpty("Active nodes should be null or empty.", stage.getActiveNodes());
        }

        List<CaseAdHocFragment> adHocFragments = stage.getAdHocFragments();
        assertEquals(2, adHocFragments.size());
        assertEquals("Provide accident information", adHocFragments.get(0).getName());
        assertEquals("HumanTaskNode", adHocFragments.get(0).getType());
        assertEquals("Submit police report", adHocFragments.get(1).getName());
        assertEquals("HumanTaskNode", adHocFragments.get(1).getType());
    }

    private void assertClaimAssesmentCaseStage(CaseStage stage, String status) {
        assertEquals("Claim assesment", stage.getName());
        assertNotNull(stage.getIdentifier());
        assertEquals(status, stage.getStatus());

        if (status.endsWith("Active")) {
            List<NodeInstance> activeNodes = stage.getActiveNodes();
            assertEquals(1, activeNodes.size());
            assertEquals("Assessor evaluation", activeNodes.get(0).getName());
            assertEquals("HumanTaskNode", activeNodes.get(0).getNodeType());
        } else {
            KieServerAssert.assertNullOrEmpty("Active nodes should be null or empty.", stage.getActiveNodes());
        }

        List<CaseAdHocFragment> adHocFragments = stage.getAdHocFragments();
        assertEquals(2, adHocFragments.size());
        assertEquals("Classify claim", adHocFragments.get(0).getName());
        assertEquals("RuleSetNode", adHocFragments.get(0).getType());
        assertEquals("Calculate claim", adHocFragments.get(1).getName());
        assertEquals("WorkItemNode", adHocFragments.get(1).getType());
    }

    private void assertEscalateRejectedClaimCaseStage(CaseStage stage, String status) {
        assertEquals("Escalate rejected claim", stage.getName());
        assertNotNull(stage.getIdentifier());
        assertEquals(status, stage.getStatus());

        KieServerAssert.assertNullOrEmpty("Active nodes should be null or empty.", stage.getActiveNodes());

        List<CaseAdHocFragment> adHocFragments = stage.getAdHocFragments();
        assertEquals(1, adHocFragments.size());
        assertEquals("Negotiation meeting", adHocFragments.get(0).getName());
        assertEquals("HumanTaskNode", adHocFragments.get(0).getType());
    }

    private void assertComment(CaseComment comment, String author, String text) {
        assertNotNull(comment);
        assertEquals(comment.getAuthor(), author);
        assertEquals(comment.getText(), text);
    }

    @FunctionalInterface
    private interface PaginatingRecordsProvider<T> extends BiFunction<Integer, Integer, List<T>> {
        //just for type safety
    }
}

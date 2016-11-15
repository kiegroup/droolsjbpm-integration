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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseAdHocFragment;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

public class CaseRuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String CONTAINER_ID2 = "insurance-second";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";

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

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(CLAIM_REPORT_CLASS_NAME, Class.forName(CLAIM_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(PROPERTY_DAMAGE_REPORT_CLASS_NAME, Class.forName(PROPERTY_DAMAGE_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
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
        assertEquals("2", milestone.getIdentifier());
        assertEquals("Milestone1", milestone.getName());
        assertEquals("Completed", milestone.getStatus());
        assertNotNull(milestone.getAchievedAt());
        assertTrue(milestone.isAchieved());

        milestone = caseInstance.getMilestones().get(1);
        assertNotNull(milestone.getIdentifier());
        assertEquals("Milestone2", milestone.getName());
        assertEquals("Available", milestone.getStatus());
        assertNull(milestone.getAchievedAt());
        assertFalse(milestone.isAchieved());
    }

    @Test
    public void testGetCaseInstanceCarInsuranceClaimCaseWithData() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId, true, true, true, true);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        KieServerAssert.assertNullOrEmpty("Milestones should be empty.", caseInstance.getMilestones());

        // Assert case file
        assertNotNull(caseInstance.getCaseFile());
        assertEquals("first case started", caseInstance.getCaseFile().getData().get("s"));

        // Assert role assignments
        assertNotNull(caseInstance.getRoleAssignments());
        assertEquals(3, caseInstance.getRoleAssignments().size());

        CaseRoleAssignment insuredRole = caseInstance.getRoleAssignments().get(0);
        assertEquals("insured", insuredRole.getName());
        assertEquals(1, insuredRole.getUsers().size());
        assertEquals(USER_YODA, insuredRole.getUsers().get(0));
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", insuredRole.getGroups());

        CaseRoleAssignment assessorRole = caseInstance.getRoleAssignments().get(1);
        assertEquals("assessor", assessorRole.getName());
        KieServerAssert.assertNullOrEmpty("Users should be empty.", assessorRole.getUsers());
        KieServerAssert.assertNullOrEmpty("Groups should be empty.", assessorRole.getGroups());

        CaseRoleAssignment insuranceRepresentativeRole = caseInstance.getRoleAssignments().get(2);
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
        KieServerAssert.assertNullOrEmpty("Active nodes should be empty.", stage.getActiveNodes());
        assertEquals(2, stage.getAdHocFragments().size());
    }

    @Test
    public void testGetCaseInstances() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
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
        String claimCaseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
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
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ABORTED), 0, 1000);
        assertNotNull(caseInstances);
        int abortedCaseInstanceCount = caseInstances.size();

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ABORTED), 0, 1000);
        assertNotNull(caseInstances);
        assertEquals(abortedCaseInstanceCount + 1, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByStatusSorting() {
        String hrCaseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String claimCaseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
        assertNotEquals(hrCaseId, claimCaseId);

        List<CaseInstance> caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(claimCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertEquals(2, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());
        assertEquals(claimCaseId, caseInstances.get(1).getCaseId());
    }

    @Test
    public void testGetCaseInstancesByNotExistingContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer("not-existing-container", Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);
    }

    @Test
    public void testGetCaseInstancesByContainerSorting() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertNotNull(caseInstances);
        assertEquals(2, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);
        assertCarInsuranceCaseInstance(caseInstances.get(1), caseId2, USER_YODA);
    }

    @Test
    public void testGetCaseInstancesByDefinitionNotExistingContainer() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition("not-existing-container", CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByNotExistingDefinition() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, "not-existing-case", Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByDefinition() {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByDefinitionSorting() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        String caseId2 = startUserTaskCase(USER_YODA, USER_JOHN);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId2, USER_YODA);

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertNotNull(caseInstances);
        assertEquals(2, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), caseId2, USER_YODA);
        assertHrCaseInstance(caseInstances.get(1), caseId, USER_YODA);
    }

    @Test
    public void testGetCaseInstancesOwnedByNotExistingUser() throws Exception {
        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy("not-existing-user", Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesOwnedBy() throws Exception {
        // Test is using user authentication, isn't available for local execution(which has mocked authentication info).
        Assume.assumeFalse(TestConfig.isLocalServer());

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(caseInstances);
        assertEquals(0, caseInstances.size());

        try {
            changeUser(USER_YODA);
            String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
            changeUser(USER_JOHN);
            String caseId2 = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

            changeUser(USER_YODA);
            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1);
            assertNotNull(caseInstances);
            assertEquals(1, caseInstances.size());
            assertHrCaseInstance(caseInstances.get(0), caseId, USER_YODA);

            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1);
            assertNotNull(caseInstances);
            assertEquals(0, caseInstances.size());

            changeUser(USER_JOHN);
            caseInstances = caseClient.getCaseInstancesOwnedBy(USER_JOHN, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
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
        String claimCaseId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertCarInsuranceCaseInstance(caseInstances.get(0), claimCaseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertNotNull(caseInstances);
        assertEquals(1, caseInstances.size());
        assertHrCaseInstance(caseInstances.get(0), hrCaseId, USER_YODA);

        caseInstances = caseClient.getCaseInstancesOwnedBy(USER_YODA, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
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
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertHrProcessInstance(processInstances.get(0), userTaskCase);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertCarInsuranceProcessInstance(processInstances.get(0), carInsuranceClaimCase);

        caseClient.cancelCaseInstance(CONTAINER_ID, userTaskCase);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, userTaskCase, Arrays.asList(ProcessInstance.STATE_ABORTED), 0, 10);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertHrProcessInstance(processInstances.get(0), userTaskCase, ProcessInstance.STATE_ABORTED);
    }

    @Test
    public void testGetProcessInstancesNotExistingCase() {
        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, "not-existing-case", Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetProcessInstancesNotExistingContainer() {
        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getProcessInstances("not-existing-container", CASE_HR_DEF_ID, Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetProcessInstancesSorting() {
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);
        caseClient.addDynamicSubProcess(CONTAINER_ID, carInsuranceClaimCase, DATA_VERIFICATION_DEF_ID, new HashMap<>());

        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(ProcessInstance.STATE_ACTIVE, ProcessInstance.STATE_COMPLETED), 0, 1, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), CLAIM_CASE_DEF_ID);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(ProcessInstance.STATE_ACTIVE, ProcessInstance.STATE_COMPLETED), 1, 1, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, true);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), DATA_VERIFICATION_DEF_ID);

        processInstances = caseClient.getProcessInstances(CONTAINER_ID, carInsuranceClaimCase, Arrays.asList(ProcessInstance.STATE_ACTIVE, ProcessInstance.STATE_COMPLETED), 0, 10, CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, false);
        assertNotNull(processInstances);
        assertEquals(2, processInstances.size());
        assertEquals(processInstances.get(0).getProcessId(), DATA_VERIFICATION_DEF_ID);
        assertEquals(processInstances.get(1).getProcessId(), CLAIM_CASE_DEF_ID);
    }

    @Test
    public void testGetActiveProcessInstances() {
        String userTaskCase = startUserTaskCase(USER_YODA, USER_JOHN);
        String carInsuranceClaimCase = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, userTaskCase, 0, 1);
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
        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getActiveProcessInstances(CONTAINER_ID, "not-existing-case", 0, 10);
        assertNotNull(processInstances);
        assertEquals(0, processInstances.size());
    }

    @Test
    public void testGetActiveProcessInstancesNotExistingContainer() {

        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = caseClient.getActiveProcessInstances("not-existing-container", CASE_HR_DEF_ID, 0, 10);
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

        List<org.kie.server.api.model.instance.ProcessInstance> instances = caseClient.getActiveProcessInstances(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        org.kie.server.api.model.instance.ProcessInstance pi = instances.get(0);
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

        caseClient.addComment(CONTAINER_ID, caseId, USER_YODA, "first comment");

        comments = caseClient.getComments(CONTAINER_ID, caseId, 0, 10);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        CaseComment comment = comments.get(0);
        assertNotNull(comment);
        assertEquals(USER_YODA, comment.getAuthor());
        assertEquals("first comment", comment.getText());
        assertNotNull(comment.getAddedAt());
        assertNotNull(comment.getId());

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

        String caseClaimId = startCarInsuranceClaimCase(USER_JOHN, USER_YODA);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CASE_HR_ID_PREFIX));

        assertNotNull(caseClaimId);
        assertTrue(caseClaimId.startsWith(CLAIM_CASE_ID_PREFIX));

        List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(1), 0, 10);
        assertEquals(2, caseInstances.size());

        List<String> caseDefs = caseInstances.stream().map(c -> c.getCaseDefinitionId()).collect(toList());
        assertTrue(caseDefs.contains(CASE_HR_DEF_ID));
        assertTrue(caseDefs.contains(CLAIM_CASE_DEF_ID));

        caseInstances = caseClient.getCaseInstancesByDefinition(CONTAINER_ID, CLAIM_CASE_DEF_ID, Arrays.asList(1), 0, 10);
        assertEquals(1, caseInstances.size());

        List<CaseStage> stages = caseClient.getStages(CONTAINER_ID, caseClaimId, true, 0, 10);
        assertEquals(1, stages.size());
        CaseStage caseStage = stages.get(0);
        assertEquals("Build claim report", caseStage.getName());
        assertEquals(2, caseStage.getAdHocFragments().size());

        List<CaseRoleAssignment> roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(3, roles.size());

        Map<String, CaseRoleAssignment> mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));
        assertTrue(mappedRoles.containsKey(CASE_INSURED_ROLE));
        assertTrue(mappedRoles.containsKey(CASE_INS_REP_ROLE));
        assertTrue(mappedRoles.containsKey(CASE_ASSESSOR_ROLE));

        CaseRoleAssignment insuredRole = mappedRoles.get(CASE_INSURED_ROLE);
        assertTrue(insuredRole.getUsers().contains(USER_JOHN));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", insuredRole.getGroups());

        CaseRoleAssignment insRepRole = mappedRoles.get(CASE_INS_REP_ROLE);
        assertTrue(insRepRole.getUsers().contains(USER_YODA));
        KieServerAssert.assertNullOrEmpty("Groups should be empty", insRepRole.getGroups());

        CaseRoleAssignment assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        KieServerAssert.assertNullOrEmpty("Users should be empty", assessorRole.getUsers());
        KieServerAssert.assertNullOrEmpty("Groups should be empty", assessorRole.getGroups());

        caseClient.assignUserToRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, USER_MARY);
        caseClient.assignGroupToRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, "managers");

        roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(3, roles.size());
        mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));

        assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        assertTrue(assessorRole.getUsers().contains(USER_MARY));
        assertTrue(assessorRole.getGroups().contains("managers"));

        caseClient.removeUserFromRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, USER_MARY);
        caseClient.removeGroupFromRole(CONTAINER_ID, caseClaimId, CASE_ASSESSOR_ROLE, "managers");

        roles = caseClient.getRoleAssignments(CONTAINER_ID, caseClaimId);
        assertEquals(3, roles.size());
        mappedRoles = roles.stream().collect(toMap(CaseRoleAssignment::getName, r -> r));

        assessorRole = mappedRoles.get(CASE_ASSESSOR_ROLE);
        KieServerAssert.assertNullOrEmpty("Users should be empty", assessorRole.getUsers());
        KieServerAssert.assertNullOrEmpty("Groups should be empty", assessorRole.getGroups());
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

    private String startCarInsuranceClaimCase(String insured, String insuranceRep) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, insured)
                .addUserAssignments(CASE_INS_REP_ROLE, insuranceRep)
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
        assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
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
        assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
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

    private void assertHrProcessInstance(org.kie.server.api.model.instance.ProcessInstance processInstance, String caseId) {
        assertHrProcessInstance(processInstance, caseId, ProcessInstance.STATE_ACTIVE);
    }

    private void assertHrProcessInstance(org.kie.server.api.model.instance.ProcessInstance processInstance, String caseId, long processInstanceState) {
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

    private void assertCarInsuranceProcessInstance(org.kie.server.api.model.instance.ProcessInstance processInstance, String caseId) {
        assertNotNull(processInstance);
        assertNotNull(processInstance.getId());
        assertEquals(caseId, processInstance.getCorrelationKey());
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());
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
}

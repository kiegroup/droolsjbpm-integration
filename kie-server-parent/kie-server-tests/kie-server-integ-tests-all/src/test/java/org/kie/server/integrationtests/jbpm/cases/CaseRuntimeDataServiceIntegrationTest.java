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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

public class CaseRuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CASE_DEF_ID = "UserTaskCase";
    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";

    private static final String CASE_ID_PREFIX = "HR";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";
    private static final String CASE_ASSESSOR_ROLE = "assessor";

    private static final String CLAIM_CASE_ID_PREFIX = "CAR_INS";

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
    public void testListCaseDefinitions() {
        List<CaseDefinition> definitions = caseClient.getCaseDefinitionsByContainer(CONTAINER_ID, 0, 10);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());

        Map<String, CaseDefinition> mappedDefinitions = definitions.stream().collect(Collectors.toMap(CaseDefinition::getIdentifier, d -> d));
        assertTrue(mappedDefinitions.containsKey(CASE_DEF_ID));
        assertTrue(mappedDefinitions.containsKey(CLAIM_CASE_DEF_ID));

        CaseDefinition hrCase = caseClient.getCaseDefinition(CONTAINER_ID, CASE_DEF_ID);
        assertNotNull(hrCase);
        assertEquals(CASE_DEF_ID, hrCase.getIdentifier());
        assertEquals("Simple Case with User Tasks", hrCase.getName());
        assertEquals("HR", hrCase.getCaseIdPrefix());
        assertEquals("1.0", hrCase.getVersion());
        assertEquals(3, hrCase.getAdHocFragments().size());
        KieServerAssert.assertNullOrEmpty("Stages should be empty", hrCase.getCaseStages());
        assertEquals(2, hrCase.getMilestones().size());
        assertEquals(3, hrCase.getRoles().size());

        definitions = caseClient.getCaseDefinitions("User*", 0, 10);
        assertNotNull(definitions);
        assertEquals(1, definitions.size());

        definitions = caseClient.getCaseDefinitions(0, 10);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
    }

    @Test
    public void testCreateCaseWithCaseFileAndTriggerMilestones() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .addUserAssignments(CASE_CONTACT_ROLE, USER_JOHN)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertHrCaseInstance(caseInstance, caseId);

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

            assertHrCaseInstance(caseInstances.get(0), caseId);

            // now auto complete milestone by inserting data
            caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "dataComplete", true);
            milestones = caseClient.getMilestones(CONTAINER_ID, caseId, true, 0, 10);
            assertNotNull(milestones);
            assertEquals(2, milestones.size());
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithCaseFileAndDynamicActivities() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_JOHN)
                .addUserAssignments(CASE_CONTACT_ROLE, USER_YODA)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

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
            assertEquals(CASE_DEF_ID, pi.getProcessId());
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
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithCaseFileWithComments() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_JOHN)
                .addUserAssignments(CASE_CONTACT_ROLE, USER_YODA)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

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

        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateDifferentTypesCases() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_JOHN)
                .addUserAssignments(CASE_CONTACT_ROLE, USER_YODA)
                .data(data)
                .build();

        CaseFile caseClaimFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, USER_JOHN)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_YODA)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);

        String caseClaimId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseClaimFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            assertNotNull(caseClaimId);
            assertTrue(caseClaimId.startsWith(CLAIM_CASE_ID_PREFIX));

            List<CaseInstance> caseInstances = caseClient.getCaseInstancesByContainer(CONTAINER_ID, Arrays.asList(1), 0, 10);
            assertEquals(2, caseInstances.size());

            List<String> caseDefs = caseInstances.stream().map(c -> c.getCaseDefinitionId()).collect(toList());
            assertTrue(caseDefs.contains(CASE_DEF_ID));
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

        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }

            if (caseClaimId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseClaimId);
            }
        }
    }

    protected void assertHrCaseInstance(CaseInstance caseInstance, String caseId) {
        caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertNotNull(caseInstance);
        assertEquals(caseId, caseInstance.getCaseId());
        assertEquals(CASE_DEF_ID, caseInstance.getCaseDefinitionId());
        assertEquals("Case first case started", caseInstance.getCaseDescription());
        assertEquals(USER_YODA, caseInstance.getCaseOwner());
        assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
        assertNotNull(caseInstance.getStartedAt());
        assertNull(caseInstance.getCompletedAt());
        assertEquals("", caseInstance.getCompletionMessage());
    }

}

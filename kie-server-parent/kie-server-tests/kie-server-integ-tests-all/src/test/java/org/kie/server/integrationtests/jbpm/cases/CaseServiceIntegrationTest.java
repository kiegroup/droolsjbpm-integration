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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.*;

public class CaseServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CLAIM_CASE_ID_PREFIX = "CAR_INS";
    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    private static final String CLAIM_CASE_DESRIPTION = "CarInsuranceClaimCase";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";

    private static final String CONTAINER_ALIAS = "ins";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(CLAIM_REPORT_CLASS_NAME, Class.forName(CLAIM_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(PROPERTY_DAMAGE_REPORT_CLASS_NAME, Class.forName(PROPERTY_DAMAGE_REPORT_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testCreateCaseWithEmptyCaseFile() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileButWithRoleAssignments() {
        CaseFile caseFile = CaseFile.builder().addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                                              .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                                              .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        TaskSummary task = tasks.get(0);
        assertNotNull(task);
        assertEquals("Provide accident information", task.getName());
        assertEquals(null, task.getActualOwner());
        assertEquals("Ready", task.getStatus());
    }

    @Test
    public void testCreateCaseWithCaseFile() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("car", "ford");
        CaseFile caseFile = CaseFile.builder().addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .data(caseData)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        assertNotNull(caseData);
        assertEquals(1, caseData.size());
        assertEquals("ford", caseData.get("car"));

        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "car", "fiat");

        Object carCaseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "car");
        assertNotNull(carCaseData);
        assertTrue(carCaseData instanceof String);
        assertEquals("fiat", carCaseData);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        TaskSummary task = tasks.get(0);
        assertNotNull(task);
        assertEquals("Provide accident information", task.getName());
        assertEquals(null, task.getActualOwner());
        assertEquals("Ready", task.getStatus());

        Map<String, Object> output = new HashMap<>();
        Object claimReport = createInstance(CLAIM_REPORT_CLASS_NAME);
        setValue(claimReport, "name", "John Doe");

        output.put("claimReport_", claimReport);
        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, output);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        assertNotNull(caseData);
        assertEquals(2, caseData.size());
        assertEquals("fiat", caseData.get("car"));

        Object caseClaimReport = caseData.get("claimReport");
        assertNotNull(caseClaimReport);
        assertEquals(caseClaimReport.getClass().getName(), CLAIM_REPORT_CLASS_NAME);

        caseClient.removeCaseInstanceData(CONTAINER_ID, caseId, "claimReport");
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        assertNotNull(caseData);
        assertEquals(1, caseData.size());
        assertEquals("fiat", caseData.get("car"));

        Map<String, Object> data = new HashMap<>();
        data.put("owner", "john");
        data.put("report", caseClaimReport);
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, data);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        assertNotNull(caseData);
        assertEquals(3, caseData.size());
        assertEquals("fiat", caseData.get("car"));
        assertEquals("john", caseData.get("owner"));

        caseClaimReport = caseData.get("report");
        assertNotNull(caseClaimReport);
        assertEquals(caseClaimReport.getClass().getName(), CLAIM_REPORT_CLASS_NAME);
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileThenDestroyIt() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        try {
            // this should throw exception as there is no case any more
            caseClient.getCaseInstance(CONTAINER_ID, caseId);
            fail("Case should not exists any more");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testCreateCancelAndReopenCaseWithEmptyCaseFile() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        try {
            // this should throw exception as there is no case any more
            caseClient.getCaseInstance(CONTAINER_ID, caseId);
            fail("Case should not exists any more");
        } catch (KieServicesException e) {
            // expected
        }
        Map<String, Object> data = new HashMap<>();
        data.put("additionalComment", "reopening the case");
        caseClient.reopenCase(caseId, CONTAINER_ID, CLAIM_CASE_DEF_ID, data);

        caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertNotNull(caseInstance);
        assertEquals(caseId, caseInstance.getCaseId());

        Object additionalComment = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "additionalComment");
        assertNotNull(additionalComment);
        assertEquals("reopening the case", additionalComment);
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileWithContainerAlias() {
        String caseId = caseClient.startCase(CONTAINER_ALIAS, CLAIM_CASE_DEF_ID);

        assertNotNull(caseId);
        assertTrue(caseId.startsWith(CLAIM_CASE_ID_PREFIX));

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ALIAS, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());
    }

    @Test
    public void testCancelCaseInstanceNotExistingContainer() {
        try {
            caseClient.cancelCaseInstance("not-existing-container", CLAIM_CASE_DEF_ID);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testCancelCaseInstanceNotExistingCase() {
        try {
            caseClient.cancelCaseInstance(CONTAINER_ID, "not-existing-case");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testDestroyCaseInstance() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("car", "ford");
        CaseFile caseFile = CaseFile.builder()
                .data(caseData)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        try {
            // this should throw exception as there is no case any more
            caseClient.getCaseInstance(CONTAINER_ID, caseId);
            fail("Case should not exists any more");
        } catch (KieServicesException e) {
            // expected
        }

        try {
            caseClient.reopenCase(caseId, CONTAINER_ID, CLAIM_CASE_DEF_ID);
            fail("Should have failed because destroyed case cannot be reopen.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testDestroyCaseInstanceNotExistingContainer() {
        try {
            caseClient.destroyCaseInstance("not-existing-container", CLAIM_CASE_DEF_ID);
            fail("Should have failed because of not existing container.");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testDestroyCaseInstanceNotExistingCase() {
        try {
            caseClient.destroyCaseInstance(CONTAINER_ID, "not-existing-case");
            fail("Should have failed because of not existing case definition Id.");
        } catch (KieServicesException e) {
            // expected
        }
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
}

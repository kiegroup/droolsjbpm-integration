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

    private static final String CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    private static final String CASE_DESCRIPTION = "CarInsuranceClaimCase";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";

    private static final String CASE_ID_PREFIX = "CAR_INS";

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
    public void testCreateCaseWithEmptyCaseFile() {
        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertNotNull(caseInstance);
            assertEquals(caseId, caseInstance.getCaseId());
            assertEquals(CASE_DEF_ID, caseInstance.getCaseDefinitionId());
            assertEquals(CASE_DESCRIPTION, caseInstance.getCaseDescription());
            assertEquals(USER_YODA, caseInstance.getCaseOwner());
            assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
            assertNotNull(caseInstance.getStartedAt());
            assertNull(caseInstance.getCompletedAt());
            assertEquals("", caseInstance.getCompletionMessage());
            assertEquals(CONTAINER_ID, caseInstance.getContainerId());

            // since roles were not assigned to any users/groups no tasks are available
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(0, tasks.size());
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileButWithRoleAssignments() {
        CaseFile caseFile = CaseFile.builder().addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                                              .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                                              .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertNotNull(caseInstance);
            assertEquals(caseId, caseInstance.getCaseId());
            assertEquals(CASE_DEF_ID, caseInstance.getCaseDefinitionId());
            assertEquals(CASE_DESCRIPTION, caseInstance.getCaseDescription());
            assertEquals(USER_YODA, caseInstance.getCaseOwner());
            assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
            assertNotNull(caseInstance.getStartedAt());
            assertNull(caseInstance.getCompletedAt());
            assertEquals("", caseInstance.getCompletionMessage());
            assertEquals(CONTAINER_ID, caseInstance.getContainerId());

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertNotNull(task);
            assertEquals("Provide accident information", task.getName());
            assertEquals(null, task.getActualOwner());
            assertEquals("Ready", task.getStatus());
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithCaseFile() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("car", "ford");
        CaseFile caseFile = CaseFile.builder().addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .data(caseData)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID, caseFile);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

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
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileThenDestroyIt() {
        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertNotNull(caseInstance);
            assertEquals(caseId, caseInstance.getCaseId());
            assertEquals(CASE_DEF_ID, caseInstance.getCaseDefinitionId());
            assertEquals(CASE_DESCRIPTION, caseInstance.getCaseDescription());
            assertEquals(USER_YODA, caseInstance.getCaseOwner());
            assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
            assertNotNull(caseInstance.getStartedAt());
            assertNull(caseInstance.getCompletedAt());
            assertEquals("", caseInstance.getCompletionMessage());
            assertEquals(CONTAINER_ID, caseInstance.getContainerId());

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
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());

            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileThenReopenIt() {
        String caseId = caseClient.startCase(CONTAINER_ID, CASE_DEF_ID);
        try {
            assertNotNull(caseId);
            assertTrue(caseId.startsWith(CASE_ID_PREFIX));

            CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertNotNull(caseInstance);
            assertEquals(caseId, caseInstance.getCaseId());
            assertEquals(CASE_DEF_ID, caseInstance.getCaseDefinitionId());
            assertEquals(CASE_DESCRIPTION, caseInstance.getCaseDescription());
            assertEquals(USER_YODA, caseInstance.getCaseOwner());
            assertEquals(ProcessInstance.STATE_ACTIVE, caseInstance.getCaseStatus().intValue());
            assertNotNull(caseInstance.getStartedAt());
            assertNull(caseInstance.getCompletedAt());
            assertEquals("", caseInstance.getCompletionMessage());
            assertEquals(CONTAINER_ID, caseInstance.getContainerId());

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
            caseClient.reopenCase(caseId, CONTAINER_ID, CASE_DEF_ID, data);

            caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
            assertNotNull(caseInstance);
            assertEquals(caseId, caseInstance.getCaseId());

            Object additionalComment = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "additionalComment");
            assertNotNull(additionalComment);
            assertEquals("reopening the case", additionalComment);
        } catch (Exception e) {
            fail("Failed due to " + e.getMessage());

            if (caseId != null) {
                caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
            }
        }
    }

}

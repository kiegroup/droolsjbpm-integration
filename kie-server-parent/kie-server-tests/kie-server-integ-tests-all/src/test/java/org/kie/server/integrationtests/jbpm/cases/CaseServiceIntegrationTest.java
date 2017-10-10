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

import org.assertj.core.api.Assertions;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

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

    private static final String CAR_PRODUCER_REPORT_PARAMETER = "carId";
    private static final String CAR_PRODUCER_REPORT_OUTPUT = "carProducerReport";

    private static final String NON_EXISTENT_STAGE_ID = "I don't exist stage";
    private static final String NON_EXISTENT_CASE_ID = "I don't exist case";
    private static final String BAD_CONTAINER_ID = "not-existing-container";
    private static final String BAD_CASE_ID = "not-existing-case";

    private static final String TWO_STAGES_CASE_P_ID = "CaseWithTwoStages";
    private static final String ACCIDENT_TASK_NAME = "Provide accident information";

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

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).isEmpty();
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileButWithRoleAssignments() {
        CaseFile caseFile = CaseFile.builder().addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                                              .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                                              .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        TaskSummary task = tasks.get(0);
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getName()).isEqualTo(ACCIDENT_TASK_NAME);
        Assertions.assertThat(task.getActualOwner()).isNull();
        Assertions.assertThat(task.getStatus()).isEqualTo("Ready");
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

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(1);
        Assertions.assertThat(caseData.get("car")).isEqualTo("ford");

        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "car", "fiat");

        Object carCaseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "car");
        Assertions.assertThat(carCaseData).isNotNull();
        Assertions.assertThat(carCaseData).isInstanceOf(String.class);
        Assertions.assertThat(carCaseData).isEqualTo("fiat");

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).hasSize(1);

        TaskSummary task = tasks.get(0);
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getName()).isEqualTo(ACCIDENT_TASK_NAME);
        Assertions.assertThat(task.getStatus()).isEqualTo("Ready");
        Assertions.assertThat(task.getActualOwner()).isNull();

        Map<String, Object> output = new HashMap<>();
        Object claimReport = createInstance(CLAIM_REPORT_CLASS_NAME);
        KieServerReflections.setValue(claimReport, "name", "John Doe");

        output.put("claimReport_", claimReport);
        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, output);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(2);
        Assertions.assertThat(caseData.get("car")).isEqualTo("fiat");

        Object caseClaimReport = caseData.get("claimReport");
        Assertions.assertThat(caseClaimReport).isNotNull();
        Assertions.assertThat(caseClaimReport.getClass().getName()).isEqualTo(CLAIM_REPORT_CLASS_NAME);

        caseClient.removeCaseInstanceData(CONTAINER_ID, caseId, "claimReport");
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(1);
        Assertions.assertThat(caseData.get("car")).isEqualTo("fiat");

        Map<String, Object> data = new HashMap<>();
        data.put("owner", "john");
        data.put("report", caseClaimReport);
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, data);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(3);
        Assertions.assertThat(caseData.get("car")).isEqualTo("fiat");
        Assertions.assertThat(caseData.get("owner")).isEqualTo("john");
        
        caseClaimReport = caseData.get("report");
        Assertions.assertThat(caseClaimReport).isNotNull();
        Assertions.assertThat(caseClaimReport.getClass().getName()).isEqualTo(CLAIM_REPORT_CLASS_NAME);
        
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, Arrays.asList("car", "owner"));
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(2);
        Assertions.assertThat(caseData.get("car")).isEqualTo("fiat");
        Assertions.assertThat(caseData.get("owner")).isEqualTo("john");

    }

    @Test
    public void testCreateCaseWithEmptyCaseFileThenDestroyIt() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(0, tasks.size());

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        CaseInstance closed = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(closed.getCaseStatus()).isEqualTo(3);
    }

    @Test
    public void testCreateCancelAndReopenCaseWithEmptyCaseFile() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);

        CaseInstance closed = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(closed.getCaseStatus()).isEqualTo(3);

        Map<String, Object> data = new HashMap<>();
        data.put("additionalComment", "reopening the case");
        caseClient.reopenCase(caseId, CONTAINER_ID, CLAIM_CASE_DEF_ID, data);

        caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(caseInstance).isNotNull();
        Assertions.assertThat(caseInstance.getCaseId()).isEqualTo(caseId);

        Object additionalComment = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "additionalComment");
        Assertions.assertThat(additionalComment).isNotNull();
        Assertions.assertThat(additionalComment).isEqualTo("reopening the case");
    }

    @Test
    public void testCreateCaseWithEmptyCaseFileWithContainerAlias() {
        String caseId = caseClient.startCase(CONTAINER_ALIAS, CLAIM_CASE_DEF_ID);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ALIAS, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        // since roles were not assigned to any users/groups no tasks are available
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).hasSize(0);
    }

    @Test
    public void testCancelCaseInstanceNotExistingContainer() {
    	String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);
        assertClientException(() -> caseClient.cancelCaseInstance(BAD_CONTAINER_ID, caseId), 404 , BAD_CONTAINER_ID);
    }

    @Test
    public void testCancelCaseInstanceNotExistingCase() {
        assertClientException(() -> caseClient.cancelCaseInstance(CONTAINER_ID, BAD_CASE_ID), 404 , BAD_CASE_ID);
    }

    @Test
    public void testDestroyCaseInstance() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("car", "ford");
        CaseFile caseFile = CaseFile.builder()
                .data(caseData)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        Assertions.assertThat(caseId).isNotNull();

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        CaseInstance closed = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(closed.getCaseStatus()).isEqualTo(3);
        // Should throw exception because destroyed case cannot be reopen.
        assertClientException(() -> caseClient.reopenCase(caseId, CONTAINER_ID, CLAIM_CASE_DEF_ID), 404 , caseId);
    }

    @Test
    public void testDestroyCaseInstanceNotExistingContainer() {
    	String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);
        assertClientException(() -> caseClient.destroyCaseInstance(BAD_CONTAINER_ID, caseId), 404 , BAD_CONTAINER_ID);
    }

    @Test
    public void testDestroyCaseInstanceNotExistingCase() {
        assertClientException(() -> caseClient.destroyCaseInstance(CONTAINER_ID, BAD_CASE_ID), 404 , BAD_CASE_ID);
    }

    @Test
    public void testAddDynamicWorkItemTask() {
        String carId = "Ford Mustang";
        String producerReportResponse = carId + " was regularly maintained and checked.";

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        Map<String, Object> data = new HashMap<>();
        data.put(CAR_PRODUCER_REPORT_PARAMETER, carId);

        caseClient.addDynamicTask(CONTAINER_ID, caseId, "ContactCarProducer", "Contact car producer", data);

        Map<String, Object> caseInstanceData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseInstanceData).containsKey(CAR_PRODUCER_REPORT_OUTPUT);
        Assertions.assertThat(caseInstanceData.get(CAR_PRODUCER_REPORT_OUTPUT)).isEqualTo(producerReportResponse);
    }

    @Test
    public void testAddDynamicWorkItemTaskNotExistingContainer() {
    	String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);
    	
        assertClientException(
                () -> caseClient.addDynamicTask(BAD_CONTAINER_ID, caseId, "ContactCarProducer", "Contact car producer", null),
                404 , BAD_CONTAINER_ID);
    }

    @Test
    public void testAddDynamicWorkItemTaskNotExistingCase() {
        assertClientException(
                () -> caseClient.addDynamicTask(CONTAINER_ID, BAD_CASE_ID, "ContactCarProducer", "Contact car producer", null),
                404 , BAD_CASE_ID);
    }

    @Test
    public void testAddDynamicTaskToNotExistingStage() throws Exception {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);
        Assertions.assertThat(caseId).isNotNull();
        assertClientException(
                () -> caseClient.addDynamicTaskToStage(CONTAINER_ID, caseId, NON_EXISTENT_STAGE_ID,
                        "ContactCarProducer", "Contact car producer", null), 404, NON_EXISTENT_STAGE_ID);
    }

    @Test
    public void testAddDynamicTaskToStageNotExistingCase() throws Exception {
        assertClientException(
                () -> caseClient.addDynamicTaskToStage(CONTAINER_ID, NON_EXISTENT_CASE_ID, "Stage One",
                        "ContactCarProducer", "Contact car producer", null), 404, NON_EXISTENT_CASE_ID);
    }

    @Test
    public void testAddDynamicTaskToStageInvalidContainer() throws Exception {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);
        Assertions.assertThat(caseId).isNotNull();

        List<CaseStage> caseStages = caseClient.getStages(CONTAINER_ID, caseId, false, 0, 100);
        Assertions.assertThat(caseStages).isNotNull();
        Assertions.assertThat(caseStages).isNotEmpty();

        CaseStage firstCaseStage = caseStages.iterator().next();
        String firstStageId = firstCaseStage.getIdentifier();
        assertClientException(
                () -> caseClient.addDynamicTaskToStage(BAD_CONTAINER_ID, caseId, firstStageId,
                        "ContactCarProducer", "Contact car producer", null), 404, BAD_CONTAINER_ID);
    }
    
    @Test
    public void testCreateCloseAndReopenCaseWithEmptyCaseFile() {
        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        caseClient.closeCaseInstance(CONTAINER_ID, caseId, "work done at the moment");
       
        CaseInstance closed = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(closed.getCaseStatus()).isEqualTo(2);
        Assertions.assertThat(closed.getCompletionMessage()).isEqualTo("work done at the moment");        
        
        Map<String, Object> data = new HashMap<>();
        data.put("additionalComment", "reopening the case");
        caseClient.reopenCase(caseId, CONTAINER_ID, CLAIM_CASE_DEF_ID, data);

        caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(caseInstance).isNotNull();
        Assertions.assertThat(caseInstance.getCaseId()).isEqualTo(caseId);

        Object additionalComment = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "additionalComment");
        Assertions.assertThat(additionalComment).isNotNull();
        Assertions.assertThat(additionalComment).isEqualTo("reopening the case");
    }

    private void assertCarInsuranceCaseInstance(CaseInstance caseInstance, String caseId, String owner) {
        Assertions.assertThat(caseInstance).isNotNull();
        Assertions.assertThat(caseInstance.getCaseId()).isEqualTo(caseId);
        Assertions.assertThat(caseInstance.getCaseDefinitionId()).isEqualTo(CLAIM_CASE_DEF_ID);
        Assertions.assertThat(caseInstance.getCaseDescription()).isEqualTo(CLAIM_CASE_DESRIPTION);
        Assertions.assertThat(caseInstance.getCaseOwner()).isEqualTo(owner);
        Assertions.assertThat(caseInstance.getCaseStatus().intValue()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Assertions.assertThat(caseInstance.getStartedAt()).isNotNull();
        Assertions.assertThat(caseInstance.getCompletedAt()).isNull();
        Assertions.assertThat(caseInstance.getCompletionMessage()).isEqualTo("");
        Assertions.assertThat(caseInstance.getContainerId()).isEqualTo(CONTAINER_ID);
    }
}

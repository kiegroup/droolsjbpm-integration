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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

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
    private static final String CASE_ASSESSOR_ROLE = "assessor";

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
    public void testStartNonExistingCaseDefinition() {
        assertClientException(() -> caseClient.startCase(CONTAINER_ID, "NonExistingCaseDefinition"), 404,
                              "Could not find case definition \"NonExistingCaseDefinition\" in container \"insurance\"");
    }

    @Test
    public void testCreateCaseWithEmptyCaseFile() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).hasSize(1);
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
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        caseClient.destroyCaseInstance(CONTAINER_ID, caseId);

        CaseInstance closed = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        Assertions.assertThat(closed.getCaseStatus()).isEqualTo(3);
    }

    @Test
    public void testCreateCancelAndReopenCaseWithEmptyCaseFile() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

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
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ALIAS, caseId);
        assertCarInsuranceCaseInstance(caseInstance, caseId, USER_YODA);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    public void testCancelCaseInstanceNotExistingContainer() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
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
                .addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .addUserAssignments(CASE_ASSESSOR_ROLE, USER_YODA)
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
    	String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
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

        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

        Map<String, Object> data = new HashMap<>();
        data.put(CAR_PRODUCER_REPORT_PARAMETER, carId);

        caseClient.addDynamicTask(CONTAINER_ID, caseId, "ContactCarProducer", "Contact car producer", data);

        Map<String, Object> caseInstanceData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseInstanceData).containsKey(CAR_PRODUCER_REPORT_OUTPUT);
        Assertions.assertThat(caseInstanceData.get(CAR_PRODUCER_REPORT_OUTPUT)).isEqualTo(producerReportResponse);
    }

    @Test
    public void testAddDynamicWorkItemTaskNotExistingContainer() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
    	
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
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
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
    public void testAddDynamicUserTaskToNonExistingStage() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        assertClientException(
                () -> caseClient.addDynamicUserTaskToStage(
                        CONTAINER_ID,
                        caseId,
                        NON_EXISTENT_STAGE_ID,
                        "ContactCarProducer",
                        "Contact car producer",
                        USER_JOHN,
                        null,
                        null),
                404, "No stage found with id " + NON_EXISTENT_STAGE_ID);
    }

    @Test
    public void testAddDynamicUserTaskToInactiveStage() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        List<CaseStage> caseStages = caseClient.getStages(CONTAINER_ID, caseId, false, 0, 50);
        Assertions.assertThat(caseStages).isNotEmpty();
        String inactiveStageId = caseStages.stream().filter(stage -> stage.getActiveNodes().isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No inactive stage found."))
                .getIdentifier();

        assertClientException(
                () -> caseClient.addDynamicUserTaskToStage(
                        CONTAINER_ID,
                        caseId,
                        inactiveStageId,
                        "ContactCarProducer",
                        "Contact car producer",
                        USER_JOHN,
                        "mygroup",
                        null),
                404,
                "No stage found"
        );
    }

    @Test
    public void testAddDynamicUserTaskToActiveStage() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
        List<CaseStage> caseStages = caseClient.getStages(CONTAINER_ID, caseId, false, 0, 50);
        Assertions.assertThat(caseStages).isNotEmpty();
        String activeStageId = caseStages.stream().filter(stage -> stage.getActiveNodes().size() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found."))
                .getIdentifier();

        final String taskName = "ContactCarProducer";
        caseClient.addDynamicUserTaskToStage(
                CONTAINER_ID,
                caseId,
                activeStageId,
                taskName,
                "Contact car producer",
                USER_JOHN,
                "mygroup",
                null);

        TaskSummary currentTask;
        do {
            List<TaskSummary> activeTasks = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN,0, 50);
            Assertions.assertThat(activeTasks).isNotEmpty();
            currentTask = activeTasks.get(0);
            taskClient.completeAutoProgress(CONTAINER_ID, currentTask.getId(), USER_JOHN, new HashMap<>());
            System.out.println(currentTask);
        } while (currentTask.getName() != taskName);
    }

    @Test
    public void testAddDynamicTaskToStageInvalidContainer() {
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);
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
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN, USER_YODA);

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

    @Test
    public void testCreateCaseWithCaseFileWithRestrictions() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("car", "ford");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, USER_YODA)
                .addUserAssignments(CASE_INS_REP_ROLE, USER_JOHN)
                .addDataAccessRestrictions("car", CASE_INSURED_ROLE)
                .data(caseData)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);

        Assertions.assertThat(caseId).isNotNull();
        Assertions.assertThat(caseId).startsWith(CLAIM_CASE_ID_PREFIX);

        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(1);
        Assertions.assertThat(caseData.get("car")).isEqualTo("ford");
        
        // remove yoda from insured role to simulate lack of access
        caseClient.removeUserFromRole(CONTAINER_ID, caseId, CASE_INSURED_ROLE, USER_YODA);
        
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(0);
        
        // add back yoda to insured role
        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_INSURED_ROLE, USER_YODA);
        
        List<String> restrictions = new ArrayList<>();
        restrictions.add(CaseServicesClient.ACCESS_PUBLIC_GROUP);

        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "car", "fiat", restrictions);

        Object carCaseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "car");
        Assertions.assertThat(carCaseData).isNotNull();
        Assertions.assertThat(carCaseData).isInstanceOf(String.class);
        Assertions.assertThat(carCaseData).isEqualTo("fiat");

        // remove yoda from insured role to simulate lack of access
        caseClient.removeUserFromRole(CONTAINER_ID, caseId, CASE_INSURED_ROLE, USER_YODA);
        // but it should have access to it as all now eligible to see it
        carCaseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "car");
        Assertions.assertThat(carCaseData).isNotNull();
        Assertions.assertThat(carCaseData).isInstanceOf(String.class);
        Assertions.assertThat(carCaseData).isEqualTo("fiat");
        
        restrictions = new ArrayList<>();
        restrictions.add(CASE_INSURED_ROLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("car", "opel");

        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, updates, restrictions);
        
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(0);
               
        assertClientException(() -> caseClient.removeCaseInstanceData(CONTAINER_ID, caseId, "car"), 403, "does not have access to data item named car");
        
        // add back yoda to insured role
        caseClient.assignUserToRole(CONTAINER_ID, caseId, CASE_INSURED_ROLE, USER_YODA);
        
        caseClient.removeCaseInstanceData(CONTAINER_ID, caseId, "car");
        caseData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(caseData).isNotNull();
        Assertions.assertThat(caseData).hasSize(0);
    }

    @Test
    public void testRetrievalOfNonExistingCaseData() {
        String caseId = startCarInsuranceClaimCaseWithEmptyData(USER_YODA, USER_JOHN, USER_YODA);
        assertNotNull(caseId);

        Map<String, Object> data = caseClient.getCaseInstanceData(CONTAINER_ID, caseId);
        Assertions.assertThat(data).isEmpty();
        Object nonExistingData = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "NonExistingData");
        Assertions.assertThat(nonExistingData).isNull();

        assertClientException(
                () -> caseClient.getCaseInstanceData(CONTAINER_ID, "NonExistingCaseId"),
                404,
                "Could not find case instance \"NonExistingCaseId\"");
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
    
    private String startCarInsuranceClaimCase(String insured, String insuranceRep, String assessor) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        return startCarInsuranceClaimCase(insured, insuranceRep, assessor, data);
    }

    private String startCarInsuranceClaimCase(String insured, String insuranceRep, String assessor, Map<String, Object> data) {
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

    private String startCarInsuranceClaimCaseWithEmptyData(String insured, String insuranceRep, String assessor) {
        return startCarInsuranceClaimCase(insured, insuranceRep, assessor, new HashMap<>());
    }
}

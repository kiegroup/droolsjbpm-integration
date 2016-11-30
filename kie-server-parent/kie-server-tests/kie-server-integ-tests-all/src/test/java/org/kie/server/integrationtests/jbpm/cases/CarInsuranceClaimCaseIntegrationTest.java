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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class CarInsuranceClaimCaseIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";

    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";

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

    @After
    public void resetUser() throws Exception {
        changeUser(TestConfig.getUsername());
    }

    @Test
    public void testCarInsuranceClaimCase() throws Exception {
        // start case with users assigned to roles
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
        // let's verify case is created
        assertCaseInstance(caseId);
        // let's look at what stages are active
        assertBuildClaimReportStage(caseId);
        // since the first task assigned to insured is with auto start it should be already active
        // the same task can be claimed by insuranceRepresentative in case claim is reported over phone
        long taskId = assertBuildClaimReportAvailableForBothRoles(USER_YODA, USER_JOHN);
        // let's provide claim report with initial data
        // claim report should be stored in case file data
        provideAndAssertClaimReport(caseId, taskId, USER_YODA);
        // now we have another task for insured to provide property damage report
        taskId = assertPropertyDamageReportAvailableForBothRoles(USER_YODA, USER_JOHN);
        // let's provide the property damage report
        provideAndAssertPropertyDamageReport(caseId, taskId, USER_YODA);
        // let's complete the stage by explicitly stating that claimReport is done
        caseClient.putCaseInstanceData(CONTAINER_ID, caseId, "claimReportDone", Boolean.TRUE);
        // we should be in another stage - Claim assessment
        assertClaimAssesmentStage(caseId);
        // let's trigger claim offer calculation
        caseClient.triggerAdHocFragment(CONTAINER_ID, caseId, "Calculate claim", null);
        // now we have another task for insured as claim was calculated
        // let's accept the calculated claim
        assertAndAcceptClaimOffer(USER_YODA);
        // there should be no process instances for the case
        Collection<ProcessInstance> caseProcesInstances = caseClient.getProcessInstances(CONTAINER_ID, caseId, Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE), 0, 10);
        assertEquals(0, caseProcesInstances.size());
    }

    private void assertTask(TaskSummary task, String actor, String name, String status) {
        assertNotNull(task);
        assertEquals(name, task.getName());
        assertEquals(actor, task.getActualOwner());
        assertEquals(status, task.getStatus());
    }

    private void assertCaseInstance(String caseId) {
        CaseInstance cInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertNotNull(cInstance);
        assertEquals(caseId, cInstance.getCaseId());
        assertEquals(CLAIM_CASE_DEF_ID, cInstance.getCaseDefinitionId());
    }

    private void assertBuildClaimReportStage(String caseId) {
        List<CaseStage> activeStages = caseClient.getStages(CONTAINER_ID, caseId, true, 0, 10);
        assertEquals(1, activeStages.size());
        CaseStage stage = activeStages.iterator().next();
        assertEquals("Build claim report", stage.getName());
    }

    private void assertClaimAssesmentStage(String caseId) {
        List<CaseStage> activeStages = caseClient.getStages(CONTAINER_ID, caseId, true, 0, 10);
        assertEquals(1, activeStages.size());
        CaseStage stage = activeStages.iterator().next();
        assertEquals("Claim assesment", stage.getName());
    }

    private long assertBuildClaimReportAvailableForBothRoles(String insured, String insuranceRep) throws Exception {
        return assertTasksForBothRoles("Provide accident information", insured, insuranceRep, Status.Ready.toString());
    }

    private long assertPropertyDamageReportAvailableForBothRoles(String insured, String insuranceRep) throws Exception {
        return assertTasksForBothRoles("File property damage claim", insured, insuranceRep, Status.Ready.toString());
    }

    private long assertTasksForBothRoles(String taskName, String insured, String insuranceRep, String status) throws Exception {
        changeUser(insured);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(insured, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertTask(tasks.get(0), null, taskName, status);

        changeUser(insuranceRep);

        // the same task can be claimed by insuranceRepresentative in case claim is reported over phone
        tasks = taskClient.findTasksAssignedAsPotentialOwner(insuranceRep, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertTask(tasks.get(0), null, taskName, status);

        changeUser(TestConfig.getUsername());

        return tasks.get(0).getId();
    }

    private void provideAndAssertClaimReport(String caseId, Long taskId, String insured) {
        Object claimReport = createInstance(CLAIM_REPORT_CLASS_NAME);
        setValue(claimReport, "name", "John Doe");
        setValue(claimReport, "address", "Main street, NY");
        setValue(claimReport, "accidentDescription", "It happened so sudden...");
        setValue(claimReport, "accidentDate", new Date());

        Map<String, Object> params = new HashMap<>();
        params.put("claimReport_", claimReport);
        taskClient.completeAutoProgress(CONTAINER_ID, taskId, insured, params);

        // claim report should be stored in case file data
        Object caseClaimReport = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "claimReport");
        assertNotNull(caseClaimReport);
        assertEquals("John Doe", valueOf(caseClaimReport, "name"));
        assertEquals("Main street, NY", valueOf(caseClaimReport, "address"));
        assertEquals("It happened so sudden...", valueOf(caseClaimReport, "accidentDescription"));
        assertNotNull(valueOf(caseClaimReport, "accidentDate"));
    }

    private void provideAndAssertPropertyDamageReport(String caseId, Long taskId, String insured) {
        Object damageReport = createInstance(PROPERTY_DAMAGE_REPORT_CLASS_NAME);
        setValue(damageReport, "description", "Car is completely destroyed");
        setValue(damageReport, "value", 1000.0);

        Map<String, Object> params = new HashMap<>();
        params.put("propertyDamageReport_", damageReport);
        taskClient.completeAutoProgress(CONTAINER_ID, taskId, insured, params);

        // property damage report should be stored in case file data
        Object casePropertyDamageReport = caseClient.getCaseInstanceData(CONTAINER_ID, caseId, "propertyDamageReport");
        assertNotNull(casePropertyDamageReport);
        assertEquals("Car is completely destroyed", valueOf(casePropertyDamageReport, "description"));
        assertEquals(1000.0, ((Double)valueOf(casePropertyDamageReport, "value")).doubleValue(), 0);
    }

    private void assertAndAcceptClaimOffer(String insured) throws Exception {
        changeUser(insured);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(insured, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertTask(tasks.get(0), insured, "Present calculated claim", Status.Reserved.toString());

        changeUser(TestConfig.getUsername());

        // let's accept the calculated claim
        Map<String, Object> params = new HashMap<>();
        params.put("accepted", true);
        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), insured, params);
    }

    private String startCarInsuranceClaimCase(String insured, String insuranceRep) {
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, insured)
                .addUserAssignments(CASE_INS_REP_ROLE, insuranceRep)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }
}

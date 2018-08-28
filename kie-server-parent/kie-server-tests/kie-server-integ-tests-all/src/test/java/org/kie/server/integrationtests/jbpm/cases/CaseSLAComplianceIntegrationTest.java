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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class CaseSLAComplianceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-sla", "1.0.0.Final");

    private static final String CONTAINER_ID = "sla";

    private static final String CASE_SLA_OWNER_ROLE = "owner";
    private static final String CASE_SLA_ADMIN_ROLE = "admin";

    private static final String SLA_CASE_DEF_ID = "UserTaskCaseSLA";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-sla").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void resetUser() throws Exception {
        changeUser(TestConfig.getUsername());
    }

    @Test
    public void testStartCaseWithSLAEscalation() throws Exception{
        Map<String, Object> data = new HashMap<>();
        data.put("s", "with SLA");

        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_SLA_OWNER_ROLE, USER_JOHN)
                .addUserAssignments(CASE_SLA_ADMIN_ROLE, USER_MARY)
                .data(data)
                .build();

        changeUser(USER_JOHN);
        String caseId = caseClient.startCase(CONTAINER_ID, SLA_CASE_DEF_ID, caseFile);
        CaseInstance caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);

        assertThat(caseInstance.getCaseId()).isEqualTo(caseId);
        assertThat(caseInstance.getCaseDefinitionId()).isEqualTo(SLA_CASE_DEF_ID);
        assertThat(caseInstance.getContainerId()).isEqualTo(CONTAINER_ID);
        assertThat(caseInstance.getCaseDescription()).isEqualTo("Case with SLA");
        assertThat(caseInstance.getSlaCompliance()).isEqualTo(ProcessInstance.SLA_PENDING);
        assertThat(caseInstance.getSlaDueDate()).isCloseTo(new Date(), 30_000L);

        List<TaskSummary> johnTasks = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
        TaskSummary johnTask = johnTasks.get(0);
        assertThat(johnTasks).hasSize(1);
        assertThat(johnTask.getName()).isEqualTo("Hello1");

        changeUser(USER_MARY);
        List<TaskSummary> maryTasks = taskClient.findTasksAssignedAsPotentialOwner(USER_MARY, 0, 10);
        // No SLA escalation tasks for Mary yet
        assertThat(maryTasks).isEmpty();

        Long caseProcessInstanceId = johnTask.getProcessInstanceId();

        // Wait for SLA to expire
        KieServerSynchronization.waitForProcessInstanceSLAViolated(queryClient, caseProcessInstanceId, 6_000L);

        caseInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertThat(caseInstance.getCaseId()).isEqualTo(caseId);
        assertThat(caseInstance.getSlaCompliance()).isEqualTo(ProcessInstance.SLA_VIOLATED);
        assertThat(caseInstance.getSlaDueDate()).isCloseTo(new Date(), 30_000L);

        maryTasks = taskClient.findTasksAssignedAsPotentialOwner(USER_MARY, 0, 10);
        // SLA Escalation task for Mary should be created
        assertThat(maryTasks).hasSize(1);

        TaskSummary escalationTask = maryTasks.get(0);
        assertThat(escalationTask.getName()).isEqualTo("SLA violation for case " + caseId);
        assertThat(escalationTask.getDescription()).isEqualTo("Service Level Agreement has been violated for case " + caseId);

    }


}

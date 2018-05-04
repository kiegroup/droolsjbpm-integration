/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class CaseInstanceMigrationIntegrationTest extends JbpmKieServerBaseIntegrationTest {


    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");
    private static ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.1.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String CONTAINER_ID_2 = "insurance_2";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";
    private static final String CASE_INS_ASSESSOR_ROLE = "assessor";

    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    private static final String CLAIM_CASE_DEF_ID_2 = "insurance-claims.CarInsuranceClaimCase2";
    
    private static final String PROCESS_DEF_ID = "UserTask";
    private static final String PROCESS_DEF_ID_2 = "UserTask2";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance-101").getFile());
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        // use different aliases to avoid policy based removal - keep latest only
        createContainer(CONTAINER_ID, releaseId, CONTAINER_ID);
        createContainer(CONTAINER_ID_2, releaseId101, CONTAINER_ID_2);
    }
    
    
    @Test
    public void testCarInsuranceClaimCaseMigration() throws Exception {
        // start case with users assigned to roles
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
        // let's verify case is created
        assertCaseInstance(caseId);
        
        Map<String, String> processMapping = new HashMap<>();
        processMapping.put(CLAIM_CASE_DEF_ID, CLAIM_CASE_DEF_ID_2);
        
        CaseMigrationReportInstance report = caseAdminClient.migrateCaseInstance(CONTAINER_ID, caseId, CONTAINER_ID_2, processMapping);
        assertThat(report).isNotNull();
        assertThat(report.isSuccessful()).isTrue();
        
        assertMigratedCaseInstance(caseId);
    }
    
    @Test
    public void testCarInsuranceClaimCaseMigrationWithSubprocess() throws Exception {
        // start case with users assigned to roles
        String caseId = startCarInsuranceClaimCase(USER_YODA, USER_JOHN);
        // let's verify case is created
        assertCaseInstance(caseId);
                
        caseClient.addDynamicSubProcess(CONTAINER_ID, caseId, PROCESS_DEF_ID, null);
        
        List<ProcessInstance> processInstances = caseClient.getProcessInstances(CONTAINER_ID, caseId, Arrays.asList(1), 0, 10);
        assertThat(processInstances).isNotNull();
        assertThat(processInstances).hasSize(2);
        
        for (ProcessInstance pi : processInstances) {
            assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID);   
        }
        
        Map<String, String> processMapping = new HashMap<>();
        processMapping.put(CLAIM_CASE_DEF_ID, CLAIM_CASE_DEF_ID_2);
        processMapping.put(PROCESS_DEF_ID, PROCESS_DEF_ID_2);
        
        CaseMigrationReportInstance report = caseAdminClient.migrateCaseInstance(CONTAINER_ID, caseId, CONTAINER_ID_2, processMapping);
        assertThat(report).isNotNull();
        assertThat(report.isSuccessful()).isTrue();
        
        assertMigratedCaseInstance(caseId);
        
        processInstances = caseClient.getProcessInstances(CONTAINER_ID_2, caseId, Arrays.asList(1), 0, 10);
        assertThat(processInstances).isNotNull();
        assertThat(processInstances).hasSize(2);
        
        for (ProcessInstance pi : processInstances) {
            assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID_2);   
        }
    }

    
    
    private String startCarInsuranceClaimCase(String insured, String insuranceRep) {
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_INSURED_ROLE, insured)
                .addUserAssignments(CASE_INS_REP_ROLE, insuranceRep)
                .addUserAssignments(CASE_INS_ASSESSOR_ROLE, insuranceRep)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CLAIM_CASE_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }
    
    private void assertCaseInstance(String caseId) {
        CaseInstance cInstance = caseClient.getCaseInstance(CONTAINER_ID, caseId);
        assertNotNull(cInstance);
        assertEquals(caseId, cInstance.getCaseId());
        assertEquals(CLAIM_CASE_DEF_ID, cInstance.getCaseDefinitionId());
        assertEquals(CONTAINER_ID, cInstance.getContainerId());
    }
    
    private void assertMigratedCaseInstance(String caseId) {
        CaseInstance cInstance = caseClient.getCaseInstance(CONTAINER_ID_2, caseId);
        assertNotNull(cInstance);
        assertEquals(caseId, cInstance.getCaseId());
        assertEquals(CLAIM_CASE_DEF_ID_2, cInstance.getCaseDefinitionId());
        assertEquals(CONTAINER_ID_2, cInstance.getContainerId());
    }
}

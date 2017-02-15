/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class CaseAdminServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";
    private static final String PROPERTY_DAMAGE_REPORT_CLASS_NAME = "org.kie.server.testing.PropertyDamageReport";
    private static final String CLAIM_REPORT_CLASS_NAME = "org.kie.server.testing.ClaimReport";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";

    private static final String CASE_INSURED_ROLE = "insured";
    private static final String CASE_INS_REP_ROLE = "insuranceRepresentative";
    private static final String CASE_ASSESSOR_ROLE = "assessor";

    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    private static final String CASE_HR_DEF_ID = "UserTaskCase";

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
    public void testGetCaseInstances() {
        // Test is using user authentication, isn't available for local execution(which has mocked authentication info).
        Assume.assumeFalse(TestConfig.isLocalServer());

        String caseId = startUserTaskCase(USER_JOHN, USER_MARY);
        assertNotNull(caseId);

        // yoda is not involved in case at all so should not see case at all
        List<CaseInstance> caseInstances = caseClient.getCaseInstances(0, 10);
        assertEquals(0, caseInstances.size());

        // Admin client should return all case instances regardless of user
        caseInstances = caseAdminClient.getCaseInstances(0, 10);
        assertEquals(1, caseInstances.size());

        caseInstances = caseAdminClient.getCaseInstances(1, 10);
        assertEquals(0, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesByStatus() throws Exception {
        // Test is using user authentication, isn't available for local execution(which has mocked authentication info).
        Assume.assumeFalse(TestConfig.isLocalServer());

        List<CaseInstance> caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ABORTED), 0, 1000);
        assertNotNull(caseInstances);
        int abortedCaseInstanceCount = caseInstances.size();

        String caseId = startUserTaskCase(USER_JOHN, USER_MARY);

        caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10);
        assertEquals(1, caseInstances.size());

        caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 10);
        assertEquals(0, caseInstances.size());

        // Only users in roles defined in case-authorization.properties can cancel Case
        changeUser(USER_JOHN);
        caseClient.cancelCaseInstance(CONTAINER_ID, caseId);
        changeUser(USER_YODA);

        caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ABORTED), 0, 1000);
        assertNotNull(caseInstances);
        assertEquals(abortedCaseInstanceCount + 1, caseInstances.size());
    }

    @Test
    public void testGetCaseInstancesSorting() {
        String hrCaseId = startUserTaskCase(USER_JOHN, USER_MARY);
        String claimCaseId = startCarInsuranceClaimCase(USER_JOHN, USER_MARY, USER_YODA);
        assertNotEquals(hrCaseId, claimCaseId);

        List<CaseInstance> caseInstances = caseAdminClient.getCaseInstances(0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(claimCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseAdminClient.getCaseInstances(1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseAdminClient.getCaseInstances(0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertEquals(2, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());
        assertEquals(claimCaseId, caseInstances.get(1).getCaseId());
    }

    @Test
    public void testGetCaseInstancesByStatusSorting() {
        String hrCaseId = startUserTaskCase(USER_JOHN, USER_MARY);
        String claimCaseId = startCarInsuranceClaimCase(USER_JOHN, USER_MARY, USER_YODA);
        assertNotEquals(hrCaseId, claimCaseId);

        List<CaseInstance> caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(claimCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 1, 1, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, true);
        assertEquals(1, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());

        caseInstances = caseAdminClient.getCaseInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), 0, 10, CaseServicesClient.SORT_BY_CASE_INSTANCE_ID, false);
        assertEquals(2, caseInstances.size());
        assertEquals(hrCaseId, caseInstances.get(0).getCaseId());
        assertEquals(claimCaseId, caseInstances.get(1).getCaseId());
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
}

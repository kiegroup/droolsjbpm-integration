/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseIdGeneratorIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-id-generator", "1.0.0.Final");

    private static final String CONTAINER_ID = "id-generator";

    private static final String CASE_OWNER_ROLE = "owner";
    
    private static final String CORRELATION_KEY = "my correlation key";
    
    private static final String EMPTY_CASE_WITH_PREFIX_CASE_ID = "EmptyCaseWithCasePrefixId";
    private static final String EMPTY_CASE_WITH_CASE_DEPLOYMENT_ID = "EmptyCaseWithCaseDeploymentId";
    private static final String EMPTY_CASE_WITH_CASE_DEFINITION_ID = "EmptyCaseWithCaseDefinitionId";
    private static final String EMPTY_CASE_WITH_CASE_CORRELATION_KEY = "EmptyCaseWithCaseCorrelationKey";
    private static final String EMPTY_CASE_WITH_NO_SEQ_CORRELATION_KEY = "EmptyCaseWithNoSequenceCorrelationKey";
    private static final String EMPTY_CASE_WITH_UPPER_PREFIX_ID_EXPRESSION = "EmptyCaseWithUpperPrefixIdExpression";
    private static final String EMPTY_CASE_WITH_TRUNCATE_PREFIX_ID_EXPRESSION = "EmptyCaseWithTruncatePrefixIdExpression";
    private static final String EMPTY_CASE_WITH_LPAD_PREFIX_ID_EXPRESSION = "EmptyCaseWithLpadPrefixIdExpression";
    private static final String EMPTY_CASE_WITH_RPAD_PREFIX_ID_EXPRESSION = "EmptyCaseWithRpadPrefixIdExpression";
    

    private Map<String, Object> data;
    private CaseFile caseFile;

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/case-id-generator");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }
    
    @Before
    public void init() {
        data = new HashMap<>();
    }

    @Test
    public void testStartEmptyCaseWithCaseDefaultPrefixId() {
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_PREFIX_CASE_ID);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match with expected value, value is: " + caseId, 
                    caseId.matches("CASE-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithNoSequenceCaseDefaultPrefixId() {
        data.put("IS_PREFIX_SEQUENCE", false);
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_PREFIX_CASE_ID, caseFile);
        assertNotNull(caseId);
        assertEquals("CASE", caseId);
    }
    
    @Test
    public void testStartEmptyCaseWithCaseDeploymentId() {
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_DEPLOYMENT_ID);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match with expected value, value is: " + caseId, 
                    caseId.matches(CONTAINER_ID+"-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithNoSequenceCaseDeploymentId() {
        data.put("IS_PREFIX_SEQUENCE", false);
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_DEPLOYMENT_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(CONTAINER_ID, caseId);
    }
    
    @Test
    public void testStartEmptyCaseWithCaseDefinitionId() {
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_DEFINITION_ID);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match with expected value, value is: " + caseId, 
                caseId.matches(EMPTY_CASE_WITH_CASE_DEFINITION_ID+"-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithNoSequenceCaseDefinitionId() {
        data.put("IS_PREFIX_SEQUENCE", false);
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_DEFINITION_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(EMPTY_CASE_WITH_CASE_DEFINITION_ID, caseId);
    }
    
    @Test
    public void testStartEmptyCaseWithCaseCorrelationId() {
        data.put("CORRELATION_KEY", CORRELATION_KEY);
        buildCaseFile();
        
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_CORRELATION_KEY, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(CORRELATION_KEY+"-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithNoSequenceCaseCorrelationKey() {
        data.put("CORRELATION_KEY", CORRELATION_KEY);
        data.put("IS_PREFIX_SEQUENCE", false);
        buildCaseFile();
        
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_CASE_CORRELATION_KEY, caseFile);
        assertNotNull(caseId);
        assertEquals(CORRELATION_KEY, caseId);
    }

    @Test
    public void testStartEmptyCaseWithNoSeqCorrelationKeyDefinedAtResource() {
        data.put("CORRELATION_KEY", CORRELATION_KEY);
        buildCaseFile();
        
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_NO_SEQ_CORRELATION_KEY, caseFile);
        assertNotNull(caseId);
        assertEquals(CORRELATION_KEY, caseId);
    }
    
    @Test
    public void testStartEmptyCaseWithUpperPrefixIdExpression() {
        data.put("type", "type1");
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_UPPER_PREFIX_ID_EXPRESSION, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(EMPTY_CASE_WITH_UPPER_PREFIX_ID_EXPRESSION+"-TYPE1-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithTruncatePrefixIdExpression() {
        data.put("type", "type_very_very_long");
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_TRUNCATE_PREFIX_ID_EXPRESSION, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(EMPTY_CASE_WITH_TRUNCATE_PREFIX_ID_EXPRESSION+"-type_very-[0-9]+"));
    }

    @Test
    public void testStartEmptyCaseWithLpadPrefixIdExpression() {
        data.put("type", "type");
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_LPAD_PREFIX_ID_EXPRESSION, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(CONTAINER_ID+"-0000type-[0-9]+"));
    }
    
    @Test
    public void testStartEmptyCaseWithRpadPrefixIdExpression() {
        data.put("type", "type");
        buildCaseFile();
        String caseId = caseClient.startCase(CONTAINER_ID, EMPTY_CASE_WITH_RPAD_PREFIX_ID_EXPRESSION, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(CONTAINER_ID+"-type  -[0-9]+"));
    }
    
    private void buildCaseFile() {
        caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .data(data)
                .build();
    }
}

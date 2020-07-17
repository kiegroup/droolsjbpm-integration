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

package org.kie.server.integrationtests.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CustomCaseIdIntegrationTest extends CaseIdBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CASE_HR_DEF_ID = "UserTaskCase";
    private static final String PREFIX_VARIABLE_LOWER_CAPITALIZE = "EmptyCaseIdExpressionPrefixVariableLowerCapitalize";

    private static final String CONTAINER_ID = "insurance";

    private static final String CASE_OWNER_ROLE = "owner";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/case-insurance");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testCustomCaseId() {
        Assume.assumeFalse(TestConfig.isLocalServer());

        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_HR_DEF_ID, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't start with expected value, value is: " + caseId, caseId.startsWith("HR-01234"));
    }
    
    @Test
    public void testCustomCaseIdWithOptionalParams() {
        Assume.assumeFalse(TestConfig.isLocalServer());

        Map<String, Object> data = new HashMap<>();
        data.put("var1", "VALUE1");
        data.put("country", "spa");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, PREFIX_VARIABLE_LOWER_CAPITALIZE, caseFile);
        assertNotNull(caseId);
        assertTrue("Created Case Id doesn't match expected value, value is: " + caseId, 
                    caseId.matches(CONTAINER_ID+"-value1-Spa-01234[0-9]+"));
                
    }
    
    @Test
    public void testCustomCaseIdWithoutParamsThrowsException() {
        Assume.assumeFalse(TestConfig.isLocalServer());

        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, USER_YODA)
                .data(Collections.emptyMap())
                .build();

        assertThatThrownBy(() -> caseClient.startCase(CONTAINER_ID, PREFIX_VARIABLE_LOWER_CAPITALIZE, caseFile))
                .isInstanceOf(KieServicesException.class)
                .hasMessageContaining("Case Id Prefix cannot be generated");
    }
}

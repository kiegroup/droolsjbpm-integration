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

package org.kie.server.integrationtests.dmn;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DMNLocalDateIntegrationTest
        extends DMNKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "local-date-unary-check",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "local-date-unary-check";
    private static final String CONTAINER_1_ALIAS = "localdate";
    private static final String PERSON_CLASS_NAME = "org.example.localdateunarytest.Person";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/local-date-unary-check");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1, CONTAINER_1_ALIAS);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void test_evaluateLocaleDateUnaryCheck() {
        DMNContext dmnContext = dmnClient.newContext();
        final Object person = KieServerReflections.createInstance(PERSON_CLASS_NAME, kieContainer.getClassLoader(), "person-id-1", "Eager Join", LocalDate.of(2021, 8, 11));
        dmnContext.set("PersonData", person);
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID,
                                                                       "https://kiegroup.org/dmn/_5851203A-5DA1-4020-B7D1-E23C89634164",
                                                                       "Test",
                                                                       dmnContext);

        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());

        DMNResult dmnResult = evaluateAll.getResult();

        final Map<String, Object> expectedPerson = new HashMap<>();
        expectedPerson.put("id", "abc");
        expectedPerson.put("name", "xyz");

        assertThat(dmnResult.getDecisionResults().size(), is(1));
        assertThat(dmnResult.getDecisionResultByName("PersonDecision").getResult(), is(expectedPerson));
    }
}
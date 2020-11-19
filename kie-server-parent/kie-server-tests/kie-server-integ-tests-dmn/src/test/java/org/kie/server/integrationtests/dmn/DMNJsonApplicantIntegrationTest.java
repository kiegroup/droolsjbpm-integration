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

package org.kie.server.integrationtests.dmn;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DMNJsonApplicantIntegrationTest extends DMNKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "json-applicant", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "json-applicant";

    private static final String APPLICANT_FQCN = "com.acme.Applicant";
    private static final String ADDRESS_FQCN = "com.acme.Address";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/json-applicant");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(APPLICANT_FQCN, Class.forName(APPLICANT_FQCN, true, kieContainer.getClassLoader()));
        extraClasses.put(ADDRESS_FQCN, Class.forName(ADDRESS_FQCN, true, kieContainer.getClassLoader()));
    }

    @Test
    public void test_model1() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set("Applicant", 47);

        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID,
                                                                       "https://kiegroup.org/dmn/_EB0C0DE7-9BC0-4CE2-B406-5F25550B9F76",
                                                                       "model1",
                                                                       dmnContext);
        KieServerAssert.assertSuccess(evaluateAll);
        
        DMNResult dmnResult = evaluateAll.getResult();
        assertThat(dmnResult.getMessages().toString(), dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision-1"), is(Boolean.TRUE));
    }
    
    @Test
    public void test_model2() {
        DMNContext dmnContext = dmnClient.newContext();
        Object address = KieServerReflections.createInstance(ADDRESS_FQCN, kieContainer.getClassLoader(), "Italy", "12345");
        Object applicant = KieServerReflections.createInstance(APPLICANT_FQCN, kieContainer.getClassLoader(), "John Doe", 47, address);
        dmnContext.set("Applicant", applicant);

        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID,
                                                                       "https://kiegroup.org/dmn/_8E568485-BC02-4917-830C-39D17632BD3A",
                                                                       "model2",
                                                                       dmnContext);
        KieServerAssert.assertSuccess(evaluateAll);

        DMNResult dmnResult = evaluateAll.getResult();
        assertThat(dmnResult.getMessages().toString(), dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision-1"), is("John Doe lives in Italy"));
    }

}
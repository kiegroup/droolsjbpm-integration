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

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNDecisionServiceInfo;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class DMNDecisionServiceIntegrationTest extends DMNKieServerBaseIntegrationTest {

    public static final Logger LOG = LoggerFactory.getLogger(DMNDecisionServiceIntegrationTest.class);

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "decision-services", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "decision-services";
    private static final String CONTAINER_1_ALIAS = "ds";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/decision-services").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1, CONTAINER_1_ALIAS);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // no extra classes.
    }
    
    @Test
    public void test_getModels() {
        ServiceResponse<DMNModelInfoList> getModels = dmnClient.getModels(CONTAINER_1_ID);
        KieServerAssert.assertSuccess(getModels);
        
        List<DMNModelInfo> models = getModels.getResult().getModels();
        assertThat(models, hasSize(3));
        
        DMNModelInfo fdModel = models.stream().filter(mi -> mi.getName().equals("Decision Services")).findFirst().orElse(null);
        assertThat(fdModel, notNullValue());
        assertThat(fdModel.getNamespace(), is("http://www.trisotech.com/definitions/_686f58d4-4ec3-4c65-8c06-0e4fd8983def"));
        assertThat(fdModel.getDecisionServices(), hasSize(2));
        DMNDecisionServiceInfo ds1 = fdModel.getDecisionServices().stream().filter(x -> x.getName().equals("A Only Knowing B and C")).findFirst().orElse(null);
        assertThat(ds1, notNullValue());
        assertThat(ds1.getId(), is("_70386614-9838-420b-a2ae-ff901ada63fb"));
        DMNDecisionServiceInfo ds2 = fdModel.getDecisionServices().stream().filter(x -> x.getName().equals("A only as output knowing D and E")).findFirst().orElse(null);
        assertThat(ds1, notNullValue());
        assertThat(ds2.getId(), is("_4620ef13-248a-419e-bc68-6b601b725a03"));
    }

    @Test
    public void test_checkDSwithInputData() {
        DMNContext context = DMNFactory.newContext();
        context.set("D", "d");
        context.set("E", "e");

        ServiceResponse<DMNResult> evaluate = dmnClient.evaluateDecisionService(CONTAINER_1_ID,
                                                                                "http://www.trisotech.com/definitions/_686f58d4-4ec3-4c65-8c06-0e4fd8983def",
                                                                                "Decision Services",
                                                                                "A only as output knowing D and E",
                                                                                context);
        KieServerAssert.assertSuccess(evaluate);

        DMNResult dmnResult = evaluate.getResult();
        LOG.debug("{}", dmnResult);
        dmnResult.getDecisionResults().forEach(x -> LOG.debug("{}", x));
        assertThat(dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("A"), is("de"));
    }

    @Test
    public void test_checkDSwithInputDecision() {
        DMNContext context = DMNFactory.newContext();
        context.set("D", "d");
        context.set("E", "e");

        ServiceResponse<DMNResult> evaluate = dmnClient.evaluateDecisionService(CONTAINER_1_ID,
                                                                                "http://www.trisotech.com/definitions/_686f58d4-4ec3-4c65-8c06-0e4fd8983def",
                                                                                "Decision Services",
                                                                                "A Only Knowing B and C",
                                                                                context);
        KieServerAssert.assertSuccess(evaluate);

        DMNResult dmnResult = evaluate.getResult();
        LOG.debug("{}", dmnResult);
        dmnResult.getDecisionResults().forEach(x -> LOG.debug("{}", x));
        assertThat(dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("A"), nullValue()); // because B and C are not defined in input.
    }

    @Test
    public void test_checkDSwithInputDecision2() {
        DMNContext context = DMNFactory.newContext();
        context.set("D", "d");
        context.set("E", "e");
        context.set("B", "inB");
        context.set("C", "inC");

        ServiceResponse<DMNResult> evaluate = dmnClient.evaluateDecisionService(CONTAINER_1_ID,
                                                                                "http://www.trisotech.com/definitions/_686f58d4-4ec3-4c65-8c06-0e4fd8983def",
                                                                                "Decision Services",
                                                                                "A Only Knowing B and C",
                                                                                context);
        KieServerAssert.assertSuccess(evaluate);

        DMNResult dmnResult = evaluate.getResult();
        LOG.debug("{}", dmnResult);
        dmnResult.getDecisionResults().forEach(x -> LOG.debug("{}", x));
        assertThat(dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("A"), is("inBinC"));
    }

    @Test
    public void testImportDS_testEvaluateAll() {
        // DROOLS-2768 DMN Decision Service encapsulate Decision which imports a Decision Service
        DMNContext context = DMNFactory.newContext();
        context.set("L1 person name", "L1 Import John");

        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID,
                                                                       "http://www.trisotech.com/dmn/definitions/_0ff3708a-c861-4a96-b85c-7b882f18b7a1",
                                                                       "Import Decision Service 20180718",
                                                                       context);
        KieServerAssert.assertSuccess(evaluateAll);

        DMNResult dmnResult = evaluateAll.getResult();
        dmnResult.getDecisionResults().forEach(x -> LOG.debug("{}", x));
        assertThat(dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.get("invoke imported DS"), is("Hello, L1 Import John; you are allowed"));
        assertThat(result.get("Prefixing"), is("Hello, L1 Import John"));
        assertThat(result.get("final Import L1 decision"), is("Hello, L1 Import John the result of invoking the imported DS is: Hello, L1 Import John; you are allowed"));
    }

    @Test
    public void testImportDS_testEvaluateDS() {
        // DROOLS-2768 DMN Decision Service encapsulate Decision which imports a Decision Service
        DMNContext context = DMNFactory.newContext();
        context.set("L1 person name", "L1 Import Evaluate DS NAME");

        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateDecisionService(CONTAINER_1_ID,
                                                                                   "http://www.trisotech.com/dmn/definitions/_0ff3708a-c861-4a96-b85c-7b882f18b7a1",
                                                                                   "Import Decision Service 20180718",
                                                                                   "Import L1 DS",
                                                                                   context);
        KieServerAssert.assertSuccess(evaluateAll);

        DMNResult dmnResult = evaluateAll.getResult();
        dmnResult.getDecisionResults().forEach(x -> LOG.debug("{}", x));
        assertThat(dmnResult.hasErrors(), is(false));

        DMNContext result = dmnResult.getContext();
        assertThat(result.getAll(), not(hasEntry(is("invoke imported DS"), anything()))); // Decision Service will encapsulate this decision
        assertThat(result.getAll(), not(hasEntry(is("Prefixing"), anything()))); // Decision Service will encapsulate this decision
        assertThat(result.get("final Import L1 decision"), is("Hello, L1 Import Evaluate DS NAME the result of invoking the imported DS is: Hello, L1 Import Evaluate DS NAME; you are allowed"));
    }

}
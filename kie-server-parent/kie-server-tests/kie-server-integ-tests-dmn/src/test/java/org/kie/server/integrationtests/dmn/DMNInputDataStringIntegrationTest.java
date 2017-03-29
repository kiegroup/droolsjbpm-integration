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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DMNInputDataStringIntegrationTest
        extends DMNKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "input-data-string",
            "1.0.0.Final" );

    private static final String CONTAINER_ID  = "input-data-string";
    
    private static final String MODEL_NAMESPACE = "https://github.com/kiegroup/kie-dmn/input-data-string";
    private static final String MODEL_NAME = "input-data-string";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/input-data-string" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // no extra classes.
    }

    // See org.kie.dmn.core.DMNInputRuntimeTest
    @Test
    public void test_evaluateAll() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "Full Name", "John Doe" );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_ID, dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        assertThat( dmnResult.getDecisionResults().size(), is( 1 ) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getResult(), is( "Hello John Doe" ) );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Greeting Message" ), is( "Hello John Doe" ) );
    }
    
    // See org.kie.dmn.core.DMNInputRuntimeTest
    @Test
    public void testInputStringEvaluateDecisionByName() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "Full Name", "John Doe" );

        DMNResult dmnResult = dmnClient.evaluateDecisionByName( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "Greeting Message", dmnContext ).getResult();

        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getResult(), is( "Hello John Doe" ) );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Greeting Message" ), is( "Hello John Doe" ) );

        dmnResult = dmnClient.evaluateDecisionByName( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "nonExistantName", dmnContext ).getResult();
        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getEvaluationStatus(), is( DMNDecisionResult.DecisionEvaluationStatus.NOT_EVALUATED ) );

        dmnResult = dmnClient.evaluateDecisionByName( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "", dmnContext ).getResult();
        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getEvaluationStatus(), is( DMNDecisionResult.DecisionEvaluationStatus.NOT_EVALUATED ) );

        // difference with org.kie.dmn.core.DMNInputRuntimeTest:
        try {
            dmnResult = dmnClient.evaluateDecisionByName( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, null, dmnContext ).getResult();
            fail("There is no point in calling evaluateDecisionByName with null parameter");
        } catch ( RuntimeException e ) {
            // Ok.
        }
    }
    
    // See org.kie.dmn.core.DMNInputRuntimeTest
    @Test
    public void testInputStringEvaluateDecisionById() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "Full Name", "John Doe" );

        DMNResult dmnResult = dmnClient.evaluateDecisionById( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "d_GreetingMessage", dmnContext ).getResult();

        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultById( "d_GreetingMessage" ).getResult(), is( "Hello John Doe" ) );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Greeting Message" ), is( "Hello John Doe" ) );

        dmnResult = dmnClient.evaluateDecisionById( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "nonExistantId", dmnContext ).getResult();
        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getEvaluationStatus(), is( DMNDecisionResult.DecisionEvaluationStatus.NOT_EVALUATED ) );

        dmnResult = dmnClient.evaluateDecisionById( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, "", dmnContext ).getResult();
        assertThat( dmnResult.getDecisionResults().size(), is(1) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getEvaluationStatus(), is( DMNDecisionResult.DecisionEvaluationStatus.NOT_EVALUATED ) );

        // difference with org.kie.dmn.core.DMNInputRuntimeTest:
        try {
            dmnResult = dmnClient.evaluateDecisionById( CONTAINER_ID, MODEL_NAMESPACE, MODEL_NAME, null, dmnContext ).getResult();
            fail("There is no point in calling evaluateDecisionById with null parameter");
        } catch ( RuntimeException e ) {
            // Ok.
        }
    }
    
}
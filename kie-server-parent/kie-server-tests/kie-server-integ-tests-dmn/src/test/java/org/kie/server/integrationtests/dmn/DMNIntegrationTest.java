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

import java.math.BigDecimal;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessageType;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class DMNIntegrationTest
        extends DMNKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "function-definition",
            "1.0.0.Final" );

    private static final String CONTAINER_1_ID  = "function-definition";
    private static final String CONTAINER_1_ALIAS  = "functions";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/function-definition");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1, CONTAINER_1_ALIAS);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }

    @Test
    public void test_evaluateAll() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        assertThat( mathInCtx, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
        
        Map<String, Object> dr0 = (Map<String, Object>) dmnResult.getDecisionResultByName("Math").getResult();
        assertThat( dr0, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
    }
    
    // Using explicit namespace and model name
    @Test
    public void test_evaluateAll2() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID,
                "https://www.drools.org/kie-dmn/function-definition", "function-definition",
                dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        assertThat( mathInCtx, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
        
        Map<String, Object> dr0 = (Map<String, Object>) dmnResult.getDecisionResultByName("Math").getResult();
        assertThat( dr0, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
    }
    
    @Test
    public void test_evaluateAll_missingDependency() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        assertThat( mathInCtx, nullValue()  );
        
        assertThat( dmnResult.getMessages().isEmpty(), is(false) );
        
        assertThat( dmnResult.getMessages().stream().anyMatch(m -> m.getMessageType() == DMNMessageType.REQ_NOT_FOUND), is(true) );
    }
    
    @Test
    public void test_evaluateAllWithAlias() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ALIAS, dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        assertThat( mathInCtx, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
        
        Map<String, Object> dr0 = (Map<String, Object>) dmnResult.getDecisionResultByName("Math").getResult();
        assertThat( dr0, hasEntry( "Sum", BigDecimal.valueOf( 15 ) ) );
    }
}

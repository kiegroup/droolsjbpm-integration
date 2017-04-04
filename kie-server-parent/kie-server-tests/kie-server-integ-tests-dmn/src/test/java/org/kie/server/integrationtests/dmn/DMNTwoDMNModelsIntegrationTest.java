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
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DMNTwoDMNModelsIntegrationTest
        extends DMNKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "two-dmn-models",
            "1.0.0.Final" );

    private static final String CONTAINER_1_ID  = "two-dmn-models";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/two-dmn-models" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }
    
    @Test
    public void test_getModels() {
        ServiceResponse<DMNModelInfoList> getModels = dmnClient.getModels(CONTAINER_1_ID);
        
        assertEquals(ResponseType.SUCCESS, getModels.getType());
        
        List<DMNModelInfo> models = getModels.getResult().getModels();
        System.out.println(models);
        assertThat( models, hasSize( 2 ) );
        
        DMNModelInfo fdModel = models.stream().filter(mi -> mi.getName().equals("function-definition")).findFirst().orElse(null);
        assertThat( fdModel, notNullValue() );
        assertThat( fdModel.getNamespace(), is("https://www.drools.org/kie-dmn/function-definition") );
        assertThat( fdModel.getDecisions(), hasSize(1) );
        assertThat( fdModel.getDecisions().iterator().next().getName(), is("Math") );
        
        DMNModelInfo idsModel = models.stream().filter(mi -> mi.getName().equals("input-data-string")).findFirst().orElse(null);
        assertThat( idsModel, notNullValue() );
        assertThat( idsModel.getNamespace(), is("https://github.com/kiegroup/kie-dmn/input-data-string") );
        assertThat( idsModel.getDecisions(), hasSize(1) );
        assertThat( idsModel.getDecisions().iterator().next().getName(), is("Greeting Message") );
    }

    @Test
    public void test_evaluateAllOnInputDataStringModel() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "Full Name", "John Doe" );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, 
                "https://github.com/kiegroup/kie-dmn/input-data-string", "input-data-string",
                dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());
        
        DMNResult dmnResult = evaluateAll.getResult();
        
        assertThat( dmnResult.getDecisionResults().size(), is( 1 ) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getResult(), is( "Hello John Doe" ) );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Greeting Message" ), is( "Hello John Doe" ) );
    }
    
    @Test
    public void test_evaluateAllOnFunctionDefinitionModel() {
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
}
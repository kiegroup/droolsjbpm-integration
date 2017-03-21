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

    private static final String CONTAINER_1_ID  = "input-data-string";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/input-data-string" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }

    @Test
    public void test_evaluateAllDecisions() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "Full Name", "John Doe" );
        ServiceResponse<DMNResult> evaluateAllDecisions = dmnClient.evaluateAllDecisions(CONTAINER_1_ID, dmnContext);
        
        assertEquals(ResponseType.SUCCESS, evaluateAllDecisions.getType());
        
        DMNResult dmnResult = evaluateAllDecisions.getResult();
        
        assertThat( dmnResult.getDecisionResults().size(), is( 1 ) );
        assertThat( dmnResult.getDecisionResultByName( "Greeting Message" ).getResult(), is( "Hello John Doe" ) );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Greeting Message" ), is( "Hello John Doe" ) );
    }

}
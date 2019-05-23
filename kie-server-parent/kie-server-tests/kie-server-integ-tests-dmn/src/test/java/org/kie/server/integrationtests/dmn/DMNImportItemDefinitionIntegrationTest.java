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

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.assertEquals;

public class DMNImportItemDefinitionIntegrationTest
        extends DMNKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "import-itemdef",
            "1.0.0.Final" );
    
    private static final ReleaseId kjar_v101 = new ReleaseId(
            "org.kie.server.testing", "import-itemdef",
            "1.0.1.Final" );

    private static final String CONTAINER_ID = "import-itemdef";
    
    private static final String MODEL_NAMESPACE = "https://kiegroup.org/dmn/_ECD4A4EA-F713-48CF-A7E4-A5AAFFA555DA";
    private static final String MODEL_NAME = "air-conditioning-control";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/import-itemdef-100");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/import-itemdef-101");
    }
    
    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // no extra classes.
    }
    
    @Test
    public void test_kieContainer_upgrade() {
        ServiceResponse<DMNModelInfoList> models = dmnClient.getModels(CONTAINER_ID);
        assertEquals( ResponseType.SUCCESS, models.getType() );
        assertEquals(2, models.getResult().getModels().size());
        
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, kjar_v101));
        
        models = dmnClient.getModels(CONTAINER_ID);
        assertEquals( ResponseType.SUCCESS, models.getType() );
        assertEquals(2, models.getResult().getModels().size());
    }
}

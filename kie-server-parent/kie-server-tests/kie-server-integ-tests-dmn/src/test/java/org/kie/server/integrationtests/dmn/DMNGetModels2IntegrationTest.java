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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNDecisionInfo;
import org.kie.server.api.model.dmn.DMNInputDataInfo;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DMNGetModels2IntegrationTest
        extends DMNKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "get-models2", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "get-models2";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/get-models2");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }

    @Test
    public void test_getModels2() {
        ServiceResponse<DMNModelInfoList> modelInfoListResponse = dmnClient.getModels(CONTAINER_1_ID);

        assertThat(modelInfoListResponse.getType(), is(ResponseType.SUCCESS));

        List<DMNModelInfo> models = modelInfoListResponse.getResult().getModels();

        assertThat(models, hasSize(1));

        DMNModelInfo modelInfo = models.get(0);
        
        assertThat(modelInfo.getName(), is("check"));
        
        Collection<DMNInputDataInfo> inputs = modelInfo.getInputs();
        assertThat(inputs, hasSize(1));
        DMNInputDataInfo input0 = inputs.stream().findFirst().get();
        assertThat(input0.getId(), is("_0633039A-6903-4249-B5FA-A7DD889854BE"));
        assertThat(input0.getName(), is("age"));
        assertThat(input0.getTypeRef(), nullValue());

        Collection<DMNDecisionInfo> decisions = modelInfo.getDecisions();
        assertThat(decisions, hasSize(1));
        DMNDecisionInfo decision0 = decisions.stream().findFirst().get();
        assertThat(decision0.getId(), is("_DC745BB7-A67C-4986-A9CF-390291A72D30"));
        assertThat(decision0.getName(), is("Decision-1"));
    }


}

/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.dmn.pmml;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class DMNPMMLTrustyKieServerBaseIntegrationTest
        extends RestJmsSharedBaseIntegrationTest {

    protected static final String TEST_GROUP = "org.kie.server.testing.dmn.pmml-trusty";
    protected static final String TEST_VERSION = "1.0.0.Final";
    protected static final String DMN_PMML_TRUSTY_PREFIX = "dmn-pmml-trusty-";
    protected static final String COMPILED_SUFFIX = "-compiled";
    protected static final String NOT_COMPILED_SUFFIX = "-not-compiled";
    protected static final String KJAR_SOURCES_PREFIX = "/kjars-sources/";
    protected DMNServicesClient dmnClient;

    public static void setup(String resourceDir,
                             long extendedTimeout,
                             String containerId,
                             ReleaseId releaseId) {
        commandsFactory = KieServices.Factory.get().getCommands();
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource(resourceDir);

        // Having timeout issues due to pmml -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(extendedTimeout);
        ServiceResponse<KieContainerResource> reply = client.createContainer(containerId,
                                                                             new KieContainerResource(containerId,
                                                                                                      releaseId));
        KieServerAssert.assertSuccess(reply);
    }

    protected void execute(String containerId,
                           String pmmlModelNameSpace,
                           String pmmlModelName,
                           String pmmlDecisionName,
                           Object expectedResult,
                           Map<String, Object> inputData) {

        final DMNContext dmnContext = dmnClient.newContext();
        inputData.forEach(dmnContext::set);

        final ServiceResponse<DMNResult> serviceResponse = dmnClient.evaluateDecisionByName(
                containerId,
                pmmlModelNameSpace,
                pmmlModelName,
                pmmlDecisionName,
                dmnContext);

        Assertions.assertThat(serviceResponse.getType()).isEqualTo(KieServiceResponse.ResponseType.SUCCESS);

        final DMNResult dmnResult = serviceResponse.getResult();
        Assertions.assertThat(dmnResult).isNotNull();
        Assertions.assertThat(dmnResult.hasErrors()).isFalse();

        final Object result = dmnResult.getDecisionResultByName(pmmlDecisionName).getResult();
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.dmnClient = kieServicesClient.getServicesClient(DMNServicesClient.class);
    }
}

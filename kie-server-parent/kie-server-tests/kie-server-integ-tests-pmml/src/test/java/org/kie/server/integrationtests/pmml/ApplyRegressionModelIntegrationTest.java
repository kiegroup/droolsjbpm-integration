/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.integrationtests.pmml;

import org.assertj.core.api.Assertions;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ApplyRegressionModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static final ReleaseId releaseId =
            new ReleaseId("org.kie.server.testing", "pmml-trusty-regression", "1.0.0.Final");

    private static final String CONTAINER_ID = "regression";

    private static final long EXTENDED_TIMEOUT = 30000000L;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/pmml-trusty-regression");

        // Having timeout issues due to pmml -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));
        KieServerAssert.assertSuccess(reply);
    }

    @Test
    public void testApplyPmmlRegressionModel() {
        final PMMLRequestData request = new PMMLRequestData("123", "LinReg");
        request.setSource("test_regression.pmml");
        request.addRequestParam("fld1", 12.0);
        request.addRequestParam("fld2", 25.0);
        request.addRequestParam("fld3", "x");

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory)
                .newApplyPmmlModel(request);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);

        Assertions.assertThat(results.getResult()).isNotNull();
        Assertions.assertThat(results.getResult().getValue("results")).isNotNull();

        final PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");

        String resultObjectName = resultHolder.getResultObjectName();
        Assertions.assertThat(resultObjectName).isNotNull();

        Assertions.assertThat(resultObjectName).isEqualTo("fld4");

        final Object obj = resultHolder.getResultValue("fld4", null);
        Assertions.assertThat(obj).isNotNull();

        Assertions.assertThat(obj).isEqualTo(1.0);
//
//        final Double targetValue = resultHolder.getResultValue("fld4", "value", Double.class)
//                .orElse(null);
//        Assertions.assertThat(targetValue).isNotNull();
//        Assertions.assertThat(targetValue).isEqualTo(1.0);
    }
}

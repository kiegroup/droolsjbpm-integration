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

package org.kie.server.integrationtests.drools.pmml;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ApplyPmmlMultipleTimesIntegrationTest extends PMMLApplyModelBaseTest {

    private static final ReleaseId releaseId =
            new ReleaseId("org.kie.server.testing", "pmml-regression", "1.0.0.Final");

    private static final String CONTAINER_ID = "regression";

    private static final long EXTENDED_TIMEOUT = 300000L;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/pmml-regression");

        // Having timeout issues due to pmml -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));
        KieServerAssert.assertSuccess(reply);
    }

    @Test
    public void testMultipleRegressionEvaluation() {
        BigDecimal fld1 = BigDecimal.valueOf(12.0);
        BigDecimal fld2 = BigDecimal.valueOf(25.0);
        String fld3 = "x";
        PMMLRequestData request = new PMMLRequestData("123", "LinReg");
        request.setSource("test_regression.pmml");
        request.addRequestParam("fld1", fld1);
        request.addRequestParam("fld2", fld2);
        request.addRequestParam("fld3", fld3);

        ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory)
                .newApplyPmmlModel(request);
        ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);

        PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");
        Object obj = resultHolder.getResultValue("Fld4", null);
        Assertions.assertThat(obj).isNotNull();

        BigDecimal targetValue = resultHolder.getResultValue("Fld4", "value", BigDecimal.class)
                .orElse(null);
        Assertions.assertThat(targetValue).isNotNull();
        Assertions.assertThat(targetValue).isEqualTo(simpleRegressionResult(fld1, fld2, fld3));

        fld1 = BigDecimal.valueOf(5);
        fld2 = BigDecimal.valueOf(8);
        fld3 = "y";
        request = new PMMLRequestData("123", "LinReg");
        request.addRequestParam("fld1", fld1);
        request.addRequestParam("fld2", fld2);
        request.addRequestParam("fld3", fld3);

        command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory)
                .newApplyPmmlModel(request);
        results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);

        resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");
        obj = resultHolder.getResultValue("Fld4", null);
        Assertions.assertThat(obj).isNotNull();

        targetValue = resultHolder.getResultValue("Fld4", "value", BigDecimal.class)
                .orElse(null);
        Assertions.assertThat(targetValue).isNotNull();
        Assertions.assertThat(targetValue).isEqualTo(simpleRegressionResult(fld1, fld2, fld3));
    }

    private static BigDecimal simpleRegressionResult(BigDecimal fld1, BigDecimal fld2, String fld3) {
        BigDecimal result = fld1.multiply(fld1).multiply(BigDecimal.valueOf(5))
                .add(BigDecimal.valueOf(0.5))
                .add(fld2.multiply(BigDecimal.valueOf(2)))
                .add(fld3Coefficient(fld3))
                .add(fld1.multiply(fld2).multiply(BigDecimal.valueOf(0.4)));
        result = BigDecimal.ONE.divide(BigDecimal.ONE.add(BigDecimal.valueOf(Math.exp(-result.doubleValue()))));
        return result;
    }

    private static BigDecimal fld3Coefficient(String fld3) {
        final Map<String, Double> fld3ValueMap = new HashMap<>();
        fld3ValueMap.put("x", -3.0);
        fld3ValueMap.put("y", 3.0);

        return BigDecimal.valueOf(fld3ValueMap.getOrDefault(fld3, 0.0));
    }
}

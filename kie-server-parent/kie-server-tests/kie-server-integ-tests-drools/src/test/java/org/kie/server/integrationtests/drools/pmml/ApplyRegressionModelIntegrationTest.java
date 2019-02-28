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

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ApplyRegressionModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static final ReleaseId releaseId =
            new ReleaseId("org.kie.server.testing", "pmml-regression", "1.0.0.Final");

    private static final String CONTAINER_ID = "regression";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/pmml-regression");

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testApplyPmmlRegressionModel() {
        Assume.assumeTrue(marshallingFormat != MarshallingFormat.XSTREAM); // RHPAM-1875

        final PMMLRequestData request = new PMMLRequestData("123", "LinReg");
        request.addRequestParam("fld1", 12.0);
        request.addRequestParam("fld2", 25.0);
        request.addRequestParam("fld3", "x");

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory)
                .newApplyPmmlModel(request);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);

        final PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");
        final Object obj = resultHolder.getResultValue("Fld4", null);
        Assertions.assertThat(obj).isNotNull();

        final Double targetValue = resultHolder.getResultValue("Fld4", "value", Double.class)
                .orElse(null);
        Assertions.assertThat(targetValue).isNotNull();
        Assertions.assertThat(targetValue).isEqualTo(1.0);
    }
}

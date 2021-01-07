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

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;

public class ApplyScorecardModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static final ReleaseId RELEASE_ID =
            new ReleaseId("org.kie.server.testing", "pmml-trusty-scorecard", "1.0.0.Final");

    private static final String RESOURCE_DIR = "/kjars-sources/pmml-trusty-scorecard";
    private static final String CORRELATION_ID = "123";
    private static final String CONTAINER_ID = "scorecard";
    private static final String MODEL_NAME = "CompoundPredicateScorecard";
    private static final String FILE_NAME = "CompoundPredicateScorecard.pmml";
    private static final String TARGET_FIELD = "Score";
    private static final Object EXPECTED_RESULT = -93;
    private static final Map<String, Object> INPUT_DATA;
    private static final long EXTENDED_TIMEOUT = 30000000L;

    static {
        INPUT_DATA = new HashMap<>();
        INPUT_DATA.put("input1", -21.5);
        INPUT_DATA.put("input2", -7);
        INPUT_DATA.put("input3", "classA");
        INPUT_DATA.put("input4", "classB");
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() throws Exception {
        setup(RESOURCE_DIR,
              EXTENDED_TIMEOUT,
              CONTAINER_ID,
              RELEASE_ID);
//        KieServerDeployer.buildAndDeployCommonMavenParent();
//        KieServerDeployer.buildAndDeployMavenProjectFromResource(RESOURCE_DIR);
//
//        // Having timeout issues due to pmml -> raised timeout.
//        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
//        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));
//        KieServerAssert.assertSuccess(reply);
    }

    public ApplyScorecardModelIntegrationTest() {
        super(CORRELATION_ID, CONTAINER_ID, MODEL_NAME, FILE_NAME, TARGET_FIELD, EXPECTED_RESULT, INPUT_DATA);

    }

    @Test
    public void testApplyPmmlScorecardModel() {
        execute();
//        final PMMLRequestData request = new PMMLRequestData(CORRELATION_ID, MODEL_NAME);
//        request.setSource(FILE_NAME);
//        INPUT_DATA.forEach(request::addRequestParam);
//
//        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) commandsFactory.newApplyPmmlModel(request);
//        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);
//
//        Assertions.assertThat(results.getResult()).isNotNull();
//        Assertions.assertThat(results.getResult().getValue("results")).isNotNull();
//
//        final PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
//        Assertions.assertThat(resultHolder).isNotNull();
//        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");
//
//        String resultObjectName = resultHolder.getResultObjectName();
//        Assertions.assertThat(resultObjectName).isNotNull().isEqualTo(TARGET_FIELD);
//
//        final Object obj = resultHolder.getResultValue(TARGET_FIELD, null);
//        Assertions.assertThat(obj).isNotNull().isEqualTo(EXPECTED_RESULT);
    }
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.drools.pmml;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test used for applying a PMML model to input data.
 */
public class ApplyScorecardModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static Logger logger = LoggerFactory.getLogger(ApplyScorecardModelIntegrationTest.class);

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "scorecard", "1.0.0.Final");

    private static final String CONTAINER_ID = "scorecard";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/scorecard");

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // unused at this time
    }

    @Test
    public void testApplyPmmlScorecard() {
        PMMLRequestData request = new PMMLRequestData("123", "SimpleScorecard");
        request.addRequestParam("param1", 10.0);
        request.addRequestParam("param2", 15.0);

        ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory).newApplyPmmlModel(request);

        try {
            createRuleServicesClient();
            ServiceResponse<ExecutionResults> results = rulesClient.executeCommandsWithResults(CONTAINER_ID, command);

            assertNotNull(results);
            PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
            assertNotNull(resultHolder);
            assertEquals("OK", resultHolder.getResultCode());
            double score = resultHolder.getResultValue("ScoreCard", "score", Double.class).get();
            Assertions.assertThat(score).isEqualTo(40.8);
            Map<String, Double> rankingMap = (Map<String, Double>) resultHolder.getResultValue("ScoreCard", "ranking");
            Assertions.assertThat(rankingMap.get("reasonCh1")).isEqualTo(5);
            Assertions.assertThat(rankingMap.get("reasonCh2")).isEqualTo(-6);
            logger.info("ApplyScorecardModelIntegrationTest#testApplyPmmlScorecard completed successfully");
        } catch (Exception e) {
            fail("Error occurred during test of ApplyPmmlModelCommand: " + e.getMessage());
            logger.error("Error occurred during test of ApplyPmmlModelCommand", e);
        }

    }
}

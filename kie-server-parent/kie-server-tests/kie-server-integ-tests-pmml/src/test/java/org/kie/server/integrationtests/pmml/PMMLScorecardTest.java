package org.kie.server.integrationtests.pmml;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class PMMLScorecardTest extends PMMLApplyModelBaseTest {

    public static final Logger LOG = LoggerFactory.getLogger(PMMLDecisionTreeTest.class);

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "scorecard-model", "1.0.0.Final");
    private static final String CONTAINER_1_ID = "pmml-container";
    private static final String CONTAINER_1_ALIAS = "pc";

    @BeforeClass
    public static void deployArtifacts() {
        commandsFactory = KieServices.Factory.get().getCommands();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/scorecard");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        createContainer(CONTAINER_1_ID, releaseId, CONTAINER_1_ALIAS);
        assertNotNull(kieContainer);
    }

    @Test
    public void testSimpleScorecard() {
        try {
            createRuleServicesClient();
            assertNotNull(rulesClient);
            PMMLRequestData request = new PMMLRequestData("123", "SimpleScorecard");
            request.addRequestParam("param1", 10.0);
            request.addRequestParam("param2", 15.0);
            ApplyPmmlModelCommand cmd = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory).newApplyPmmlModel(request);
            ServiceResponse<ExecutionResults> results = rulesClient.executeCommandsWithResults(CONTAINER_1_ID, cmd);

            assertNotNull(results);
            PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
            double score = resultHolder.getResultValue("ScoreCard", "score", Double.class).get();
            Assertions.assertThat(score).isEqualTo(40.8);
            Map<String, Double> rankingMap = (Map<String, Double>) resultHolder.getResultValue("ScoreCard", "ranking");
            Assertions.assertThat(rankingMap.get("reasonCh1")).isEqualTo(5);
            Assertions.assertThat(rankingMap.get("reasonCh2")).isEqualTo(-6);

        } catch (Exception e) {
            fail("Error occurred during testing: " + e.getMessage());
        }
    }

}

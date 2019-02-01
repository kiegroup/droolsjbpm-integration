package org.kie.server.integrationtests.pmml;

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

public class PMMLDecisionTreeTest extends PMMLApplyModelBaseTest {

    public static final Logger LOG = LoggerFactory.getLogger(PMMLDecisionTreeTest.class);
    
    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "decision-tree-model", "1.0.0.Final");
    private static final String CONTAINER_1_ID = "pmml-container";
    private static final String CONTAINER_1_ALIAS = "pc";
    
    @BeforeClass
    public static void deployArtifacts() {
        commandsFactory = KieServices.Factory.get().getCommands();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/decision-tree");
        
        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        createContainer(CONTAINER_1_ID, releaseId, CONTAINER_1_ALIAS);
        assertNotNull(kieContainer);
    }

    @Test
    public void testSimpleDecisionTree() {
        try {
            createRuleServicesClient();
            assertNotNull(rulesClient);

            PMMLRequestData request = new PMMLRequestData("123", "TreeTest");
            request.addRequestParam("fld1", 30.0);
            request.addRequestParam("fld2", 60.0);
            request.addRequestParam("fld3", "false");
            request.addRequestParam("fld4", "optA");
            ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory).newApplyPmmlModel(request);
            ServiceResponse<ExecutionResults> results = rulesClient.executeCommandsWithResults(CONTAINER_1_ID, command);

            PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
            assertNotNull(resultHolder);
            assertEquals("OK", resultHolder.getResultCode());
            Object obj = resultHolder.getResultValue("Fld5", null);
            assertNotNull(obj);

            String targetValue = resultHolder.getResultValue("Fld5", "value", String.class).orElse(null);
            assertEquals("tgtY", targetValue);

        } catch (Exception e) {
            fail("Error occurred during testing: " + e.getMessage());
        }
    }
}

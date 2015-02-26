package org.kie.server.integrationtests.shared;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public class RuleFlowIntegrationTest extends KieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "ruleflow-group",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/ruleflow-group").getFile());
    }

    @Test
    public void testExecuteSimpleRuleFlowProcess() {
        assertSuccess(client.createContainer("ruleflow", new KieContainerResource("ruleflow", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <start-process processId=\"simple-ruleflow\"/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("ruleflow", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<string>Rule from first ruleflow group executed</string>\\s*<string>Rule from second ruleflow group executed</string>.*");
    }
}

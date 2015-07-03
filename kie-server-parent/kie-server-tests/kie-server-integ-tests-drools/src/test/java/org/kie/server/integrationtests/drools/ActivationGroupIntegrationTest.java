package org.kie.server.integrationtests.drools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestJmsXstreamSharedBaseIntegrationTest;

public class ActivationGroupIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "activation-group",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/activation-group").getFile());
    }

    /**
     * First rule in activation group "First group" is executed, second rule skipped.
     */
    @Test
    public void testActivationGroup() {
        assertSuccess(client.createContainer("activation", new KieContainerResource("activation", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("activation", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<string>First rule in first activation group executed</string>\\s*<string>Rule without activation group executed</string>.*");
    }

    /**
     * Activation group "First group" is cleared, so it isn't executed.
     */
    @Test
    public void testClearActivationGroup() {
        assertSuccess(client.createContainer("activation", new KieContainerResource("activation", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <clear-activation-group name=\"first-group\"/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("activation", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<list>\\s*<string>Rule without activation group executed</string>\\s*</list>.*");
    }
}

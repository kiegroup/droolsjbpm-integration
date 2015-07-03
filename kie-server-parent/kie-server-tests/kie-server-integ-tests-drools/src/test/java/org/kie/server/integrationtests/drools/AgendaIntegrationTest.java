package org.kie.server.integrationtests.drools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestJmsXstreamSharedBaseIntegrationTest;

public class AgendaIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "agenda-group",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/agenda-group").getFile());
    }

    /**
     * If clear-agenda is used then there are no rules to be executed so command will return empty list.
     */
    @Test
    public void testClearAgenda() {
        assertSuccess(client.createContainer("agenda", new KieContainerResource("agenda", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <clear-agenda/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("agenda", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<list/>.*");
    }
}

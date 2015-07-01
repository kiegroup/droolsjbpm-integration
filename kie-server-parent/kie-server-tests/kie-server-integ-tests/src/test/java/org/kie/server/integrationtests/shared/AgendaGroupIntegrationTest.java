package org.kie.server.integrationtests.shared;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public class AgendaGroupIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "agenda-group",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/agenda-group").getFile());
    }

    /**
     * Rule in agenda group "First agenda" is executed first as it gets focus, then focus is returned to main agenda.
     */
    @Test
    public void testAgendaGroup() {
        assertSuccess(client.createContainer("agenda", new KieContainerResource("agenda", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <set-focus name=\"first-agenda\"/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("agenda", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<string>Rule in first agenda group executed</string>\\s*<string>Rule without agenda group executed</string>.*");
    }

    /**
     * Agenda group "First agenda" is cleared, so it isn't executed.
     */
    @Test
    public void testClearAgendaGroup() {
        assertSuccess(client.createContainer("agenda", new KieContainerResource("agenda", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <set-focus name=\"first-agenda\"/>\n" +
                "  <clear-agenda-group name=\"first-agenda\"/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = client.executeCommands("agenda", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<list>\\s*<string>Rule without agenda group executed</string>\\s*</list>.*");
    }
}

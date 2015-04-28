package org.kie.server.integrationtests.shared;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

/**
 * Test used for verification of spreadsheet decision table processing.
 */
public class SpreadsheetIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "spreadsheet", "1.0.0-SNAPSHOT");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/spreadsheet").getFile());
    }

    @Test
    public void testExecuteSpreadsheetRule() {
        assertSuccess(client.createContainer("spreadsheet", new KieContainerResource("spreadsheet", releaseId)));
        String payload = "<batch-execution>\n" +
            "  <insert out-identifier=\"person\">\n" +
            "    <org.kie.server.testing.Person>\n" +
            "      <age>25</age>\n" +
            "    </org.kie.server.testing.Person>\n" +
            "  </insert>\n" +
            "  <fire-all-rules />\n" +
            "</batch-execution>";
        ServiceResponse<String> response = client.executeCommands("spreadsheet", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsString(result, "<canBuyAlcohol>true</canBuyAlcohol>");
    }
}

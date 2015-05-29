package org.kie.server.integrationtests.drools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestJmsXstreamSharedBaseIntegrationTest;

import static org.junit.Assert.assertTrue;

public class MultiModuleProjectIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

    private static ReleaseId releaseIdRules1 = new ReleaseId("org.kie.server.testing", "multimodule-project-rules1",
            "2.0.0.Final");
    private static ReleaseId releaseIdRules2 = new ReleaseId("org.kie.server.testing", "multimodule-project-rules2",
            "2.0.0.Final");


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        // the parent will build and deploy also all of its modules, so no need to deploy them individually
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/multimodule-project").getFile());
    }

    @Test
    public void testCreateMultipleContainersAndExecuteRules() {
        assertSuccess(client.createContainer("multimodule-rules1", new KieContainerResource("multimodule-rules1", releaseIdRules1)));
        assertSuccess(client.createContainer("multimodule-rules2", new KieContainerResource("multimodule-rules2", releaseIdRules2)));
        String payload1 = "<batch-execution lookup=\"kbase.session\">\n" +
                "   <insert out-identifier=\"car\">" +
                "    <org.kie.server.testing.multimodule.domain.Car/>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> response = client.executeCommands("multimodule-rules1", payload1);
        assertSuccess(response);
        String result1 = response.getResult();
        assertResultContainsString(result1, "<result identifier=\"car\">");
        assertResultContainsString(result1, "<message>Driving car!</message>");

        String payload2 = "<batch-execution lookup=\"kbase.session\">\n" +
                "  <insert out-identifier=\"bus\">" +
                "    <org.kie.server.testing.multimodule.domain.Bus/>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> response2 = client.executeCommands("multimodule-rules2", payload2);
        String result2 = response2.getResult();
        assertSuccess(response2);
        assertResultContainsString(result2, "<result identifier=\"bus\">");
        assertResultContainsString(result2, "<message>Driving bus!</message>");
    }

}

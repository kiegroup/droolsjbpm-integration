package org.kie.server.integrationtests.drools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

import static org.junit.Assert.*;

public class ContainerIsolationIntegrationTest extends RestJmsSharedBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar1",
            "1.0.0.Final");
    private static final ReleaseId kjar2 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar2",
                "1.0.0.Final");

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar1").getFile());
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar2").getFile());
    }

    @Test 
    public void testUseClassWithSameFQNInDifferentContainers() {
        assertSuccess(client.createContainer("container-isolation-kjar1", new KieContainerResource("container-isolation-kjar1", kjar1)));

        String payload1 = "<batch-execution lookup=\"kjar1.session\">\n" +
                "  <insert out-identifier=\"person\">\n" +
                "    <org.kie.server.testing.Person/>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> response1 = client.executeCommands("container-isolation-kjar1", payload1);
        assertSuccess(response1);
        String result1 = response1.getResult();
        assertTrue("Person's id should be 'Person from kjar1'!. Got result: " + result1, result1.contains("<id>Person from kjar1</id>"));

        // now execute the same commands, but for the second container. The rule in there should set different id
        // (namely "Person from kjar2") for the inserted person
        assertSuccess(client.createContainer("container-isolation-kjar2", new KieContainerResource("container-isolation-kjar2", kjar2)));
        String payload2 = "<batch-execution lookup=\"kjar2.session\">\n" +
                "  <insert out-identifier=\"person\">\n" +
                "    <org.kie.server.testing.Person/>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";

        ServiceResponse<String> response2 = client.executeCommands("container-isolation-kjar2", payload2);
        assertSuccess(response2);
        String result2 = response2.getResult();
        assertTrue("Person's id should be 'Person from kjar2'!. Got result: " + result2, result2.contains("<id>Person from kjar2</id>"));
    }

}

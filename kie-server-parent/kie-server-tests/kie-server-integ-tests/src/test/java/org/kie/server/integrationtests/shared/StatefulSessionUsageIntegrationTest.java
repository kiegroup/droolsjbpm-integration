package org.kie.server.integrationtests.shared;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatefulSessionUsageIntegrationTest extends KieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "state-is-kept-for-stateful-session",
            "1.0.0-SNAPSHOT");


    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/state-is-kept-for-stateful-session").getFile());
    }

    @Test
    public void testStateIsKeptBetweenCalls() {
        client.createContainer("stateful-session1", new KieContainerResource("stateful-session1", releaseId));
        String payload1 = "<batch-execution lookup=\"kbase1.stateful\">\n" +
                "  <insert out-identifier=\"person1\">\n" +
                "    <org.kie.server.testing.Person>\n" +
                "      <firstname>Darth</firstname>\n" +
                "    </org.kie.server.testing.Person>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> reply1 = client.executeCommands("stateful-session1", payload1);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person
        String result1 = reply1.getResult();
        assertTrue("Expected surname to be set to 'Vader'. Got response: " + result1,
                result1.contains("<surname>Vader</surname>"));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertTrue("The 'duplicated' field should be false! Got response: " + result1,
                result1.contains("<duplicated>false</duplicated>"));

        // insert second person and fire the rules. The duplicated field will be set to true if there are two
        // persons with name "Darth Vader" the first one is from second call and this call inserts the second one.
        // In case the state of the session was not kept between the calls, the field would not be set
        String payload2 = "<batch-execution lookup=\"kbase1.stateful\">\n" +
                "  <insert out-identifier=\"person2\">\n" +
                "    <org.kie.server.testing.Person>\n" +
                "      <firstname>Darth</firstname>\n" +
                "    </org.kie.server.testing.Person>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> reply2 = client.executeCommands("stateful-session1", payload2);
        String result2 = reply2.getResult();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply2.getType());
        assertTrue("The 'duplicated' field should be true! Got response: " + result2,
                result2.contains("<duplicated>true</duplicated>"));
    }

    @Test
    public void testErrorHandlingWhenContainerIsDisposedBetweenCalls() {
        client.createContainer("stateful-session2", new KieContainerResource("stateful-session2", releaseId));
        String payload = "<batch-execution lookup=\"kbase1.stateful\">\n" +
                "  <insert out-identifier=\"person\">\n" +
                "    <org.kie.server.testing.Person>\n" +
                "      <firstname>Darth</firstname>\n" +
                "    </org.kie.server.testing.Person>\n" +
                "  </insert>\n" +
                "  <fire-all-rules />\n" +
                "</batch-execution>";
        ServiceResponse<String> reply = client.executeCommands("stateful-session2", payload);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        // now dispose the container
        ServiceResponse<Void> disposeReply = client.disposeContainer("stateful-session2");
        assertEquals("Dispose reply response type.", ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
        // and try to call the container again. The call should fail as the container no longer exists
        reply = client.executeCommands("stateful-session2", payload);
        assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
        assertTrue("Expected message about non-instantiated container. Got: " + reply.getMsg(),
                reply.getMsg().contains("Container stateful-session2 is not instantiated"));
    }
}

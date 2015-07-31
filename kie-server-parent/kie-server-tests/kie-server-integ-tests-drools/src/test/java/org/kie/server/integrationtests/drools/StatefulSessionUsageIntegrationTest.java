/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.drools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestJmsXstreamSharedBaseIntegrationTest;

import static org.junit.Assert.*;

public class StatefulSessionUsageIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

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
        ServiceResponse<String> reply1 = ruleClient.executeCommands("stateful-session1", payload1);
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
        ServiceResponse<String> reply2 = ruleClient.executeCommands("stateful-session1", payload2);
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
        ServiceResponse<String> reply = ruleClient.executeCommands("stateful-session2", payload);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        // now dispose the container
        ServiceResponse<Void> disposeReply = client.disposeContainer("stateful-session2");
        assertEquals("Dispose reply response type.", ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
        // and try to call the container again. The call should fail as the container no longer exists
        reply = ruleClient.executeCommands("stateful-session2", payload);
        assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
        assertTrue("Expected message about non-instantiated container. Got: " + reply.getMsg(),
                reply.getMsg().contains("Container stateful-session2 is not instantiated"));
    }

    @Test
    public void testAgendaGroup() {
        client.createContainer("stateful-session1", new KieContainerResource("stateful-session1", releaseId));
        String payload1 = "<batch-execution lookup=\"kbase1.stateful\">\n" +
                          "  <insert out-identifier=\"person1\">\n" +
                          "    <org.kie.server.testing.Person>\n" +
                          "      <firstname>Bob</firstname>\n" +
                          "    </org.kie.server.testing.Person>\n" +
                          "  </insert>\n" +
                          "  <set-focus name=\"ag1\"/>\n" +
                          "  <fire-all-rules />\n" +
                          "  <clear-agenda-group name=\"ag1\"/>\n" + // this is just to test marshalling
                          "  <clear-agenda />\n" + // this is just to test marshalling
                          "</batch-execution>";
        ServiceResponse<String> reply1 = ruleClient.executeCommands("stateful-session1", payload1);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person
        String result1 = reply1.getResult();
        assertTrue("Expected surname to be set to 'Sponge'. Got response: " + result1,
                   result1.contains("<surname>Sponge</surname>"));
    }

}

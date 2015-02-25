/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.integrationtests;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;

public class StatelessSessionUsageIntegrationTest extends KieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar",
            "1.0.0-SNAPSHOT");


    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());
    }

    @Test
    public void testStatelessCall() {
        client.createContainer("stateless-kjar1", new KieContainerResource("stateless-kjar1", releaseId));
        String payload1 = "<batch-execution lookup=\"kbase1.stateless\">\n" +
                "  <insert out-identifier=\"person1\">\n" +
                "    <org.kie.server.testing.Person>\n" +
                "      <firstname>Darth</firstname>\n" +
                "    </org.kie.server.testing.Person>\n" +
                "  </insert>\n" +
                "</batch-execution>";
        ServiceResponse<String> reply1 = client.executeCommands("stateless-kjar1", payload1);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person
        String result1 = reply1.getResult();
        assertTrue("Expected surname to be set to 'Vader'. Got response: " + result1,
                result1.contains("<surname>Vader</surname>"));
    }

}

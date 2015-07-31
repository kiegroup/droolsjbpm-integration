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

/**
 * Test used for verification of spreadsheet decision table processing.
 */
public class SpreadsheetIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

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
        ServiceResponse<String> response = ruleClient.executeCommands("spreadsheet", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsString(result, "<canBuyAlcohol>true</canBuyAlcohol>");
    }
}

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

public class RuleFlowIntegrationTest extends RestJmsXstreamSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "ruleflow-group",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/ruleflow-group").getFile());
    }

    @Test
    public void testExecuteSimpleRuleFlowProcess() {
        assertSuccess(client.createContainer("ruleflow", new KieContainerResource("ruleflow", releaseId)));
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                "    <java.util.ArrayList/>\n" +
                "  </set-global>\n" +
                "  <start-process processId=\"simple-ruleflow\"/>\n" +
                "  <fire-all-rules/>\n" +
                "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                "</batch-execution>\n";
        ServiceResponse<String> response = ruleClient.executeCommands("ruleflow", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                ".*<string>Rule from first ruleflow group executed</string>\\s*<string>Rule from second ruleflow group executed</string>.*");
    }

    @Test
    public void testExecuteSimpleRuleFlowProcessInStatelessSession() {
        assertSuccess(client.createContainer("ruleflow-stateless", new KieContainerResource("ruleflow-stateless", releaseId)));
        String payload = "<batch-execution lookup=\"defaultStatelessKieSession\">\n" +
                         "  <set-global identifier=\"list\" out-identifier=\"output-list\">\n" +
                         "    <java.util.ArrayList/>\n" +
                         "  </set-global>\n" +
                         "  <start-process processId=\"simple-ruleflow\" out-identifier=\"process-out\"/>\n" +
                         "  <fire-all-rules/>\n" +
                         "  <get-global identifier=\"list\" out-identifier=\"output-list\"/>\n" +
                         "</batch-execution>\n";
        ServiceResponse<String> response = ruleClient.executeCommands("ruleflow-stateless", payload);
        assertSuccess(response);
        String result = response.getResult();
        assertResultContainsStringRegex(result,
                                        ".*<string>Rule from first ruleflow group executed</string>\\s*<string>Rule from second ruleflow group executed</string>.*");
    }
}

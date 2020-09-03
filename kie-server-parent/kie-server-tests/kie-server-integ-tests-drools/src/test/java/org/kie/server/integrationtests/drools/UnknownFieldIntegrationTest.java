/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

public class UnknownFieldIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId("org.kie.server.testing", "unknown-field-kjar", "1.0.0");
    private static ReleaseId releaseId2 = new ReleaseId("org.kie.server.testing", "unknown-field-kjar", "2.0.0");


    private static final String CONTAINER_ID = "stateful-session";
    private static final String KIE_SESSION = "kbase1.stateful";
    private static final String FACT_OUT_IDENTIFIER = "fact";
    private static final String FACT_CLASS_NAME = "org.kie.server.testing.MyFact";

    private static ClassLoader secondKjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        System.setProperty(KieServerConstants.XSTREAM_IGNORE_UNKNOWN_ELEMENTS, "true");

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/unknown-field-kjar1");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/unknown-field-kjar2");

        secondKjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId2).getClassLoader();
    }

    @AfterClass
    public static void clearSystemProperty() {
        System.clearProperty(KieServerConstants.XSTREAM_IGNORE_UNKNOWN_ELEMENTS);
    }

    @Before
    public void cleanupContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(FACT_CLASS_NAME, Class.forName(FACT_CLASS_NAME, true, secondKjarClassLoader)); // Use class in kjar2
    }

    @Test
    public void testUnknownField() {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object fact = createInstance(FACT_CLASS_NAME); // Use class in kjar2
        KieServerReflections.setValue(fact, "field1", "value1");
        KieServerReflections.setValue(fact, "field2", "value2"); // this field is unknown to kjar1

        commands.add(commandsFactory.newInsert(fact, FACT_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());
        ServiceResponse<ExecutionResults> reply1 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);

        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());

        ExecutionResults actualData = reply1.getResult();

        Object outFact = actualData.getValue(FACT_OUT_IDENTIFIER);
        assertEquals("OK", KieServerReflections.valueOf(outFact, "field1"));
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.junit.Assert.assertEquals;

public class JsonTypeInfoUpdateIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId100 = new ReleaseId("org.kie.server.testing", "json-type-info-kjar", "1.0.0");
    private static ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "json-type-info-kjar", "1.0.1");

    private static final String CONTAINER_ID = "stateful-session";
    private static final String KIE_SESSION = "kbase1.stateful";
    private static final String FACT_OUT_IDENTIFIER = "fact";
    private static final String FACT_CLASS_NAME = "org.kie.server.testing.Fact1";
    private static final String CONTACT_PARAM_CLASS_NAME = "org.kie.server.testing.params.ContactParam";
    private static final String FACT_PARAMETER_FIELD = "parameter";
    private static final String FACT_RESULT_VALUE_FIELD = "resultValue";
    private static final String CONTACT_PARAM_CONTACTINFO_FIELD = "contactInfo";

    private static ClassLoader kjarClassLoader;

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]{
            {MarshallingFormat.JSON, restConfiguration}
        }));

        if (TestConfig.getRemotingUrl() != null && !TestConfig.skipJMS()) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]{
                {MarshallingFormat.JSON, jmsConfiguration}
            }));
        }

        return parameterData;
    }

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/json-type-info-dependency");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/json-type-info-kjar");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/json-type-info-kjar101");

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId100).getClassLoader();
    }

    @Before
    public void cleanupContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId100);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(FACT_CLASS_NAME, Class.forName(FACT_CLASS_NAME, true, kjarClassLoader));
        extraClasses.put(CONTACT_PARAM_CLASS_NAME, Class.forName(CONTACT_PARAM_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testJsonTypeInfoKjarUpdate() {

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object fact = createInstance(FACT_CLASS_NAME);
        Object param = createInstance(CONTACT_PARAM_CLASS_NAME);
        KieServerReflections.setValue(param, CONTACT_PARAM_CONTACTINFO_FIELD, 123);
        KieServerReflections.setValue(fact, FACT_PARAMETER_FIELD, param);

        commands.add(commandsFactory.newInsert(fact, FACT_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());
        ServiceResponse<ExecutionResults> reply1 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);

        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());

        ExecutionResults actualData = reply1.getResult();

        Object outFact = actualData.getValue(FACT_OUT_IDENTIFIER);
        assertEquals(CONTACT_PARAM_CLASS_NAME, KieServerReflections.valueOf(outFact, FACT_RESULT_VALUE_FIELD));

        Object outParam = KieServerReflections.valueOf(outFact, FACT_PARAMETER_FIELD);
        assertEquals(CONTACT_PARAM_CLASS_NAME, outParam.getClass().getName());

        //------------- update -----------
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));

        List<Command<?>> commands2 = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand2 = commandsFactory.newBatchExecution(commands2, KIE_SESSION);

        Object fact2 = createInstance(FACT_CLASS_NAME);
        Object param2 = createInstance(CONTACT_PARAM_CLASS_NAME);
        KieServerReflections.setValue(param2, CONTACT_PARAM_CONTACTINFO_FIELD, 123);
        KieServerReflections.setValue(fact2, FACT_PARAMETER_FIELD, param2);

        commands2.add(commandsFactory.newInsert(fact2, FACT_OUT_IDENTIFIER));
        commands2.add(commandsFactory.newFireAllRules());
        ServiceResponse<ExecutionResults> reply2 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand2);

        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply2.getType());

        ExecutionResults actualData2 = reply2.getResult();

        Object outFact2 = actualData2.getValue(FACT_OUT_IDENTIFIER);
        assertEquals(CONTACT_PARAM_CLASS_NAME + "-new", KieServerReflections.valueOf(outFact2, FACT_RESULT_VALUE_FIELD));

        Object outParam2 = KieServerReflections.valueOf(outFact2, FACT_PARAMETER_FIELD);
        assertEquals(CONTACT_PARAM_CLASS_NAME, outParam2.getClass().getName());
    }

}

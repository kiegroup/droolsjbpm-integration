/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest.camel.kiecamel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.PojoProxyHelper;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.ExecutableCommand;
import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.camel.kiecamel.proxy.RemoteExecutionService;
import org.kie.karaf.itest.camel.kiecamel.tools.ExecutionServerCommand;
import org.kie.karaf.itest.kieserver.KieServerConstants;
import org.kie.karaf.itest.util.PaxExamWithWireMock;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExamWithWireMock.class)
@ExamReactorStrategy(PerClass.class)
public class KieCamelRemoteIntegrationTest extends AbstractKarafIntegrationTest {

    public static final String HOST = "localhost";
    public static final int PORT = 59400;
    public static final String TYPE = "jaxb";

    private RemoteExecutionService remoteExecutionService;

    @Inject
    private CamelContext camelContext;

    @Before
    public void init() throws Exception {
        remoteExecutionService = getRemoteExecutionService();
    }

    @Test
    public void testListContainers() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("listContainers");
        final Object response = remoteExecutionService.runOnExecServer(executionServerCommand);
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof KieContainerResourceList);

        final KieContainerResourceList kieContainerResourceList = (KieContainerResourceList) response;
        Assert.assertEquals(1, kieContainerResourceList.getContainers().size());
    }

    @Test
    public void testGetProcessDefinition() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", KieServerConstants.containerId);
        parameters.put("processId", KieServerConstants.processId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("getProcessDefinition");
        executionServerCommand.setParameters(parameters);

        final Object response = remoteExecutionService.runOnExecServer(executionServerCommand);
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof ProcessDefinition);

        final ProcessDefinition processDefinition = (ProcessDefinition) response;
        Assert.assertEquals("Evaluation", processDefinition.getName());
        Assert.assertEquals("1", processDefinition.getVersion());
    }

    @Test
    public void testStartProcess() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("containerId", KieServerConstants.containerId);
        parameters.put("processId", KieServerConstants.processId);
        parameters.put("employee", KieServerConstants.user);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("startProcess");
        executionServerCommand.setParameters(parameters);

        final Object response = remoteExecutionService.runOnExecServer(executionServerCommand);
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof Long);

        final Long processInstanceId = (Long) response;
        Assert.assertEquals((Long) 2L, processInstanceId);
    }

    @Test
    public void testExecuteCommand() {
        final InsertObjectCommand insertObjectCommand = new InsertObjectCommand();
        insertObjectCommand.setOutIdentifier("person");
        insertObjectCommand.setObject("john");
        final FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
        final List<ExecutableCommand<?>> commands = new ArrayList<ExecutableCommand<?>>();
        final BatchExecutionCommandImpl executionCommand = new BatchExecutionCommandImpl(commands);
        executionCommand.setLookup("defaultKieSession");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", KieServerConstants.containerId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("rule");
        executionServerCommand.setOperation("executeCommands");
        executionServerCommand.setParameters(parameters);
        executionServerCommand.setBodyParam("cmd");
        executionServerCommand.setBody(executionCommand);

        final Object response = remoteExecutionService.runOnExecServer(executionServerCommand);
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof String);

        final String responseString = (String) response;
        Assert.assertTrue(responseString.contains("execution-results"));
    }

    private RemoteExecutionService getRemoteExecutionService() throws Exception {
        return PojoProxyHelper.createProxy(camelContext.getEndpoint("direct:remote"),
                                           RemoteExecutionService.class);
    }

    @Configuration
    public static Option[] configure() {

        return new Option[]{
                // Install Karaf Container
                AbstractKarafIntegrationTest.getKarafDistributionOption(),

                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
//                  debugConfiguration("5005", true),

                AbstractKarafIntegrationTest.loadKieFeatures("drools-module", "drools-decisiontable", "kie-ci", "kie-aries-blueprint", "kie-camel"),

                // wrap and install junit bundle - the DRL imports a class from it
                // (simulates for instance a bundle with domain classes used in rules)
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject())
        };
    }
}

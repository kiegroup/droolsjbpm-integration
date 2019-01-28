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

package org.kie.camel.container.integration.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.runtime.help.impl.BatchExecutionHelperProviderImpl;
import org.drools.core.util.FileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.command.ExecutableCommand;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.camel.container.api.model.Person;
import org.kie.scanner.KieMavenRepository;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.ProcessInstance;

import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class RemoteIntegrationTest extends AbstractKieCamelIntegrationTest {

    private static final String KJAR_RESOURCES_PATH = "/org/kie/camel/container/integration/tests/kjar/";
    private static final String KJAR_TEST_PACKAGE_PATH = "/src/main/resources/org/test/";
    private static final String PATH_POM = KJAR_RESOURCES_PATH + "pom.xml";
    private static final String PROCESS_FILE_NAME = "process1.bpmn2";
    private static final String PROCESS_WITH_SIGNAL_FILE_NAME = "processWithSignal.bpmn2";
    private static final String RULES_FILE_NAME = "rules.drl";
    private static final ReleaseId RELEASE_ID =
            new ReleaseId("org.drools", "camel-container-tests-kjar", "1.0.0");
    private static final String CONTAINER_ID = "test-container";
    private static final String PROCESS_ID = "process1";
    private static final String PROCESS_WITH_SIGNAL_ID = "processWithSignal";
    private static final String SIGNAL_NAME = "signal1";
    private static final String INITIATOR = "yoda";

    private static final String SIMPLE_QUERY_NAME = "process-instances-query";
    private static final String SIMPLE_QUERY_EXPRESSION = "select * from ProcessInstanceLog";
    private static final String SIMPLE_QUERY_TARGET = "PROCESS";
    private static final String SIMPLE_QUERY_DATASOURCE = "java:jboss/datasources/ExampleDS";

    @BeforeClass
    public static void createKieJar() throws IOException {
        KieServices ks = KieServices.get();
        KieFileSystem kfs = createKieFileSystemWithKProject(ks);
        kfs.writePomXML(loadResource(PATH_POM));
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + PROCESS_FILE_NAME,
                                         KJAR_TEST_PACKAGE_PATH + PROCESS_FILE_NAME, kfs);
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + PROCESS_WITH_SIGNAL_FILE_NAME,
                                         KJAR_TEST_PACKAGE_PATH + PROCESS_WITH_SIGNAL_FILE_NAME, kfs);
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + RULES_FILE_NAME,
                                         KJAR_TEST_PACKAGE_PATH + RULES_FILE_NAME, kfs);

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        List<Message> messageList = kieBuilder.buildAll().getResults().getMessages();
        Assertions.assertThat(messageList).isEmpty();
        InternalKieModule kJar1 = (InternalKieModule) kieBuilder.getKieModule();

        KieMavenRepository repository = getKieMavenRepository();
        repository.installArtifact(RELEASE_ID, kJar1, createKPom());
    }

    @Before
    public void deploy() throws IOException {
        final KieContainerResource kieContainerResource = new KieContainerResource(RELEASE_ID);
        kieContainerResource.setContainerId(CONTAINER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("createContainer");
        executionServerCommand.addParameter("id", CONTAINER_ID);
        executionServerCommand.setBodyParam("resource");
        executionServerCommand.setBody(kieContainerResource);
        runOnExecutionServer(executionServerCommand);
    }

    @After
    public void teardown() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("disposeContainer");
        executionServerCommand.addParameter("id", CONTAINER_ID);
        runOnExecutionServer(executionServerCommand);
    }

    @Test
    public void listContainerTest() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("listContainers");
        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(KieContainerResourceList.class);

        final KieContainerResourceList kieContainerResourceList = (KieContainerResourceList) response;
        Assertions.assertThat(kieContainerResourceList.getContainers()).hasSize(1);
        Assertions.assertThat(kieContainerResourceList.getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
    }

    @Test
    public void testFindProcessByContainerIdProcessId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessByContainerIdProcessId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(ProcessDefinition.class);
        final ProcessDefinition processDefinition = (ProcessDefinition) response;
        Assertions.assertThat(processDefinition.getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(processDefinition.getId()).isEqualTo(PROCESS_ID);
    }

    @Test
    public void testFindProcesses() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcesses");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcessesByContainerId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesByContainerId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcessesByContainerIdWrongId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", "wrong-container-id");
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesByContainerId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isEmpty();
    }

    @Test
    public void testFindProcessesById() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesById");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcessInstances() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstances");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstancesByContainerId() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        parameters.put("status", statuses);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByContainerId");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstancesByStatus() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", statuses);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByStatus");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstanceByInitiator() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("initiator", INITIATOR);
        parameters.put("status", statuses);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByInitiator");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testListQueries() {
        registerQuery(getSimpleQueryDefinition());

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", 0);
        parameters.put("pageSize", 10);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("getQueries");
        executionServerCommand.setParameters(parameters);

        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        List<QueryDefinition> queryDefinitionList = (List<QueryDefinition>) response;
        Assertions.assertThat(queryDefinitionList).isNotEmpty();
        List<String> queryNames = queryDefinitionList.stream().map(q -> q.getName()).collect(Collectors.toList());
        Assertions.assertThat(queryNames).contains(SIMPLE_QUERY_NAME);

        unregisterQuery(SIMPLE_QUERY_NAME);
    }

    @Test
    public void testGetProcessDefinition() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("getProcessDefinition");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(ProcessDefinition.class);

        final ProcessDefinition processDefinition = (ProcessDefinition) response;
        Assertions.assertThat(processDefinition.getName()).isEqualTo(PROCESS_ID);
        Assertions.assertThat(processDefinition.getVersion()).isEqualTo("1.0");
    }

    @Test
    public void testStartProcess() {
        startProcess(CONTAINER_ID, PROCESS_ID);
    }

    @Test
    public void testExecuteCommand() {
        final Person person = new Person();
        person.setName("John");
        person.setAge(25);
        final InsertObjectCommand insertObjectCommand = new InsertObjectCommand();
        insertObjectCommand.setOutIdentifier("person");
        insertObjectCommand.setObject(person);
        final FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
        final List<ExecutableCommand<?>> commands = new ArrayList<ExecutableCommand<?>>();
        final BatchExecutionCommandImpl executionCommand = new BatchExecutionCommandImpl(commands);
        executionCommand.setLookup("defaultKieSession");
        executionCommand.addCommand(insertObjectCommand);
        executionCommand.addCommand(fireAllRulesCommand);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", CONTAINER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("rule");
        executionServerCommand.setOperation("executeCommands");
        executionServerCommand.setParameters(parameters);
        executionServerCommand.setBodyParam("cmd");
        executionServerCommand.setBody(executionCommand);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(String.class);

        final String responseString = (String) response;
        Assertions.assertThat(responseString).contains("execution-results");
    }

    private Object runOnExecutionServer(ExecutionServerCommand executionServerCommand) {
        final XStreamMarshaller marshaller = new XStreamMarshaller(new HashSet<>(),
                                                                RemoteIntegrationTest.class.getClassLoader());
        final String commandXML = marshaller.marshall(executionServerCommand);
        final String resultString = kieCamelTestService.runOnExecServer(commandXML);
        final Object result = marshaller.unmarshall(resultString, Object.class);

        return result;
    }

    private Long startProcess(final String containerId, final String processId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", containerId);
        parameters.put("processId", processId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("startProcess");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(Long.class);

        final Long processInstanceId = (Long) response;
        Assertions.assertThat(processInstanceId).isGreaterThan(0);

        return processInstanceId;
    }

    private void sendSignalToProcessInstance(final String containerId, final Long processInstanceId,
                                             final String signalName) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", containerId);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("signalName", signalName);
        parameters.put("event", null);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("signalProcessInstance");
        executionServerCommand.setParameters(parameters);

        runOnExecutionServer(executionServerCommand);
    }

    private QueryDefinition registerQuery(final QueryDefinition queryDefinition) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("queryDefinition", queryDefinition);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("registerQuery");
        executionServerCommand.setParameters(parameters);

        Object object = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(QueryDefinition.class);
        QueryDefinition response = (QueryDefinition) object;

        return response;
    }

    private void unregisterQuery(final String queryName) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("queryName", queryName);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("unregisterQuery");
        executionServerCommand.setParameters(parameters);

        runOnExecutionServer(executionServerCommand);
    }

    private static final QueryDefinition getSimpleQueryDefinition() {
        final QueryDefinition simpleQueryDefinition = new QueryDefinition();
        simpleQueryDefinition.setName(SIMPLE_QUERY_NAME);
        simpleQueryDefinition.setExpression(SIMPLE_QUERY_EXPRESSION);
        simpleQueryDefinition.setTarget(SIMPLE_QUERY_TARGET);
        simpleQueryDefinition.setSource(SIMPLE_QUERY_DATASOURCE);

        return simpleQueryDefinition;
    }

    private static KieFileSystem createKieFileSystemWithKProject(KieServices ks) {
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }

    private static File createKPom() throws IOException {
        FileManager fileManager = new FileManager();
        fileManager.setUp();
        File pomFile = fileManager.newFile("pom.xml");
        fileManager.write(pomFile, loadResource(PATH_POM));
        return pomFile;
    }

    private static KieFileSystem addClasspathResourceToKjar(String classpathPath, String kjarPath, KieFileSystem kieFileSystem) {
        final String resourceContent = loadResource(classpathPath);
        return kieFileSystem.write(kjarPath, resourceContent);
    }

    private static String loadResource(String path) {
        try (InputStream inputStream = RemoteIntegrationTest.class.getResourceAsStream(path)) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading resource from classpath", e);
        }
    }
}

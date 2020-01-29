/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.container.integration.tests.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.DrlProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.FileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.camel.container.integration.tests.AbstractKieCamelIntegrationTest;
import org.kie.scanner.KieMavenRepository;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.VariableInstance;

import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class AbstractRemoteIntegrationTest extends AbstractKieCamelIntegrationTest {

    protected static final String KJAR_RESOURCES_PATH = "/org/kie/camel/container/integration/tests/kjar/";
    protected static final String KJAR_TEST_PACKAGE_PATH = "/src/main/resources/org/test/";
    protected static final String PATH_POM = KJAR_RESOURCES_PATH + "pom.xml";
    protected static final String PROCESS_FILE_NAME = "process1.bpmn2";
    protected static final String PROCESS_WITH_SIGNAL_FILE_NAME = "processWithSignal.bpmn2";
    protected static final String PROCESS_WITH_HUMAN_TASK_FILE_NAME = "processWithHumanTask.bpmn2";
    protected static final String RULES_FILE_NAME = "rules.drl";
    protected static final String CLOUD_BALANCE_SOLVER_CONFIG = "cloudbalance-solver.xml";
    protected static final String CLOUD_BALANCE_SCORE_RULES = "cloudBalancingScoreRules.drl";
    protected static final String DMN_FUNCTION_DEFINITION = "FunctionDefinition.dmn";
    protected static final ReleaseId RELEASE_ID =
            new ReleaseId("org.drools", "camel-container-tests-kjar", "1.0.0");
    protected static final String CONTAINER_ID = "test-container";
    protected static final String PROCESS_ID = "process1";
    protected static final String PROCESS_WITH_SIGNAL_ID = "processWithSignal";
    protected static final String PROCESS_WITH_HUMAN_TASK = "processWithHumanTask";
    protected static final String SIGNAL_NAME = "signal1";
    protected static final String PROCESS_VARIABLE_NAME = "var1";
    protected static final String INITIATOR = "yoda";

    protected static final String SIMPLE_QUERY_NAME = "process-instances-query";
    protected static final String SIMPLE_QUERY_EXPRESSION = "select * from ProcessInstanceLog";
    protected static final String SIMPLE_QUERY_TARGET = "PROCESS";
    protected static final String SIMPLE_QUERY_DATASOURCE = "java:jboss/datasources/ExampleDS";

    protected static final String DEFAULT_USER = "yoda";

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
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + PROCESS_WITH_HUMAN_TASK_FILE_NAME,
                                         KJAR_TEST_PACKAGE_PATH + PROCESS_WITH_HUMAN_TASK_FILE_NAME, kfs);
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + CLOUD_BALANCE_SOLVER_CONFIG,
                                         KJAR_TEST_PACKAGE_PATH + CLOUD_BALANCE_SOLVER_CONFIG, kfs);
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + CLOUD_BALANCE_SCORE_RULES,
                                         KJAR_TEST_PACKAGE_PATH + CLOUD_BALANCE_SCORE_RULES, kfs);
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + DMN_FUNCTION_DEFINITION,
                                         KJAR_TEST_PACKAGE_PATH + DMN_FUNCTION_DEFINITION, kfs);

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        List<Message> messageList = kieBuilder.buildAll(DrlProject.class).getResults().getMessages();
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

    protected Object runOnExecutionServer(ExecutionServerCommand executionServerCommand) {
        final XStreamMarshaller marshaller = new XStreamMarshaller(new HashSet<>(),
                                                                   AbstractRemoteIntegrationTest.class.getClassLoader());
        final String commandXML = marshaller.marshall(executionServerCommand);
        final String resultString = kieCamelTestService.runOnExecServer(commandXML);
        final Object result = marshaller.unmarshall(resultString, Object.class);

        return result;
    }

    protected Long startProcess(final String containerId, final String processId) {
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

    protected Long startProcess(final String containerId, final String processId,
                                final Map<String, Object> processVariables) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", containerId);
        parameters.put("processId", processId);
        parameters.put("variables", processVariables);
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

    protected HashMap<String, String> getProcessVariables(String containerId, long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", containerId);
        parameters.put("processInstanceId", processInstanceId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("findVariablesCurrentState");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);

        final List<VariableInstance> variableInstances = (List<VariableInstance>) response;
        final HashMap<String, String> variablesMap = new HashMap<>();
        for (VariableInstance variable : variableInstances) {
            variablesMap.put(variable.getVariableName(), variable.getValue());
        }

        return variablesMap;
    }

    protected List<ProcessInstance> findActiveProcesses() {
        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", statuses);
        parameters.put("page", "0");
        parameters.put("pageSize", "100");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByStatus");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;

        return processInstances;
    }

    protected void abortProcess(final String containerId, final String processInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", containerId);
        parameters.put("processInstanceId", processInstanceId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("abortProcessInstance");
        executionServerCommand.setParameters(parameters);
        runOnExecutionServer(executionServerCommand);
    }

    protected void sendSignalToProcessInstance(final String containerId, final Long processInstanceId,
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

    @After
    public void abortAllProcesses() {
        final List<ProcessInstance> activeProcesses = findActiveProcesses();
        for (ProcessInstance processInstance : activeProcesses) {
            abortProcess(CONTAINER_ID, String.valueOf(processInstance.getId()));
        }
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
        try (InputStream inputStream = AbstractRemoteIntegrationTest.class.getResourceAsStream(path)) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading resource from classpath", e);
        }
    }
}

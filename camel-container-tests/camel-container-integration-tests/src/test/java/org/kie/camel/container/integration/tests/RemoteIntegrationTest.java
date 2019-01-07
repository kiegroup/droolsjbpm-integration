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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
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
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;

import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class RemoteIntegrationTest extends AbstractKieCamelIntegrationTest {

    private static final String KJAR_RESOURCES_PATH = "/org/kie/camel/container/integration/tests/kjar/";
    private static final String KJAR_TEST_PACKAGE_PATH = "/src/main/resources/org/test/";
    private static final String PATH_POM = KJAR_RESOURCES_PATH + "pom.xml";
    private static final String PROCESS_FILE_NAME = "process1.bpmn2";
    private static final String RULES_FILE_NAME = "rules.drl";
    private static final ReleaseId RELEASE_ID =
            new ReleaseId("org.drools", "camel-container-tests-kjar", "1.0.0");
    private static final String CONTAINER_ID = "test-container";
    private static final String PROCESS_ID = "process1";

    @BeforeClass
    public static void createKieJar() throws IOException {
        KieServices ks = KieServices.get();
        KieFileSystem kfs = createKieFileSystemWithKProject(ks);
        kfs.writePomXML(loadResource(PATH_POM));
        kfs = addClasspathResourceToKjar(KJAR_RESOURCES_PATH + PROCESS_FILE_NAME,
                                         KJAR_TEST_PACKAGE_PATH + PROCESS_FILE_NAME, kfs);
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
    public void testGetProcessDefinition() {
        final Map<String, String> parameters = new HashMap<>();
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
        Assertions.assertThat(processDefinition.getVersion()).isEqualTo("1");
    }

    @Test
    public void testStartProcess() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("process");
        executionServerCommand.setOperation("startProcess");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(Long.class);

        final Long processInstanceId = (Long) response;
        Assertions.assertThat(processInstanceId).isGreaterThan(0);
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

        Map<String, String> parameters = new HashMap<>();
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
        final BatchExecutionHelperProviderImpl batchExecutionHelperProvider = new BatchExecutionHelperProviderImpl();
        final XStream xstreamMarshaller = batchExecutionHelperProvider.newJSonMarshaller();
        final String commandJSON = xstreamMarshaller.toXML(executionServerCommand);
        final String resultString = kieCamelTestService.runOnExecServer(commandJSON);
        final Object result = xstreamMarshaller.fromXML(resultString);

        return result;
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

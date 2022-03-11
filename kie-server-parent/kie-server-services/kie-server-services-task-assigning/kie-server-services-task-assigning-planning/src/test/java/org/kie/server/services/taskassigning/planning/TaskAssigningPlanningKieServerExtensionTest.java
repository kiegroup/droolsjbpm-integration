/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.core.impl.InternalKieContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.optaplanner.core.api.solver.Solver;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.drools.core.util.KeyStoreConstants.PROP_PWD_KS_PWD;
import static org.drools.core.util.KeyStoreConstants.PROP_PWD_KS_URL;
import static org.junit.Assert.fail;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_URL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_NAME;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.CAPABILITY_TASK_ASSIGNING_PLANNING;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.EXTENSION_NAME;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.EXTENSION_START_ORDER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelperTest.clearTaskAssigningServiceProperties;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.ACTIVATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.CREATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.HEALTH_CHECK_IS_ALIVE_MESSAGE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_INTEGER_VALUE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_CONTAINER_NOT_AVAILABLE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.SOLVER_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.TASK_ASSIGNING_SERVICE_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.UNDESIRED_EXTENSIONS_RUNNING_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_CONTAINER_NOT_AVAILABLE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_SERVICE_NOT_FOUND;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_SERVICE_START_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.addExtensionMessagePrefix;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningPlanningKieServerExtensionTest {

    private static final String RUNTIME_URL = "http://testserver:8080/kie-server/services/rest/server";
    private static final String RUNTIME_USER = "RUNTIME_USER";
    private static final String RUNTIME_PWD = "RUNTIME_PWD";
    private static final String RUNTIME_TIMEOUT = "1234";

    private static final String KEY_STORE_RESOURCE = "taskAssigningKeystore.jceks";
    /**
     * was generated with the KeyStoreUtil class.
     */
    private static final String KEY_STORE_RESOURCE_IBM = "taskAssigningKeystoreIBM.jceks";

    private static final String KEY_STORE_PASSWORD = "jBPMKeyStorePassword";
    private static final String RUNTIME_ALIAS = "jBPMAlias";
    private static final String RUNTIME_ALIAS_PWD = "jBPMKeyPassword";
    private static final String RUNTIME_ALIAS_STORED_PWD = "kieserver1!";

    private static final String SOLVER_MOVE_THREAD_COUNT = "AUTO";
    private static final int SOLVER_MOVE_THREAD_BUFFER_SIZE = 2;
    private static final String SOLVER_THREAD_FACTORY_CLASS = "SOLVER_THREAD_FACTORY_CLASS";

    private static final String SOLVER_CONTAINER_ID = "SOLVER_CONTAINER_ID";
    private static final String SOLVER_CONTAINER_GROUP_ID = "SOLVER_CONTAINER_GROUP_ID";
    private static final String SOLVER_CONTAINER_ARTIFACT_ID = "SOLVER_CONTAINER_ARTIFACT_ID";
    private static final String SOLVER_CONTAINER_VERSION = "SOLVER_CONTAINER_VERSION";

    private static final String USER_SYSTEM_NAME = "USER_SYSTEM_NAME";
    private static final String USER_SYSTEM_CONTAINER_ID = "USER_SYSTEM_CONTAINER_ID";
    private static final String USER_SYSTEM_CONTAINER_GROUP_ID = "USER_SYSTEM_CONTAINER_GROUP_ID";
    private static final String USER_SYSTEM_CONTAINER_ARTIFACT_ID = "USER_SYSTEM_CONTAINER_ARTIFACT_ID";
    private static final String USER_SYSTEM_CONTAINER_VERSION = "USER_SYSTEM_CONTAINER_VERSION";

    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    @Mock
    private KieServerImpl kieServer;

    @Mock
    private KieServerRegistry registry;

    private TaskAssigningPlanningKieServerExtension extension;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @Mock
    private UserSystemService userSystemService;

    @Mock
    private Solver<TaskAssigningSolution> solver;

    @Captor
    private ArgumentCaptor<SolverDef> solverDefCaptor;

    @Mock
    private KieContainerInstanceImpl solverContainer;

    @Mock
    private InternalKieContainer internalSolverKieContainer;

    private ClassLoader internalSolverKieContainerClassLoader;

    @Mock
    private KieContainerInstanceImpl userSystemContainer;

    @Mock
    private InternalKieContainer internalUserSystemKieContainer;

    private ClassLoader internalUserSystemKieContainerClassLoader;

    @Mock
    private ServiceResponse<KieContainerResource> containerResponse;

    @Mock
    private TaskAssigningService taskAssigningService;

    @Captor
    private ArgumentCaptor<TaskAssigningServiceConfig> taskAssigningServiceConfigCaptor;

    @Before
    public void setUp() {
        internalUserSystemKieContainerClassLoader = getClass().getClassLoader();
        internalSolverKieContainerClassLoader = getClass().getClassLoader();
        extension = spy(new TaskAssigningPlanningKieServerExtension());
        prepareTaskAssigningServiceProperties();
        doReturn(taskAssigningService).when(extension).createTaskAssigningService(any());
    }

    @After
    public void cleanUp() {
        System.clearProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED);

        System.clearProperty(TASK_ASSIGNING_PROCESS_RUNTIME_URL);
        System.clearProperty(TASK_ASSIGNING_PROCESS_RUNTIME_USER);
        System.clearProperty(TASK_ASSIGNING_PROCESS_RUNTIME_PWD);
        System.clearProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT);

        clearTaskAssigningServiceProperties();

        System.clearProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT);
        System.clearProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE);
        System.clearProperty(TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS);

        System.clearProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID);
        System.clearProperty(TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID);
        System.clearProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID);
        System.clearProperty(TASK_ASSIGNING_SOLVER_CONTAINER_VERSION);
        System.clearProperty(TASK_ASSIGNING_USER_SYSTEM_NAME);

        System.clearProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID);
        System.clearProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID);
        System.clearProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID);
        System.clearProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION);
    }

    @Test
    public void isActiveDefaultValue() {
        assertThat(extension.isActive()).isFalse();
    }

    @Test
    public void isActiveTrue() {
        enableExtension();
        assertThat(extension.isActive()).isTrue();
    }

    @Test
    public void isActiveFalse() {
        disableExtension();
        assertThat(extension.isActive()).isFalse();
    }

    @Test
    public void initWithJbpmExtensionEnabled() {
        JbpmKieServerExtension jbpmExtension = mock(JbpmKieServerExtension.class);
        when(registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME)).thenReturn(jbpmExtension);
        enableExtension();
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        extension.init(kieServer, registry);
        assertKieServerMessageWasAdded(Severity.WARN,
                                       addExtensionMessagePrefix(String.format(UNDESIRED_EXTENSIONS_RUNNING_ERROR, JbpmKieServerExtension.EXTENSION_NAME)),
                                       1,
                                       0,
                                       false);
    }

    @Test
    public void initRuntimeClient() {
        prepareInitRuntimeClient();
        enableExtension();
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        extension.init(kieServer, registry);
        verify(extension).createRuntimeClient(RUNTIME_URL, RUNTIME_USER, RUNTIME_PWD, Long.parseLong(RUNTIME_TIMEOUT));
    }

    @Test
    public void initRuntimeClientWithKeyStore() throws URISyntaxException {
        initRuntimeClientWithKeyStore(RUNTIME_ALIAS, RUNTIME_ALIAS_PWD, RUNTIME_ALIAS_STORED_PWD);
    }

    @Test
    public void initRuntimeClientWithKeyStoreMissingAlias() throws URISyntaxException {
        initRuntimeClientWithKeyStore("whateverAlias", RUNTIME_ALIAS_PWD, RUNTIME_PWD);
    }

    @Test
    public void initRuntimeClientWithKeyStoreWrongAliasPwd() throws URISyntaxException {
        initRuntimeClientWithKeyStore(RUNTIME_ALIAS, "whateverPassword", RUNTIME_PWD);
    }

    private void initRuntimeClientWithKeyStore(String alias, String aliasPwd, String expectedPwd) throws URISyntaxException {
        prepareInitRuntimeClient();
        String currentKeyStore = getCurrentKeyStore();
        URL keyStoreResourceURL = getClass().getClassLoader().getResource(currentKeyStore);
        if (keyStoreResourceURL == null) {
            fail(currentKeyStore + " was not found");
        } else {
            System.out.println("Current key store loaded: " + currentKeyStore);
            System.out.println("keyStoreResourceURL = " + keyStoreResourceURL.toURI());
            System.setProperty(PROP_PWD_KS_URL, keyStoreResourceURL.toURI().toString());
            System.setProperty(PROP_PWD_KS_PWD, KEY_STORE_PASSWORD);
            System.setProperty(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS, alias);
            System.setProperty(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD, aliasPwd);
            enableExtension();
            System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
            System.getProperties().list(System.out);
            extension.init(kieServer, registry);
            verify(extension).createRuntimeClient(RUNTIME_URL, RUNTIME_USER, expectedPwd, Long.parseLong(RUNTIME_TIMEOUT));
        }
    }

    @Test
    public void initWithSolverContainerConfigurationError() {
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID, SOLVER_CONTAINER_ID);
        enableExtension();
        String error = String.format(SOLVER_CONFIGURATION_ERROR, String.format(REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING, SOLVER_CONTAINER_ID, null, null, null));
        assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertThat(extension.isInitialized()).isFalse();
    }

    @Test
    public void initWithSolverContainer() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        prepareSolverContainerProperties();
        enableExtension();
        extension.init(kieServer, registry);
        assertThat(extension.isInitialized()).isTrue();
    }

    @Test
    public void initWithUserSystemMissingError() {
        enableExtension();
        String error = String.format(USER_SYSTEM_CONFIGURATION_ERROR, String.format(USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR, TASK_ASSIGNING_USER_SYSTEM_NAME));
        assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertThat(extension.isInitialized()).isFalse();
    }

    @Test
    public void initWithUserSystemContainerError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, USER_SYSTEM_CONTAINER_ID);
        enableExtension();
        String error = String.format(USER_SYSTEM_CONFIGURATION_ERROR, String.format(REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING, USER_SYSTEM_CONTAINER_ID, null, null, null));
        assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertThat(extension.isInitialized()).isFalse();
    }

    @Test
    public void initWithUserContainer() {
        prepareUserContainerProperties();
        enableExtension();
        extension.init(kieServer, registry);
        assertThat(extension.isInitialized()).isTrue();
    }

    @Test
    public void initWithTaskAssigningServiceConfigErrors() {
        System.setProperty(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, "make failure");
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        String error = String.format(TASK_ASSIGNING_SERVICE_CONFIGURATION_ERROR, String.format(PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_INTEGER_VALUE_ERROR,
                                                                                               TASK_ASSIGNING_PUBLISH_WINDOW_SIZE));
        assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessageStartingWith(error);
        assertThat(extension.isInitialized()).isFalse();
    }

    @Test
    public void getExtensionName() {
        assertThat(extension.getExtensionName()).isEqualTo(EXTENSION_NAME);
    }

    @Test
    public void getServices() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        List<Object> services = extension.getServices();
        assertThat(services.size()).isEqualTo(1);
        assertThat(services.get(0)).isInstanceOf(TaskAssigningService.class);
    }

    @Test
    public void getImplementedCapability() {
        assertThat(extension.getImplementedCapability()).isEqualTo(CAPABILITY_TASK_ASSIGNING_PLANNING);
    }

    @Test
    public void getAppComponents() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        assertThat(extension.getAppComponents(TaskAssigningService.class)).isNotNull();
    }

    @Test
    public void getStartOrder() {
        assertThat(extension.getStartOrder()).isEqualTo(EXTENSION_START_ORDER);
    }

    @Test
    public void toStringTest() {
        String expectedValue = EXTENSION_NAME + " KIE Server extension";
        assertThat(extension).hasToString(expectedValue);
    }

    @Test
    public void serverStartedSuccessful() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        System.setProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT, SOLVER_MOVE_THREAD_COUNT);
        System.setProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE, Integer.toString(SOLVER_MOVE_THREAD_BUFFER_SIZE));
        System.setProperty(TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS, SOLVER_THREAD_FACTORY_CLASS);

        enableExtension();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doReturn(solver).when(extension).createSolver(eq(registry), any());

        initAndStartServerSuccessful();
        verify(extension).createSolver(eq(registry), solverDefCaptor.capture());
        assertThat(solverDefCaptor.getValue().getMoveThreadCount()).isEqualTo(SOLVER_MOVE_THREAD_COUNT);
        assertThat(solverDefCaptor.getValue().getMoveThreadBufferSize()).isEqualTo(SOLVER_MOVE_THREAD_BUFFER_SIZE);
        assertThat(solverDefCaptor.getValue().getThreadFactoryClass()).isEqualTo(SOLVER_THREAD_FACTORY_CLASS);
    }

    @Test
    public void serverStartedWithCreateSolverError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(extension).createSolver(eq(registry), any());
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR, ERROR_MESSAGE)), 1, 0, true);
    }

    @Test
    public void serverStartedWithSolverContainerExistingAndStartedSuccessful() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        when(registry.getContainer(SOLVER_CONTAINER_ID)).thenReturn(solverContainer);
        when(solverContainer.getStatus()).thenReturn(KieContainerStatus.STARTED);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(solverContainer);
    }

    @Test
    public void serverStartedWithSolverContainerExistingButNeedsActivationSuccessful() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareExistingContainerButNeedsActivationSuccessful(SOLVER_CONTAINER_ID, solverContainer);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(solverContainer);
    }

    @Test
    public void serverStartedWithSolverContainerExistingButNeedsActivationFailed() {
        prepareServerStartWithSolverContainerConfig();
        prepareExistingContainerButNeedsActivationFailed(SOLVER_CONTAINER_ID, solverContainer);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(ACTIVATE_CONTAINER_ERROR, SOLVER_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(PLANNER_CONTAINER_NOT_AVAILABLE, SOLVER_CONTAINER_ID)), 2, 1, true);
        verify(extension, never()).registerExtractors(solverContainer);
    }

    @Test
    public void serverStartedWithSolverContainerNotExistingButCreatedSuccessful() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareContainerNotExistingButCreatedSuccessful(SOLVER_CONTAINER_ID, solverContainer);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(solverContainer);
    }

    @Test
    public void serverStartedWithSolverContainerNotExistingButCreatedFailed() {
        prepareServerStartWithSolverContainerConfig();
        prepareContainerNotExistingButCreatedFailed(SOLVER_CONTAINER_ID);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(CREATE_CONTAINER_ERROR, SOLVER_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(PLANNER_CONTAINER_NOT_AVAILABLE, SOLVER_CONTAINER_ID)), 2, 1, true);
        verify(extension, never()).registerExtractors(solverContainer);
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingAndStartedSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        when(registry.getContainer(USER_SYSTEM_CONTAINER_ID)).thenReturn(userSystemContainer);
        when(userSystemContainer.getStatus()).thenReturn(KieContainerStatus.STARTED);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(userSystemContainer);
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingButNeedsActivationSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareExistingContainerButNeedsActivationSuccessful(USER_SYSTEM_CONTAINER_ID, userSystemContainer);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(userSystemContainer);
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingButNeedsActivationFailed() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareExistingContainerButNeedsActivationFailed(USER_SYSTEM_CONTAINER_ID, userSystemContainer);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(ACTIVATE_CONTAINER_ERROR, USER_SYSTEM_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, USER_SYSTEM_CONTAINER_ID)), 2, 1, true);
        verify(extension, never()).registerExtractors(userSystemContainer);
    }

    @Test
    public void serverStartedWithUserSystemContainerNotExistingButCreatedSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareContainerNotExistingButCreatedSuccessful(USER_SYSTEM_CONTAINER_ID, userSystemContainer);

        initAndStartServerSuccessful();
        verify(extension).registerExtractors(userSystemContainer);
    }

    @Test
    public void serverStartedWithUserSystemContainerNotExistingButCreatedFailed() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareContainerNotExistingButCreatedFailed(USER_SYSTEM_CONTAINER_ID);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(CREATE_CONTAINER_ERROR, USER_SYSTEM_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, USER_SYSTEM_CONTAINER_ID)), 2, 1, true);
        verify(extension, never()).registerExtractors(userSystemContainer);
    }

    @Test
    public void serverStartedWithUserSystemNotFoundError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        doReturn(null).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        enableExtension();
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_SERVICE_NOT_FOUND, USER_SYSTEM_NAME)), 1, 0, true);
    }

    @Test
    public void serverStartedWithUserSystemStartError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(userSystemService).start();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        enableExtension();
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_SERVICE_START_ERROR, USER_SYSTEM_NAME, ERROR_MESSAGE)), 1, 0, true);
    }

    @Test
    public void destroy() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        extension.init(kieServer, registry);
        extension.serverStarted();
        extension.destroy(kieServer, registry);
        verify(taskAssigningService).destroy();
    }

    @Test
    public void destroyWhenNotInitialized() {
        extension.destroy(kieServer, registry);
        verify(taskAssigningService, never()).destroy();
    }

    @Test
    public void healthCheck() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        List<Message> messages = extension.healthCheck(true);
        assertContainsMesssage(messages, Severity.INFO, HEALTH_CHECK_IS_ALIVE_MESSAGE, 0);
    }

    private void prepareTaskAssigningServiceProperties() {
        TaskAssigningPlanningKieServerExtensionHelperTest.prepareTaskAssigningServiceProperties();
    }

    private void prepareInitRuntimeClient() {
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_URL, RUNTIME_URL);
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_USER, RUNTIME_USER);
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_PWD, RUNTIME_PWD);
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT, RUNTIME_TIMEOUT);
    }

    private void prepareServerStartWithSolverContainerConfig() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        when(solverContainer.getKieContainer()).thenReturn(internalSolverKieContainer);
        when(internalSolverKieContainer.getClassLoader()).thenReturn(internalSolverKieContainerClassLoader);
        prepareSolverContainerProperties();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
    }

    private void prepareServerStartWithUserSystemContainerConfig() {
        prepareUserContainerProperties();
        enableExtension();
        when(userSystemContainer.getKieContainer()).thenReturn(internalUserSystemKieContainer);
        when(internalUserSystemKieContainer.getClassLoader()).thenReturn(internalUserSystemKieContainerClassLoader);
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doReturn(solver).when(extension).createSolver(eq(registry), any());
    }

    private void prepareUserContainerProperties() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, USER_SYSTEM_CONTAINER_ID);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID, USER_SYSTEM_CONTAINER_GROUP_ID);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID, USER_SYSTEM_CONTAINER_ARTIFACT_ID);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION, USER_SYSTEM_CONTAINER_VERSION);
    }

    private void prepareSolverContainerProperties() {
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID, SOLVER_CONTAINER_ID);
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID, SOLVER_CONTAINER_GROUP_ID);
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID, SOLVER_CONTAINER_ARTIFACT_ID);
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_VERSION, SOLVER_CONTAINER_VERSION);
    }

    private void prepareExistingContainerButNeedsActivationSuccessful(String containerId, KieContainerInstanceImpl containerMock) {
        when(registry.getContainer(containerId)).thenReturn(containerMock);
        when(kieServer.activateContainer(containerId)).thenReturn(containerResponse);
        when(containerResponse.getType()).thenReturn(KieServiceResponse.ResponseType.SUCCESS);

        prepareContainerStatusInvocations(containerMock, Arrays.asList(KieContainerStatus.DEACTIVATED,
                                                                       KieContainerStatus.DEACTIVATED,
                                                                       KieContainerStatus.STARTED));
    }

    private void prepareExistingContainerButNeedsActivationFailed(String containerId, KieContainerInstanceImpl containerMock) {
        when(registry.getContainer(containerId)).thenReturn(containerMock);
        when(kieServer.activateContainer(containerId)).thenReturn(containerResponse);
        when(containerResponse.getType()).thenReturn(KieServiceResponse.ResponseType.FAILURE);
        when(containerResponse.getMsg()).thenReturn(ERROR_MESSAGE);
        prepareContainerStatusInvocations(containerMock, Arrays.asList(KieContainerStatus.DEACTIVATED,
                                                                       KieContainerStatus.DEACTIVATED,
                                                                       KieContainerStatus.DEACTIVATED));
    }

    private void prepareContainerNotExistingButCreatedSuccessful(String containerId, KieContainerInstanceImpl containerMock) {
        prepareGetContainerInvocations(registry, containerId, Arrays.asList(null, containerMock));
        when(kieServer.createContainer(eq(containerId), any())).thenReturn(containerResponse);
        when(containerResponse.getType()).thenReturn(KieServiceResponse.ResponseType.SUCCESS);
        prepareContainerStatusInvocations(containerMock, Arrays.asList(KieContainerStatus.STARTED, KieContainerStatus.STARTED));
    }

    private void prepareContainerNotExistingButCreatedFailed(String containerId) {
        prepareGetContainerInvocations(registry, containerId, Arrays.asList(null, null));
        when(kieServer.createContainer(eq(containerId), any())).thenReturn(containerResponse);
        when(containerResponse.getType()).thenReturn(KieServiceResponse.ResponseType.FAILURE);
        when(containerResponse.getMsg()).thenReturn(ERROR_MESSAGE);
    }

    private void prepareContainerStatusInvocations(KieContainerInstanceImpl containerMock, List<KieContainerStatus> results) {
        doAnswer(new Answer() {
            private int invocations = 0;

            public Object answer(InvocationOnMock invocation) {
                return results.get(invocations++);
            }
        }).when(containerMock).getStatus();
    }

    private void prepareGetContainerInvocations(KieServerRegistry registryMock, String containerId, List<KieContainerInstanceImpl> results) {
        doAnswer(new Answer() {
            private int invocations = 0;

            public Object answer(InvocationOnMock invocation) {
                return results.get(invocations++);
            }
        }).when(registryMock).getContainer(containerId);
    }

    private void assertKieServerMessageWasAdded(Severity severity, String message, int times, int index, boolean includeHealthCheck) {
        verify(kieServer, times(times)).addServerMessage(messageCaptor.capture());
        assertContainsMesssage(messageCaptor.getAllValues(), severity, message, index);
        if (includeHealthCheck) {
            List<Message> healthCheckMessages = extension.healthCheck(true);
            assertContainsMesssage(healthCheckMessages, severity, message, index);
        }
    }

    private void assertContainsMesssage(List<Message> messages, Severity severity, String message, int index) {
        assertThat(messages.get(index).getSeverity()).isEqualTo(severity);
        assertThat(messages.get(index).getMessages().iterator().next()).isEqualTo(message);
    }

    private void initAndStartServerSuccessful() {
        extension.init(kieServer, registry);
        extension.serverStarted();
        verify(kieServer, never()).addServerMessage(any());
        verify(taskAssigningService).setExecutorService(any());
        verify(taskAssigningService).setDelegate(any());
        verify(taskAssigningService).setUserSystemService(userSystemService);
        verify(taskAssigningService).start(any(), eq(registry));
        verify(extension).createTaskAssigningService(taskAssigningServiceConfigCaptor.capture());
        assertThat(taskAssigningServiceConfigCaptor.getValue()).isNotNull();
    }

    private void enableExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, "false");
    }

    private void disableExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, "true");
    }

    private String getCurrentKeyStore() {
        String vendor = System.getProperty("java.vendor");
        if (vendor.toUpperCase().contains("IBM")) {
            return KEY_STORE_RESOURCE_IBM;
        } else {
            return KEY_STORE_RESOURCE;
        }
    }
}

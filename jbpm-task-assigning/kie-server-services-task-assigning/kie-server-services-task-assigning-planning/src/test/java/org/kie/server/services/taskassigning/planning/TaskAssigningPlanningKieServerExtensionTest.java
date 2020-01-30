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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_NAME;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.CAPABILITY_TASK_ASSIGNING_PLANNING;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.EXTENSION_NAME;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.EXTENSION_START_ORDER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.ACTIVATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.CREATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.HEALTH_CHECK_IS_ALIVE_MESSAGE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.SOLVER_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_CONTAINER_NOT_AVAILABLE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING;
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

    @Mock
    private KieContainerInstanceImpl solverContainer;

    @Mock
    private KieContainerInstanceImpl userSystemContainer;

    @Mock
    private InternalKieContainer internalKieContainer;

    @Mock
    private ClassLoader internalKieContainerClassLoader;

    @Mock
    private ServiceResponse<KieContainerResource> containerResponse;

    @Mock
    private TaskAssigningService taskAssigningService;

    @Before
    public void setUp() {
        extension = spy(new TaskAssigningPlanningKieServerExtension());
        when(kieServer.healthCheck(anyBoolean())).thenReturn(new ArrayList<>());
        doReturn(taskAssigningService).when(extension).createTaskAssigningService();
    }

    @After
    public void cleanUp() {
        System.clearProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED);

        System.clearProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL);
        System.clearProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER);
        System.clearProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD);

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
        assertFalse(extension.isActive());
    }

    @Test
    public void isActiveTrue() {
        enableExtension();
        assertTrue(extension.isActive());
    }

    @Test
    public void isActiveFalse() {
        disableExtension();
        assertFalse(extension.isActive());
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
        System.setProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL, RUNTIME_URL);
        System.setProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER, RUNTIME_USER);
        System.setProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD, RUNTIME_PWD);
        enableExtension();
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        extension.init(kieServer, registry);
        verify(extension).createRuntimeClient(RUNTIME_URL, RUNTIME_USER, RUNTIME_PWD);
    }

    @Test
    public void initWithSolverContainerConfigurationError() {
        System.setProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID, SOLVER_CONTAINER_ID);
        enableExtension();
        String error = String.format(SOLVER_CONFIGURATION_ERROR, String.format(REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING, SOLVER_CONTAINER_ID, null, null, null));
        Assertions.assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertFalse(extension.isInitialized());
    }

    @Test
    public void initWithSolverContainer() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        prepareSolverContainerProperties();
        enableExtension();
        extension.init(kieServer, registry);
        assertTrue(extension.isInitialized());
    }

    @Test
    public void initWithUserSystemMissingError() {
        enableExtension();
        String error = String.format(USER_SYSTEM_CONFIGURATION_ERROR, String.format(USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR, TASK_ASSIGNING_USER_SYSTEM_NAME));
        Assertions.assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertFalse(extension.isInitialized());
    }

    @Test
    public void initWithUserSystemContainerError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, USER_SYSTEM_CONTAINER_ID);
        enableExtension();
        String error = String.format(USER_SYSTEM_CONFIGURATION_ERROR, String.format(REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING, USER_SYSTEM_CONTAINER_ID, null, null, null));
        Assertions.assertThatThrownBy(() -> extension.init(kieServer, registry)).hasMessage(error);
        assertFalse(extension.isInitialized());
    }

    @Test
    public void initWithUserContainer() {
        prepareUserContainerProperties();
        enableExtension();
        extension.init(kieServer, registry);
        assertTrue(extension.isInitialized());
    }

    @Test
    public void getExtensionName() {
        assertEquals(EXTENSION_NAME, extension.getExtensionName());
    }

    @Test
    public void getServices() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        List<Object> services = extension.getServices();
        assertEquals(1, services.size());
        assertTrue(services.get(0) instanceof TaskAssigningService);
    }

    @Test
    public void getImplementedCapability() {
        assertEquals(CAPABILITY_TASK_ASSIGNING_PLANNING, extension.getImplementedCapability());
    }

    @Test
    public void getAppComponents() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        assertNotNull(extension.getAppComponents(TaskAssigningService.class));
    }

    @Test
    public void getStartOrder() {
        assertEquals(EXTENSION_START_ORDER, extension.getStartOrder(), 0);
    }

    @Test
    public void serverStartedSuccessful() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
        doReturn(solver).when(extension).createSolver(eq(registry), any());

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithCreateSolverError() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
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
    }

    @Test
    public void serverStartedWithSolverContainerExistingButNeedsActivationSuccessful() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareExistingContainerButNeedsActivationSuccessful(SOLVER_CONTAINER_ID, solverContainer);

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithSolverContainerExistingButNeedsActivationFailed() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareExistingContainerButNeedsActivationFailed(SOLVER_CONTAINER_ID, solverContainer);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(ACTIVATE_CONTAINER_ERROR, SOLVER_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(PLANNER_CONTAINER_NOT_AVAILABLE, SOLVER_CONTAINER_ID)), 2, 1, true);
    }

    @Test
    public void serverStartedWithSolverContainerNotExistingButCreatedSuccessful() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareContainerNotExistingButCreatedSuccessful(SOLVER_CONTAINER_ID, solverContainer);

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithSolverContainerNotExistingButCreatedFailed() {
        prepareServerStartWithSolverContainerConfig();
        doReturn(solver).when(extension).createSolver(eq(registry), any());
        prepareContainerNotExistingButCreatedFailed(SOLVER_CONTAINER_ID);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(CREATE_CONTAINER_ERROR, SOLVER_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(PLANNER_CONTAINER_NOT_AVAILABLE, SOLVER_CONTAINER_ID)), 2, 1, true);
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingAndStartedSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        when(registry.getContainer(USER_SYSTEM_CONTAINER_ID)).thenReturn(userSystemContainer);
        when(userSystemContainer.getStatus()).thenReturn(KieContainerStatus.STARTED);

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingButNeedsActivationSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareExistingContainerButNeedsActivationSuccessful(USER_SYSTEM_CONTAINER_ID, userSystemContainer);

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithUserSystemContainerExistingButNeedsActivationFailed() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareExistingContainerButNeedsActivationFailed(USER_SYSTEM_CONTAINER_ID, userSystemContainer);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(ACTIVATE_CONTAINER_ERROR, USER_SYSTEM_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, USER_SYSTEM_CONTAINER_ID)), 2, 1, true);
    }

    @Test
    public void serverStartedWithUserSystemContainerNotExistingButCreatedSuccessful() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareContainerNotExistingButCreatedSuccessful(USER_SYSTEM_CONTAINER_ID, userSystemContainer);

        initAndStartServerSuccessful();
    }

    @Test
    public void serverStartedWithUserSystemContainerNotExistingButCreatedFailed() {
        prepareServerStartWithUserSystemContainerConfig();
        prepareContainerNotExistingButCreatedFailed(USER_SYSTEM_CONTAINER_ID);
        extension.init(kieServer, registry);
        extension.serverStarted();
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(CREATE_CONTAINER_ERROR, USER_SYSTEM_CONTAINER_ID, ERROR_MESSAGE)), 2, 0, true);
        assertKieServerMessageWasAdded(Severity.ERROR, addExtensionMessagePrefix(String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, USER_SYSTEM_CONTAINER_ID)), 2, 1, true);
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
    public void healthCheck() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        extension.init(kieServer, registry);
        List<Message> messages = extension.healthCheck(true);
        assertContainsMesssage(messages, Severity.INFO, HEALTH_CHECK_IS_ALIVE_MESSAGE, 0);
    }

    private void prepareServerStartWithSolverContainerConfig() {
        System.setProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, USER_SYSTEM_NAME);
        enableExtension();
        prepareSolverContainerProperties();
        doReturn(userSystemService).when(extension).lookupUserSystem(eq(USER_SYSTEM_NAME), any());
    }

    private void prepareServerStartWithUserSystemContainerConfig() {
        prepareUserContainerProperties();
        enableExtension();
        when(userSystemContainer.getKieContainer()).thenReturn(internalKieContainer);
        when(internalKieContainer.getClassLoader()).thenReturn(internalKieContainerClassLoader);
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
        assertEquals(severity, messages.get(index).getSeverity());
        assertEquals(message, messages.get(index).getMessages().iterator().next());
    }

    private void initAndStartServerSuccessful() {
        extension.init(kieServer, registry);
        extension.serverStarted();
        verify(kieServer, never()).addServerMessage(any());
        verify(taskAssigningService).setExecutorService(any());
        verify(taskAssigningService).setDelegate(any());
        verify(taskAssigningService).setUserSystemService(userSystemService);
        verify(taskAssigningService).start(any(), eq(registry));
    }

    private void enableExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, "false");
    }

    private void disableExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, "true");
    }
}

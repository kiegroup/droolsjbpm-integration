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

package org.kie.server.services.taskassigning.runtime;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.kie.server.services.taskassigning.runtime.query.TaskAssigningTaskDataQueryMapper;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension.CAPABILITY_TASK_ASSIGNING_RUNTIME;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension.EXTENSION_NAME;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension.EXTENSION_START_ORDER;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.HEALTH_CHECK_ERROR;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.HEALTH_CHECK_IS_ALIVE_MESSAGE;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.MISSING_REQUIRED_JBPM_EXTENSION_ERROR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeKieServerExtensionTest {

    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    @Mock
    private KieServerImpl kieServer;

    @Mock
    private KieServerRegistry registry;

    private TaskAssigningRuntimeKieServerExtension extension;

    @Mock
    private JbpmKieServerExtension jbpmExtension;

    @Mock
    private QueryService queryService;

    @Mock
    private UserTaskService userTaskService;

    @Before
    public void setUp() {
        extension = new TaskAssigningRuntimeKieServerExtension();
        List<Object> services = new ArrayList<>();
        services.add(queryService);
        services.add(userTaskService);
        when(kieServer.isKieServerReady()).thenReturn(true);
        when(jbpmExtension.getServices()).thenReturn(services);
    }

    @After
    public void cleanUp() {
        System.clearProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED);
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
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, "true");
        assertFalse(extension.isActive());
    }

    @Test
    public void initWithJbpmExtensionNotAvailable() {
        enableExtension();
        Assertions.assertThatThrownBy(() -> extension.init(kieServer, registry))
                .hasMessage(MISSING_REQUIRED_JBPM_EXTENSION_ERROR);
        assertFalse(extension.isInitialized());
    }

    @Test
    public void initSuccessful() {
        prepareExtension();
        extension.init(kieServer, registry);
        assertTrue(extension.isInitialized());
        verify(queryService, times(3)).replaceQuery(any());
    }

    @Test
    public void getExtensionName() {
        assertEquals(EXTENSION_NAME, extension.getExtensionName());
    }

    @Test
    public void getServices() {
        prepareExtension();
        extension.init(kieServer, registry);
        List<Object> services = extension.getServices();
        assertEquals(1, services.size(), 0);
        assertTrue(services.get(0) instanceof TaskAssigningRuntimeServiceBase);
    }

    @Test
    public void getAppComponents() {
        prepareExtension();
        extension.init(kieServer, registry);
        List<Object> components = extension.getAppComponents(SupportedTransports.REST);
        assertEquals(1, components.size());
        assertTrue(components.get(0) instanceof DummyKieServerApplicationComponentService.DummyComponent);
    }

    @Test
    public void getAppComponentsForTaskAssigningRuntimeServiceBase() {
        prepareExtension();
        extension.init(kieServer, registry);
        assertNotNull(extension.getAppComponents(TaskAssigningRuntimeServiceBase.class));
    }

    @Test
    public void getImplementedCapability() {
        assertEquals(CAPABILITY_TASK_ASSIGNING_RUNTIME, extension.getImplementedCapability());
    }

    @Test
    public void getStartOrder() {
        assertEquals(EXTENSION_START_ORDER, extension.getStartOrder(), 0);
    }

    @Test
    public void healthCheck() {
        prepareExtension();
        extension.init(kieServer, registry);
        List<Message> messages = extension.healthCheck(true);
        assertEquals(1, messages.size());
        assertEquals(Severity.INFO, messages.get(0).getSeverity());
        assertEquals(HEALTH_CHECK_IS_ALIVE_MESSAGE, messages.get(0).getMessages().iterator().next());
    }

    @Test
    public void healthCheckWithFailure() {
        prepareExtension();
        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(queryService).query(anyString(), any(TaskAssigningTaskDataQueryMapper.class), any(QueryContext.class), any(QueryParam[].class));
        extension.init(kieServer, registry);
        List<Message> messages = extension.healthCheck(true);
        assertEquals(1, messages.size());
        assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        assertEquals(String.format(HEALTH_CHECK_ERROR, ERROR_MESSAGE), messages.get(0).getMessages().iterator().next());
    }

    private void prepareExtension() {
        enableExtension();
        when(registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME)).thenReturn(jbpmExtension);
    }

    private void enableExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, "false");
    }
}

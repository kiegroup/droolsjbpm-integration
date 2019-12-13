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
package org.kie.server.services.jbpm.ui;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.task.commands.GetUserTaskCommand;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.TaskData;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormServiceBaseTest {

    private final String dummyForm = "{\"id\": \"30f64834-1fe6-4ffb-ba48-024bc6839d5c\", \"name\": \"form-taskform.frm\", \"fields\": [{\"maxLength\": 100,\"placeHolder\": \"Resultado\",\"id\": \"field_id\",\"name\": \"field\",\"label\": \"Field\",\"code\": \"TextBox\"}]}";

    private final String CONTAINER_ID = "test-container";
    private final String PROCESS_ID = "test-processId";

    @Mock
    KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

    @Mock
    DefinitionService definitionService;

    @Mock
    RuntimeDataService dataService;

    @Mock
    UserTaskService userTaskService;

    @Mock
    FormManagerService formManagerService;

    @Mock
    IdentityProvider identityProvider;

    @Mock
    KieServerConfig config;

    @Mock
    TaskImpl task;

    @Mock
    TaskData taskData;

    @Mock
    ProcessDefinition processDefinition;

    @Before
    public void setupMocks() {
        when(kieServerRegistry.getContainerId(anyString(), any(ContainerLocator.class))).thenReturn(CONTAINER_ID);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(kieServerRegistry.getIdentityProvider()).thenReturn(identityProvider);

        when(identityProvider.getName()).thenReturn("admin");

        when(userTaskService.execute(any(), any())).thenReturn(task);
        when(task.getName()).thenReturn("task");
        when(task.getId()).thenReturn(1L);
        when(task.getTaskData()).thenReturn(taskData);
        when(task.getFormName()).thenReturn("form");
        when(taskData.getProcessId()).thenReturn(PROCESS_ID);
        when(taskData.getDeploymentId()).thenReturn(CONTAINER_ID);

        when(dataService.getProcessesByDeploymentIdProcessId(eq(CONTAINER_ID), eq(PROCESS_ID))).thenReturn(processDefinition);
        when(processDefinition.getName()).thenReturn("test");
        when(formManagerService.getFormByKey(eq(CONTAINER_ID), eq("form-taskform.frm"))).thenReturn(dummyForm);
    }

    @Test
    public void testGetFormDisplayTaskUsesRequestorIdentity() {
        when(config.getConfigItemValue(eq(KieServerConstants.CFG_BYPASS_AUTH_USER), anyString())).thenReturn("false");

        FormServiceBase formServiceBase = new FormServiceBase(definitionService, dataService, userTaskService, formManagerService, kieServerRegistry);

        String userId = "john";
        formServiceBase.getFormDisplayTask(CONTAINER_ID, 1L, userId, "en_US", true, FormServiceBase.FormType.ANY.getName());

        verify(kieServerRegistry, times(1)).getIdentityProvider();

        ArgumentCaptor<GetUserTaskCommand> argument = ArgumentCaptor.forClass(GetUserTaskCommand.class);
        verify(userTaskService, times(1)).execute(eq(CONTAINER_ID), argument.capture());
        Assert.assertEquals("admin", argument.getValue().getUserId());
    }

    @Test
    public void testGetFormDisplayTaskBypassesAuthUser() {
        // Set configuration to allow bypass Auth User
        when(config.getConfigItemValue(eq(KieServerConstants.CFG_BYPASS_AUTH_USER), anyString())).thenReturn("true");

        FormServiceBase formServiceBase = new FormServiceBase(definitionService, dataService, userTaskService, formManagerService, kieServerRegistry);

        String userId = "john";
        formServiceBase.getFormDisplayTask(CONTAINER_ID, 1L, userId, "en_US", true, FormServiceBase.FormType.ANY.getName());

        verify(kieServerRegistry, times(0)).getIdentityProvider();

        ArgumentCaptor<GetUserTaskCommand> argument = ArgumentCaptor.forClass(GetUserTaskCommand.class);
        verify(userTaskService, times(1)).execute(eq(CONTAINER_ID), argument.capture());
        Assert.assertEquals("john", argument.getValue().getUserId());
    }

}

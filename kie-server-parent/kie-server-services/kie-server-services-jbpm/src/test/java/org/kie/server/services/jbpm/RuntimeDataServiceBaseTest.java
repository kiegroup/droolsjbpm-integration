/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.DeploymentNotFoundException;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.junit.Test;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieServerRegistry;

public class RuntimeDataServiceBaseTest {

    private final AdvanceRuntimeDataService advanceRuntimeDataService = mock(AdvanceRuntimeDataService.class);
    private final RuntimeDataService runtimeDataService = mock(RuntimeDataService.class);
    private final KieServerRegistry context = mock(KieServerRegistry.class);
    private final KieServerConfig config = mock(KieServerConfig.class);

    public RuntimeDataServiceBaseTest() {
        when(context.getConfig()).thenReturn(config);
    }

    @Test
    public void testGetProcessesByDeploymentId() {
        RuntimeDataServiceBase svc = new RuntimeDataServiceBase(runtimeDataService, advanceRuntimeDataService, context);

        String containerId = "container_id";
        when(context.getContainerId(anyString(), any(ContainerLocator.class))).thenReturn(containerId);
        List<ProcessDefinition> definitions = new ArrayList<>();
        definitions.add( mock(ProcessDefinition.class));
        when(runtimeDataService.getProcessesByDeploymentId(anyString(), any())).thenReturn(definitions);

        ProcessDefinitionList result = svc.getProcessesByDeploymentId(containerId, 0, 100, null, true);

        assertEquals(1, result.getItems().size());
    }

    @Test
    public void testGetProcessesByDeploymentId_DeploymentNotFound() {
        RuntimeDataServiceBase svc = new RuntimeDataServiceBase(runtimeDataService, advanceRuntimeDataService, context);

        String containerId = "container_id";
        when(context.getContainerId(anyString(), any(ContainerLocator.class))).thenReturn(containerId);
        List<ProcessDefinition> definitions = new ArrayList<>();
        definitions.add( mock(ProcessDefinition.class));
        when(runtimeDataService.getProcessesByDeploymentId(anyString(), any())).thenThrow(new DeploymentNotFoundException("not found"));

        ProcessDefinitionList result = svc.getProcessesByDeploymentId(containerId, 0, 100, null, true);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void testGetProcessesByDeploymentId_IllegalArgumentException() {
        RuntimeDataServiceBase svc = new RuntimeDataServiceBase(runtimeDataService, advanceRuntimeDataService, context);

        String containerId = "container_id";
        when(context.getContainerId(anyString(), any(ContainerLocator.class))).thenReturn(containerId);
        List<ProcessDefinition> definitions = new ArrayList<>();
        definitions.add( mock(ProcessDefinition.class));
        when(runtimeDataService.getProcessesByDeploymentId(anyString(), any())).thenThrow(new IllegalArgumentException("not found"));

        ProcessDefinitionList result = svc.getProcessesByDeploymentId(containerId, 0, 100, null, true);

        assertTrue(result.getItems().isEmpty());
    }
}
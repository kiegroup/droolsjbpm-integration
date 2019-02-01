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

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;

public class KieServicesClientIntegrationTest extends AbstractRemoteIntegrationTest {

    private static final String KIE_SERVER_CAPABILITY = "KieServer";

    @Test
    public void testGetKieServerInfo() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("getServerInfo");
        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(KieServerInfo.class);

        final KieServerInfo kieServerInfo = (KieServerInfo) response;
        Assertions.assertThat(kieServerInfo.getCapabilities()).contains(KIE_SERVER_CAPABILITY);
    }

    @Test
    public void testGetKieServerState() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("getServerState");
        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();

        final KieServerStateInfo kieServerStateInfo = (KieServerStateInfo) response;
        Assertions.assertThat(kieServerStateInfo.getContainers()).isNotEmpty();
    }

    @Test
    public void testGetContainerResource() {
        final KieContainerResource kieContainerResource = getContainerResource(CONTAINER_ID);
        Assertions.assertThat(kieContainerResource).isNotNull();
        Assertions.assertThat(kieContainerResource.getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(kieContainerResource.getReleaseId()).isEqualToComparingFieldByField(RELEASE_ID);
    }

    @Test
    public void testDeactivateActivateContainer() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", CONTAINER_ID);

        final ExecutionServerCommand deactivateContainerCommand = new ExecutionServerCommand();
        deactivateContainerCommand.setClient("kieServices");
        deactivateContainerCommand.setOperation("deactivateContainer");
        deactivateContainerCommand.setParameters(parameters);
        runOnExecutionServer(deactivateContainerCommand);

        KieContainerResource kieContainerResource = getContainerResource(CONTAINER_ID);
        Assertions.assertThat(kieContainerResource.getStatus()).isEqualTo(KieContainerStatus.DEACTIVATED);

        final ExecutionServerCommand activateContainerCommand = new ExecutionServerCommand();
        activateContainerCommand.setClient("kieServices");
        activateContainerCommand.setOperation("activateContainer");
        activateContainerCommand.setParameters(parameters);
        runOnExecutionServer(activateContainerCommand);

        kieContainerResource = getContainerResource(CONTAINER_ID);
        Assertions.assertThat(kieContainerResource.getStatus()).isEqualTo(KieContainerStatus.STARTED);
    }

    @Test
    public void testListContainers() {
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

    private KieContainerResource getContainerResource(String containerId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", containerId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("kieServices");
        executionServerCommand.setOperation("getContainerInfo");
        executionServerCommand.setParameters(parameters);
        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(KieContainerResource.class);

        final KieContainerResource kieContainerResource = (KieContainerResource) response;
        return kieContainerResource;
    }
}

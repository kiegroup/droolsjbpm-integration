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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.server.api.model.KieContainerResourceList;

public class KieServicesClientIntegrationTest extends AbstractRemoteIntegrationTest {

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
}

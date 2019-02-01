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
import org.kie.server.api.model.definition.ProcessDefinition;

public class ProcessClientIntegrationTest extends AbstractRemoteIntegrationTest {

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
}

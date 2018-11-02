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

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;

public class ProcessEngineIntegrationTest extends AbstractKieCamelIntegrationTest {

    private static final String SIMPLE_PROCESS_ID = "process1";
    private static final String PROCESS_WITH_SIGNAL = "processWithSignal";
    private static final String SIGNAL_NAME = "signal1";

    @Test
    public void testGetProcessIds() {
        final GetProcessIdsCommand getProcessIdsCommand = new GetProcessIdsCommand();
        getProcessIdsCommand.setOutIdentifier(DEFAULT_OUT_ID);

        final ExecutionResults executionResults = runCommand(getProcessIdsCommand);
        Assertions.assertThat(executionResults).isNotNull();
        Assertions.assertThat((List) executionResults.getValue(DEFAULT_OUT_ID)).contains(SIMPLE_PROCESS_ID,
                                                                                         PROCESS_WITH_SIGNAL);
    }

    @Test
    public void testStartProcess() {
        final StartProcessCommand command = (StartProcessCommand) kieCommands.newStartProcess(SIMPLE_PROCESS_ID);
        command.setOutIdentifier(DEFAULT_OUT_ID);
        final ExecutionResults response = runCommand(command);
        Assertions.assertThat(response).isNotNull();
        final Long processId = (Long) response.getValue(DEFAULT_OUT_ID);
        Assertions.assertThat(processId).isNotNull();
        Assertions.assertThat(processId).isPositive();
    }

    @Test
    public void testSignalToProcess() {
        /* Start process */
        final StartProcessCommand startProcessCommand = (StartProcessCommand) kieCommands.newStartProcess(PROCESS_WITH_SIGNAL);
        startProcessCommand.setOutIdentifier(DEFAULT_OUT_ID);
        final ExecutionResults responseStartProcess = runCommand(startProcessCommand);
        Assertions.assertThat(responseStartProcess).isNotNull();
        final Long processId = (Long) responseStartProcess.getValue(DEFAULT_OUT_ID);
        Assertions.assertThat(processId).isNotNull();
        Assertions.assertThat(processId).isPositive();

        /* Check that process is running */
        Assertions.assertThat(listProcesses()).contains(processId);

        /* Send signal to allow process to terimanate */
        final SignalEventCommand signalEventCommand = new SignalEventCommand(SIGNAL_NAME, new Object());
        runCommand(signalEventCommand);

        /* Check that process was finished */
        Assertions.assertThat(listProcesses()).doesNotContain(processId);
    }
}

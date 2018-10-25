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

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;

public class ProcessEngineIntegrationTest extends AbstractKieCamelIntegrationTest {

    private static final String TEST_PROCESS_ID = "process1";
    private static final String PROCESS_INSTANCE_ID_PROPERTY = "process-id";

    @Test
    public void testStartProcess() {
        final KieCommands kieCommands = KieServices.Factory.get().getCommands();

        final StartProcessCommand command = (StartProcessCommand) kieCommands.newStartProcess(TEST_PROCESS_ID);
        command.setOutIdentifier(PROCESS_INSTANCE_ID_PROPERTY);
        final ExecutionResultImpl response = kieCamelTestService.startProcessCommand(command);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getResults().get(PROCESS_INSTANCE_ID_PROPERTY)).isNotNull();
    }
}

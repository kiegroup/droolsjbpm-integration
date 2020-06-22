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

package org.kie.server.springboot.autoconfiguration.taskassigning;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension;
import org.springframework.context.event.ContextClosedEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;
import static org.mockito.Mockito.mock;

public class TaskAssigningRuntimeKieServerAutoConfigurationTest {

    private TaskAssigningRuntimeKieServerAutoConfiguration configuration;

    private static final String KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED_PREVIOUS_VALUE = "KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED_PREVIOUS_VALUE";

    @Before
    public void setUp() {
        configuration = new TaskAssigningRuntimeKieServerAutoConfiguration();
    }

    @After
    public void cleanUp() {
        System.clearProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED_PREVIOUS_VALUE);
    }

    @Test
    public void taskAssigningRuntimeServerExtension() {
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED_PREVIOUS_VALUE);
        TaskAssigningRuntimeKieServerExtension extension = (TaskAssigningRuntimeKieServerExtension) configuration.taskAssigningRuntimeServerExtension();
        assertTrue(extension.isActive());
        configuration.handleContextRefreshEvent(mock(ContextClosedEvent.class));
        assertEquals(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED_PREVIOUS_VALUE, System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED));
    }

    @Test
    public void taskAssigningPersistenceUnitPostProcessor() {
        assertNotNull(configuration.taskAssigningPersistenceUnitPostProcessor());
    }
}

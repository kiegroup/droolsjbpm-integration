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

package org.kie.server.services.taskassigning.runtime.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningPersistenceUnitExtensionsLoaderTest {

    private TaskAssigningPersistenceUnitExtensionsLoader extensionsLoader;

    @Mock
    private PersistenceUnitInfo info;

    @Before
    public void setUp() {
        extensionsLoader = new TaskAssigningPersistenceUnitExtensionsLoader();
    }

    @After
    public void cleanUp() {
        System.clearProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED);
    }

    @Test
    public void isEnabledTrue() {
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, "false");
        assertThat(extensionsLoader.isEnabled()).isTrue();
    }

    @Test
    public void isEnabledFalse() {
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, "true");
        assertThat(extensionsLoader.isEnabled()).isFalse();
    }

    @Test
    public void isEnabledFalseWithUnSetProperty() {
        System.clearProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED);
        assertThat(extensionsLoader.isEnabled()).isFalse();
    }

    @Test
    public void loadExtensions() {
        List<String> managedClassNames = new ArrayList<>();
        when(info.getManagedClassNames()).thenReturn(managedClassNames);
        extensionsLoader.loadExtensions(info);
        assertThat(managedClassNames).contains(PlanningTaskImpl.class.getName());
    }
}

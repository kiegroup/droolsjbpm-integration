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

package org.kie.server.remote.rest.taskassigning.runtime;

import java.util.Collection;

import org.junit.Test;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TaskAssigningRuntimeRestApplicationComponentsServiceTest {

    @Test
    public void getAppComponents() {
        TaskAssigningRuntimeServiceBase serviceMock = mock(TaskAssigningRuntimeServiceBase.class);
        KieServerRegistry registryMock = mock(KieServerRegistry.class);

        TaskAssigningRuntimeRestApplicationComponentsService componentsService = new TaskAssigningRuntimeRestApplicationComponentsService();
        Collection<Object> result = componentsService.getAppComponents(TaskAssigningRuntimeKieServerExtension.EXTENSION_NAME,
                                                                       SupportedTransports.REST,
                                                                       serviceMock,
                                                                       registryMock);
        assertEquals(1, result.size());
        assertTrue(result.iterator().next() instanceof TaskAssigningRuntimeResource);
    }
}

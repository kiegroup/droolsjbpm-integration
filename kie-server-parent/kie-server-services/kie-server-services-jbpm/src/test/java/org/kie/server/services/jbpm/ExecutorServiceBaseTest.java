/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


import org.junit.Test;
import org.junit.runner.RunWith;

import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecutorServiceBaseTest extends JbpmKieServerExtensionBaseTest {

    @Mock
    private JobRequestInstance jobRequest;

    @Mock
    private MarshallerHelper marshallerHelper;

    @InjectMocks
    private ExecutorServiceBase executorServiceBase;


    @Test
    public void testExecutorServiceDisabling() throws Exception {
        String containerId = "containerId";
        String payload = "payload";
        String marshallingType = "marshalingType";


        when(marshallerHelper.unmarshal(containerId, payload, marshallingType, JobRequestInstance.class)).thenReturn(jobRequest);
        when(jobRequest.getCommand()).thenReturn("invalidCommand");

        try {
            executorServiceBase.scheduleRequest(containerId, payload, marshallingType);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("Invalid command type", e.getMessage());
        }

    }

}

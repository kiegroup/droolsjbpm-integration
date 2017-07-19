/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.impl;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KieServerInstanceManagerTest {

    @Mock
    private ContainerSpec containerSpec;

    @Mock
    private Runnable onSuccess;

    @Mock
    private Runnable onError;

    @Mock
    private KieServicesClient client;

    @Mock
    private Container container;

    @Mock
    private ServiceResponse<?> response;

    @Mock
    private ServerTemplate serverTemplate;

    private KieServerInstanceManager instanceManager;

    @Before
    public void setUp() {
        final ArrayList<Container> containers = new ArrayList<>();

        instanceManager = spy(new KieServerInstanceManager());

        doReturn(containers).when(instanceManager).callRemoteKieServerOperation(any(),
                                                                                any(),
                                                                                any());
        doNothing().when(instanceManager).collectContainerInfo(any(),
                                                               any(),
                                                               any());
    }

    @Test
    public void testStopOperationWhenDisposeContainerIsSuccessfully() throws Exception {
        doReturn(ServiceResponse.ResponseType.SUCCESS).when(response).getType();
        doReturn(response).when(client).disposeContainer(any());

        final KieServerInstanceManager.RemoteKieServerOperation<Void> operation = instanceManager.stopOperation(containerSpec,
                                                                                                                onSuccess,
                                                                                                                onError);

        operation.doOperation(client,
                              container);

        verify(onSuccess).run();
        verify(onError,
               never()).run();
        verify(instanceManager).collectContainerInfo(any(),
                                                     any(),
                                                     any());
    }

    @Test
    public void testStopOperationWhenDisposeContainerIsNotSuccessfully() throws Exception {
        doReturn(ServiceResponse.ResponseType.NO_RESPONSE).when(response).getType();
        doReturn(response).when(client).disposeContainer(any());

        final KieServerInstanceManager.RemoteKieServerOperation<Void> operation = instanceManager.stopOperation(containerSpec,
                                                                                                                onSuccess,
                                                                                                                onError);

        operation.doOperation(client,
                              container);

        verify(onError).run();
        verify(onSuccess,
               never()).run();
        verify(instanceManager).collectContainerInfo(any(),
                                                     any(),
                                                     any());
    }

    @Test
    public void testStopContainerWithCallbacks() throws Exception {
        final KieServerInstanceManager.RemoteKieServerOperation<?> operation = mock(KieServerInstanceManager.RemoteKieServerOperation.class);

        doReturn(operation).when(instanceManager).stopOperation(containerSpec,
                                                                onSuccess,
                                                                onError);

        instanceManager.stopContainer(serverTemplate,
                                      containerSpec,
                                      onSuccess,
                                      onError);

        verify(instanceManager).stopOperation(containerSpec,
                                              onSuccess,
                                              onError);
        verify(instanceManager).callRemoteKieServerOperation(serverTemplate,
                                                             containerSpec,
                                                             operation);
    }

    @Test
    public void testStopContainerWithoutCallbacks() throws Exception {
        final ArrayList<Container> containers = new ArrayList<>();
        final Runnable emptyCallback = () -> {
        };

        doReturn(emptyCallback).when(instanceManager).emptyCallback();
        doReturn(containers).when(instanceManager).stopContainer(any(),
                                                                 any(),
                                                                 any(),
                                                                 any());

        instanceManager.stopContainer(serverTemplate,
                                      containerSpec);

        verify(instanceManager).stopContainer(serverTemplate,
                                              containerSpec,
                                              emptyCallback,
                                              emptyCallback);
    }
}

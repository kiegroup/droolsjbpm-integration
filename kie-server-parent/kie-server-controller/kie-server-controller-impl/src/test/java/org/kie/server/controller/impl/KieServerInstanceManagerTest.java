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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KieServerInstanceManagerTest {

    @Mock
    private ServerTemplate serverTemplate;

    @Mock
    private ContainerSpec containerSpec;

    @Mock
    private KieServicesClient client;

    @Mock
    private Container container;

    @Mock
    private ServiceResponse<?> response;

    @Mock
    private KieContainerResource containerResource;

    private KieServerInstanceManager instanceManager;

    @Before
    public void setUp() {
        instanceManager = spy(new KieServerInstanceManager());
    }

    @Test
    public void testGetContainers() {
        final KieServerInstanceManager.RemoteKieServerOperation<?> operation = mock(KieServerInstanceManager.RemoteKieServerOperation.class);

        doReturn(operation).when(instanceManager).getContainersRemoteOperation(serverTemplate,
                                                                               containerSpec);

        instanceManager.getContainers(serverTemplate,
                                      containerSpec);

        verify(instanceManager).callRemoteKieServerOperation(serverTemplate,
                                                             containerSpec,
                                                             operation);
    }

    @Test
    public void testGetContainersRemoteOperationWhenResponseTypeIsSUCCESS() {
        doReturn(containerResource).when(response).getResult();
        doReturn(response).when(client).getContainerInfo(any());
        doReturn(ServiceResponse.ResponseType.SUCCESS).when(response).getType();

        final KieServerInstanceManager.RemoteKieServerOperation<Void> operation = instanceManager.getContainersRemoteOperation(serverTemplate,
                                                                                                                               containerSpec);

        operation.doOperation(client,
                              container);

        verify(container).setContainerSpecId(containerResource.getContainerId());
        verify(container).setContainerName(containerResource.getContainerId());
        verify(container).setResolvedReleasedId(containerResource.getReleaseId());
        verify(container).setServerTemplateId(serverTemplate.getId());
        verify(container).setStatus(containerResource.getStatus());
        verify(container).setMessages(containerResource.getMessages());
    }

    @Test
    public void testGetContainersRemoteOperationWhenResponseTypeIsNotSUCCESS() {
        doReturn(containerResource).when(response).getResult();
        doReturn(response).when(client).getContainerInfo(any());
        doReturn(ServiceResponse.ResponseType.FAILURE).when(response).getType();

        final KieServerInstanceManager.RemoteKieServerOperation<Void> operation = instanceManager.getContainersRemoteOperation(serverTemplate,
                                                                                                                               containerSpec);

        operation.doOperation(client,
                              container);

        verify(container,
               never()).setContainerSpecId(any());
        verify(container,
               never()).setContainerName(any());
        verify(container,
               never()).setResolvedReleasedId(any());
        verify(container,
               never()).setServerTemplateId(any());
        verify(container,
               never()).setStatus(any());
        verify(container,
               never()).setMessages(any());
    }
}

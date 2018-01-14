/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.policy;

import org.drools.core.impl.InternalKieContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeepLatestContainerOnlyPolicyTest {

    private static final String CONTAINER_ONE = "container1";
    private static final String CONTAINER_TWO = "container2";

    private KieServer kieServer;
    private InternalKieContainer kieContainerOne;
    private InternalKieContainer kieContainerTwo;

    @Before
    public void setUp() {
        kieServer = mock(KieServer.class);
        when(kieServer.disposeContainer(anyString())).thenReturn(new ServiceResponse<>(ServiceResponse.ResponseType.NO_RESPONSE, "Success."));

        kieContainerOne = mock(InternalKieContainer.class);
        when(kieContainerOne.getReleaseId()).thenReturn(new ReleaseId("org.kie", "container", "1.0"));
        when(kieContainerOne.getContainerReleaseId()).thenReturn(new ReleaseId("org.kie", "container", "1.0"));

        kieContainerTwo = mock(InternalKieContainer.class);
        when(kieContainerTwo.getReleaseId()).thenReturn(new ReleaseId("org.kie", "container", "2.0"));
        when(kieContainerTwo.getContainerReleaseId()).thenReturn(new ReleaseId("org.kie", "container", "2.0"));
    }

    @Test
    public void testDisposeOldContainer() {
        KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

        KieContainerInstanceImpl kieContainerInstance = new KieContainerInstanceImpl(CONTAINER_ONE, KieContainerStatus.STARTED, kieContainerOne);
        kieServerRegistry.registerContainer(CONTAINER_ONE, kieContainerInstance);

        kieContainerInstance = new KieContainerInstanceImpl(CONTAINER_TWO, KieContainerStatus.STARTED, kieContainerTwo);
        kieServerRegistry.registerContainer(CONTAINER_TWO, kieContainerInstance);

        KeepLatestContainerOnlyPolicy policy = new KeepLatestContainerOnlyPolicy();
        policy.apply(kieServerRegistry, kieServer);

        verify(kieServer).disposeContainer(CONTAINER_ONE);
    }

    @Test
    public void testDoNotDisposeNewerCreatedContainer() {
        KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

        KieContainerInstanceImpl kieContainerInstance = new KieContainerInstanceImpl(CONTAINER_ONE, KieContainerStatus.STARTED, kieContainerOne);
        kieServerRegistry.registerContainer(CONTAINER_ONE, kieContainerInstance);

        kieContainerInstance = new KieContainerInstanceImpl(CONTAINER_TWO, KieContainerStatus.CREATING, kieContainerTwo);
        kieServerRegistry.registerContainer(CONTAINER_TWO, kieContainerInstance);

        KeepLatestContainerOnlyPolicy policy = new KeepLatestContainerOnlyPolicy();
        policy.apply(kieServerRegistry, kieServer);

        verify(kieServer, never()).disposeContainer(anyString());
    }
}

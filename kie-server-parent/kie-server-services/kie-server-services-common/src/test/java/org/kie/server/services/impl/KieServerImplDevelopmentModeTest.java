/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServerExtension;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KieServerImplDevelopmentModeTest extends AbstractKieServerImplTest {

    @Override
    KieServerMode getTestMode() {
        return KieServerMode.DEVELOPMENT;
    }

    @Test
    public void testCreateContainerValidationSNAPSHOT() {
        testCreateContainer(getVersion(KieServerMode.DEVELOPMENT));
    }

    @Test
    public void testCreateContainerValidationNonSNAPSHOT() {
        testCreateContainer(getVersion(KieServerMode.PRODUCTION));
    }

    @Test
    public void testUpdateContainerNonSnapshot() {
        testUpdateContainer(getVersion(KieServerMode.PRODUCTION));
    }

    @Test
    public void testUpdateContainerSNAPSHOT() {
        testUpdateContainer(getVersion(KieServerMode.DEVELOPMENT));
    }

    private void testCreateContainer(String version) {
        String containerId = "container-to-create";

        createEmptyKjar(containerId, version);

        ReleaseId testReleaseId = new ReleaseId(GROUP_ID, containerId, version);

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, testReleaseId);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieContainerResource.setScanner(kieScannerResource);
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, kieContainerResource);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        kieServer.disposeContainer(containerId);
    }

    private void testUpdateContainer(String version) {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(true);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId, version);

        ReleaseId updateReleaseId = new ReleaseId(GROUP_ID, containerId, version);

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, updateReleaseId, true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        verify(extension).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension).prepareContainerUpdate(anyString(), any(), any());
        verify(extension).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }
}

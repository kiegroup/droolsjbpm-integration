/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KieServerImplRegularModeTest extends AbstractKieServerImplTest {

    @Override
    KieServerMode getTestMode() {
        return KieServerMode.REGULAR;
    }

    @Test
    public void testCreateContainerValidationGAVConflict() {
        String containerId = "container-to-create";

        createEmptyKjar(containerId);

        ReleaseId testReleaseId = new ReleaseId(GROUP_ID, containerId, getVersion(KieServerMode.DEVELOPMENT));

        // create the container (provide scanner info as well)
        KieContainerResource kieContainerResource = new KieContainerResource(containerId, testReleaseId);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieContainerResource.setScanner(kieScannerResource);
        ServiceResponse<KieContainerResource> createResponse = kieServer.createContainer(containerId, kieContainerResource);
        Assertions.assertThat(createResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
    }

    @Test
    public void testUpdateContainerWithGAVConflict() {
        KieServerExtension extension = mock(KieServerExtension.class);
        when(extension.isUpdateContainerAllowed(any(), any(), any())).thenReturn(false);
        extensions.add(extension);

        String containerId = "container-to-update";

        startContainerToUpdate(containerId);

        ReleaseId updateReleaseId = new ReleaseId(GROUP_ID, containerId, getVersion(KieServerMode.DEVELOPMENT));

        ServiceResponse<ReleaseId> updateResponse = kieServer.updateContainerReleaseId(containerId, updateReleaseId, true);
        Assertions.assertThat(updateResponse.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);

        verify(extension, never()).isUpdateContainerAllowed(anyString(), any(), any());
        verify(extension, never()).updateContainer(any(), any(), any());

        kieServer.disposeContainer(containerId);
    }
}

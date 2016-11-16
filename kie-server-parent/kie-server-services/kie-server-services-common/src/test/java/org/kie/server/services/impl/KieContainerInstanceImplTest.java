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
import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;

public class KieContainerInstanceImplTest {

    private static final String CONTAINER_ID = "my-container";
    private static final String GROUP_ID = "org.kie.server.test";
    private static final String ARTIFACT_ID = "my-test-artifact";
    private static final String VERSION_100 = "1.0.0.Final";
    private static final String VERSION_101 = "1.0.1.Final";

    private static final ReleaseId RELEASE_ID_100 = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION_100);
    private static final ReleaseId RELEASE_ID_101 = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION_101);

    @Test
    public void testUpdatingOfReleaseId() {
        createEmptyKjar(GROUP_ID, ARTIFACT_ID, VERSION_100);
        createEmptyKjar(GROUP_ID, ARTIFACT_ID, VERSION_101);

        KieServices ks = KieServices.Factory.get();
        InternalKieContainer kieContainer = (InternalKieContainer) ks.newKieContainer(CONTAINER_ID, RELEASE_ID_100);
        KieContainerInstanceImpl containerInstance = new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED, kieContainer);

        Marshaller marshaller = containerInstance.getMarshaller(MarshallingFormat.JAXB);

        // Call getResource() and verify release id
        KieContainerResource containerResource = containerInstance.getResource();
        Assertions.assertThat(containerResource).isNotNull();
        verifyReleaseId(containerResource.getReleaseId(), RELEASE_ID_100);
        verifyReleaseId(containerResource.getResolvedReleaseId(), RELEASE_ID_100);

        // Marshaller is same - no change in release id
        Marshaller updatedMarshaller = containerInstance.getMarshaller(MarshallingFormat.JAXB);
        Assertions.assertThat(updatedMarshaller).isEqualTo(marshaller);

        // Setting kie container with version change
        containerInstance.getKieContainer().updateToVersion(RELEASE_ID_101);

        // Call getResource() and verify release id
        containerResource = containerInstance.getResource();
        Assertions.assertThat(containerResource).isNotNull();
        verifyReleaseId(containerResource.getReleaseId(), RELEASE_ID_101);
        verifyReleaseId(containerResource.getResolvedReleaseId(), RELEASE_ID_101);

        // Marshaller is different - release id was updated
        updatedMarshaller = containerInstance.getMarshaller(MarshallingFormat.JAXB);
        Assertions.assertThat(updatedMarshaller).isNotEqualTo(marshaller);
    }

    private void verifyReleaseId(ReleaseId actualReleaseId, ReleaseId expectedReleaseId) {
        Assertions.assertThat(actualReleaseId).isNotNull();
        Assertions.assertThat(actualReleaseId.getGroupId()).isEqualTo(expectedReleaseId.getGroupId());
        Assertions.assertThat(actualReleaseId.getArtifactId()).isEqualTo(expectedReleaseId.getArtifactId());
        Assertions.assertThat(actualReleaseId.getVersion()).isEqualTo(expectedReleaseId.getVersion());
    }

    private void createEmptyKjar(String groupId, String artifactId, String version) {
        // create empty kjar; content does not matter
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        org.kie.api.builder.ReleaseId releaseId = kieServices.newReleaseId(groupId, artifactId, version);

        kfs.generateAndWritePomXML(releaseId);
        KieModule kieModule = kieServices.newKieBuilder( kfs ).buildAll().getKieModule();
        byte[] pom = kfs.read("pom.xml");
        byte[] jar = ((InternalKieModule)kieModule).getBytes();
        MavenRepository.getMavenRepository().installArtifact(releaseId, jar, pom);
    }
}

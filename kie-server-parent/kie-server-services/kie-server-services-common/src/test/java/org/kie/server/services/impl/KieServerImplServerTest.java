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

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class KieServerImplServerTest {

    private static final String KIE_SERVER_ID = "kie-server-impl-test";
    private static final String KIE_SERVER_REPO_LOCATION = "target/";
    private static final String GROUP_ID = "org.kie.server.test";
    private static final String ARTIFACT_ID = "persist-scanner-state";
    private static final String VERSION = "1.0.0.Final";
    private static final String CONTAINER_ID = "persist-scanner-state";

    @Test
    // https://issues.jboss.org/browse/RHBPMS-4087
    public void testPersistScannerState() {
        KieServerEnvironment.setServerId(KIE_SERVER_ID);
        System.setProperty("org.kie.server.repo", KIE_SERVER_REPO_LOCATION);

        KieServerImpl kieServer = new KieServerImpl();

        // create empty kjar; content does not matter
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        org.kie.api.builder.ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        KieModule kieModule = kieServices.newKieBuilder( kfs ).buildAll().getKieModule();
        MavenRepository.getMavenRepository().installArtifact(releaseId, (InternalKieModule)kieModule, createPomFile());
        kieServices.getRepository().addKieModule(kieModule);

        // create the container and update the
        KieContainerResource kieContainerResource = new KieContainerResource(CONTAINER_ID, new ReleaseId(releaseId));
        kieServer.createContainer(CONTAINER_ID, kieContainerResource);
        KieScannerResource kieScannerResource = new KieScannerResource(KieScannerStatus.STARTED, 20000L);
        kieServer.updateScanner(CONTAINER_ID, kieScannerResource);

        KieServerStateRepository stateRepository = new KieServerStateFileRepository();
        KieServerState state = stateRepository.load(KIE_SERVER_ID);
        Set<KieContainerResource> containers = state.getContainers();
        Assertions.assertThat(containers).hasSize(1);
        KieContainerResource container = containers.iterator().next();
        Assertions.assertThat(container.getScanner()).isEqualTo(kieScannerResource);

        KieScannerResource updatedKieScannerResource = new KieScannerResource(KieScannerStatus.DISPOSED);
        kieServer.updateScanner(CONTAINER_ID, updatedKieScannerResource);

        // create new state repository instance to avoid caching via 'knownStates'
        // this is simulates the server restart (since the status is loaded from filesystem after restart)
        stateRepository = new KieServerStateFileRepository();
        state = stateRepository.load(KIE_SERVER_ID);
        containers = state.getContainers();
        Assertions.assertThat(containers).hasSize(1);
        container = containers.iterator().next();
        Assertions.assertThat(container.getScanner()).isEqualTo(updatedKieScannerResource);
    }

    private File createPomFile() {
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>org.kie.server.test</groupId>\n" +
                "  <artifactId>persist-scanner-state</artifactId>\n" +
                "  <version>1.0.0.Final</version>\n" +
                "  <packaging>pom</packaging>\n" +
                "</project>";
        try {
            File file = new File("target/persist-scanner-state-1.0.0.Final.pom");
            FileUtils.write(file, pomContent);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

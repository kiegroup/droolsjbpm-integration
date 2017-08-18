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

package org.kie.server.integrationtests.drools;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class CombinedUpdateIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ClassLoader classLoader = CombinedUpdateIntegrationTest.class.getClassLoader();

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "update-kjar",
            "1.0.0.Final");
    private static final ReleaseId kjar101 = new ReleaseId("org.kie.server.testing", "update-kjar",
            "1.0.1.Final");
    private static final ReleaseId kjar102 = new ReleaseId("org.kie.server.testing", "update-kjar",
            "1.0.2.Final");

    private static final String CONTAINER_ID = "container-update";

    @BeforeClass
    public static void deployArtifacts() {
        String personClassContent = readFile("Person.java");
        KieServerDeployer.createAndDeployKJar(kjar1, Collections.singletonMap("src/main/java/org/kie/server/testing/Person.java", personClassContent));
        KieServerDeployer.createAndDeployKJar(kjar101, new HashMap<>());
        KieServerDeployer.createAndDeployKJar(kjar102, Collections.singletonMap("src/main/java/org/kie/server/testing/Person.java", personClassContent));
    }

    @After
    public void cleanContainers() {
        disposeAllContainers();
    }

    @Test
    public void testMultipleContainerUpdate() throws Exception {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID, kjar1);
        ServiceResponse<KieContainerResource> createContainer = client.createContainer(CONTAINER_ID, containerResource);
        verifyResourceResult(createContainer);

        ServiceResponse<ReleaseId> updateReleaseId = client.updateReleaseId(CONTAINER_ID, kjar101);
        KieServerAssert.assertSuccess(updateReleaseId);

        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        verifyResourceResult(containerInfo);

        updateReleaseId = client.updateReleaseId(CONTAINER_ID, kjar102);
        KieServerAssert.assertSuccess(updateReleaseId);

        containerInfo = client.getContainerInfo(CONTAINER_ID);
        verifyResourceResult(containerInfo);
    }

    private void verifyResourceResult(ServiceResponse<KieContainerResource> response) {
        KieServerAssert.assertSuccess(response);
        assertThat(response.getResult().getMessages()).as("Shound have one message").hasSize(1);

        Message message = response.getResult().getMessages().get(0);
        assertThat(message.getSeverity()).as("Message should be of type info").isEqualTo(Severity.INFO);
    }

    private static String readFile(String resourceName) {
        try {
            URI resourceUri = classLoader.getResources(resourceName).nextElement().toURI();
            return new String(Files.readAllBytes(Paths.get(resourceUri)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

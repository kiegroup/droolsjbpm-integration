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

package org.kie.server.integrationtests.jbpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

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

public class EmptyContainerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.createAndDeployKJar(releaseId, new HashMap<>());
    }

    @After
    public void cleanContainers() {
        disposeAllContainers();
    }

    @Test
    public void testCreateEmptyContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID, releaseId);
        ServiceResponse<KieContainerResource> createContainer = client.createContainer(CONTAINER_ID, containerResource);
        KieServerAssert.assertSuccess(createContainer);

        List<Message> messages = createContainer.getResult().getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getSeverity()).isEqualTo(Severity.INFO);
    }
}

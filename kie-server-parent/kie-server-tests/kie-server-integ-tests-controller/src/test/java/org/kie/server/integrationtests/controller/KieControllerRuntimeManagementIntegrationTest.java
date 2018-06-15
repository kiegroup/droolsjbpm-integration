/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.controller;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.*;

public abstract class KieControllerRuntimeManagementIntegrationTest<T extends KieServerControllerClientException> extends KieControllerManagementBaseTest {

    private KieServerInfo kieServerInfo;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());
    }

    protected abstract void assertNotFoundException(T exception);

    protected abstract void assertBadRequestException(T exception);

    @Before
    public void getKieServerInfo() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        KieServerAssert.assertSuccess(reply);
        kieServerInfo = reply.getResult();
    }

    @Test
    public void testGetContainersByInstance() throws Exception {
        // Create kie server template connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server template.
        createContainerSpec(serverTemplate,
                            RELEASE_ID,
                            KieContainerStatus.STARTED);
        KieServerSynchronization.waitForContainerWithReleaseId(client,
                                                               RELEASE_ID);

        ServerInstanceKeyList serverInstances = controllerClient.getServerInstances(serverTemplate.getId());
        ServerInstanceKey serverInstance = serverInstances.getServerInstanceKeys()[0];

        ContainerList containers = controllerClient.getContainers(serverInstance);
        assertContainerList(serverTemplate,
                            serverInstance,
                            containers);
    }

    @Test
    public void testGetContainersByTemplate() throws Exception {
        // Create kie server template connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server template.
        ContainerSpec containerSpec = createContainerSpec(serverTemplate, RELEASE_ID, KieContainerStatus.STARTED);
        KieServerSynchronization.waitForContainerWithReleaseId(client, RELEASE_ID);

        ServerInstanceKeyList serverInstances = controllerClient.getServerInstances(serverTemplate.getId());
        ServerInstanceKey serverInstance = serverInstances.getServerInstanceKeys()[0];

        ContainerList containers = controllerClient.getContainers(serverTemplate, containerSpec);
        assertContainerList(serverTemplate,
                            serverInstance,
                            containers);
    }

    protected void assertContainerList(ServerTemplate serverTemplate,
                                       ServerInstanceKey serverInstance,
                                       ContainerList containers) {
        assertThat(containers.getContainers()).hasSize(1);
        Container container = containers.getContainers()[0];
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(container.getContainerSpecId()).isEqualTo(CONTAINER_ID);
            softly.assertThat(container.getContainerName()).isEqualTo(CONTAINER_NAME);
            softly.assertThat(container.getResolvedReleasedId()).isEqualTo(RELEASE_ID);
            softly.assertThat(container.getServerInstanceId()).isEqualTo(serverInstance.getServerInstanceId());
            softly.assertThat(container.getServerTemplateId()).isEqualTo(serverTemplate.getId());
            softly.assertThat(container.getStatus()).isEqualTo(KieContainerStatus.STARTED);
        });
    }

    @Test
    public void testGetContainersFromNotExistingServerInstance() {
        ServerTemplate serverTemplate = createServerTemplate();

        ServerInstanceKey serverInstance = new ServerInstanceKey(serverTemplate.getId(),
                                                                 "not-existing",
                                                                 "not-existing",
                                                                 "not-existing");
        try {
            controllerClient.getContainers(serverInstance);
            fail("Should throw exception about the server instance not existing.");
        } catch (KieServerControllerClientException e) {
            assertNotFoundException((T) e);
            assertThat(e.getMessage()).endsWith("Server template with id " + serverTemplate.getId() +" has no instance with id not-existing");
        }
    }

    protected ServerTemplate createServerTemplate() {
        return createServerTemplate(kieServerInfo.getServerId(),
                                    kieServerInfo.getName(),
                                    kieServerInfo.getLocation());
    }
}
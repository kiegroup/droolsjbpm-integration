/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Configuration;

import org.assertj.core.api.SoftAssertions;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

public abstract class KieControllerManagementBaseTest extends RestOnlyBaseIntegrationTest {

    protected static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0");
    protected static final ReleaseId RELEASE_ID_101 = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.1");
    protected static final ReleaseId RELEASE_ID_LATEST = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "LATEST");

    protected static final String CONTAINER_ID = "kie-concurrent";
    protected static final String CONTAINER_NAME = "containerName";

    protected KieServerControllerClient controllerClient;

    @Before
    public void createControllerClient() {
        final Configuration configuration =
                new ResteasyClientBuilder()
                        .establishConnectionTimeout(10,
                                                    TimeUnit.SECONDS)
                        .socketTimeout(60,
                                       TimeUnit.SECONDS)
                        .getConfiguration();
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newRestClient(TestConfig.getControllerHttpUrl(),
                                                                              null,
                                                                              null,
                                                                              marshallingFormat,
                                                                              configuration);
        } else {
            controllerClient = KieServerControllerClientFactory.newRestClient(TestConfig.getControllerHttpUrl(),
                                                                              TestConfig.getUsername(),
                                                                              TestConfig.getPassword(),
                                                                              marshallingFormat,
                                                                              configuration);
        }
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        disposeAllContainers();
        disposeAllServerInstances();
    }

    @After
    public void closeControllerClient() {
        if (controllerClient != null) {
            try {
                logger.info("Closing Kie Server Management Controller client");
                controllerClient.close();
            } catch (IOException e) {
                logger.error("Error trying to close Kie Server Management Controller Client: {}",
                             e.getMessage(),
                             e);
            }
        }
    }

    protected ServerTemplate createServerTemplate(String id, String name) {
        return createServerTemplate(id, name, null, null, null);
    }

    protected ServerTemplate createServerTemplate(String id, String name, String location) {
        return createServerTemplate(id, name, location, null, null);
    }

    protected ServerTemplate createServerTemplate(String id, String name, String location, KieServerMode mode, List<String> capabilities) {
        ServerTemplate serverTemplate = buildServerTemplate(id, name, location, null, null);
        controllerClient.saveServerTemplate(serverTemplate);
        return serverTemplate;
    }

    protected static ServerTemplate buildServerTemplate(String id, String name, String location, KieServerMode mode, List<String> capabilities) {
        ServerTemplate serverTemplate = new ServerTemplate(id, name);
        if (mode != null) {
            serverTemplate.setMode(mode);
        }

        if (capabilities != null && !capabilities.isEmpty()) {
            serverTemplate.setCapabilities(capabilities);
        }

        if (location != null) {
            serverTemplate.addServerInstance(ModelFactory.newServerInstanceKey(serverTemplate.getId(), location));
        }

        return serverTemplate;
    }

    protected ContainerSpec createContainerSpec(ServerTemplate serverTemplate, ReleaseId releaseId, KieContainerStatus status) {
        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, status, Collections.emptyMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        return containerSpec;
    }

    protected void checkContainer(ContainerSpec container, KieContainerStatus status) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(container).isNotNull();
            softly.assertThat(container.getId()).isEqualTo(CONTAINER_ID);
            softly.assertThat(container.getReleasedId()).isEqualTo(RELEASE_ID);
            softly.assertThat(container.getStatus()).isEqualTo(status);
        });
    }
}

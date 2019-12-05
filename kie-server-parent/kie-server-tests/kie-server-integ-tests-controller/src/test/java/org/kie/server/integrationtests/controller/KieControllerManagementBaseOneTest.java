package org.kie.server.integrationtests.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Configuration;

import org.assertj.core.api.SoftAssertions;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.integrationtests.config.TestConfig;

public abstract class KieControllerManagementBaseOneTest extends RestOnlyBaseIntegrationOneTest {

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
        ServerTemplate serverTemplate = new ServerTemplate(id, name);

        controllerClient.saveServerTemplate(serverTemplate);

        return serverTemplate;
    }

    protected ServerTemplate createServerTemplate(String id, String name, String location) {
        ServerTemplate serverTemplate = new ServerTemplate(id, name);

        serverTemplate.addServerInstance(ModelFactory.newServerInstanceKey(serverTemplate.getId(), location));
        controllerClient.saveServerTemplate(serverTemplate);

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

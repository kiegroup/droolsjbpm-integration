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
package org.kie.server.integrationtests.common.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.ResponseCallback;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.assertj.core.api.Assertions.*;

@Category({JMSOnly.class})
@Ignore
public class ContainerJmsResponseHandlerIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID_1 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static final ReleaseId RELEASE_ID_2 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.1.GA");

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        return new ArrayList<Object[]>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration},
                {MarshallingFormat.JSON, jmsConfiguration},
                {MarshallingFormat.XSTREAM, jmsConfiguration}
        }));
    }

    private KieServicesClient asyncClient;
    private ResponseCallback responseCallback;

    private KieServicesClient fireAndForgetClient;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(RELEASE_ID_1);
        KieServerDeployer.createAndDeployKJar(RELEASE_ID_2);
    }

    @Before
    public void setupClients() throws Exception {
        asyncClient = createDefaultClient();
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()),
                configuration.getMarshallingFormat(), asyncClient.getClassLoader());
        responseCallback = new BlockingResponseCallback(marshaller);
        asyncClient.setResponseHandler(new AsyncResponseHandler(responseCallback));

        fireAndForgetClient = createDefaultClient();
        fireAndForgetClient.setResponseHandler(new FireAndForgetResponseHandler());
    }

    @Before
    public void setupKieServer() throws Exception {
        disposeAllContainers();
    }

    @Test
    public void testContainerWithAsyncResponseHandler() {
        String containerId = "asyncContainer";

        ServiceResponse<?> response = asyncClient.createContainer(containerId, new KieContainerResource(containerId, RELEASE_ID_1));
        assertThat(response).isNull();
        KieContainerResource container = responseCallback.get(KieContainerResource.class);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isEqualTo(containerId);
        assertThat(container.getReleaseId()).isEqualTo(RELEASE_ID_1);

        response = asyncClient.getServerState();
        assertThat(response).isNull();
        KieServerStateInfo serverState = responseCallback.get(KieServerStateInfo.class);
        assertThat(serverState).isNotNull();
        assertThat(serverState.getContainers()).hasSize(1);
        container = serverState.getContainers().iterator().next();
        assertThat(container.getReleaseId()).isEqualTo(RELEASE_ID_1);

        response = asyncClient.updateReleaseId(containerId, RELEASE_ID_2);
        assertThat(response).isNull();
        ReleaseId releaseId = responseCallback.get(ReleaseId.class);
        assertThat(releaseId).isEqualTo(RELEASE_ID_2);

        response = asyncClient.getContainerInfo(containerId);
        assertThat(response).isNull();
        container = responseCallback.get(KieContainerResource.class);
        assertThat(container.getReleaseId()).isEqualTo(RELEASE_ID_2);

        response = asyncClient.disposeContainer(containerId);
        assertThat(response).isNull();
        responseCallback.get(Void.class);

        response = asyncClient.listContainers();
        assertThat(response).isNull();
        KieContainerResourceList containerList = responseCallback.get(KieContainerResourceList.class);
        assertThat(containerList.getContainers()).isNullOrEmpty();
    }

    @Test
    public void testContainerWithFireAndForgetResponseHandler() throws Exception {
        String containerId = "fireAndForgetContainer";

        ServiceResponse<?> response = fireAndForgetClient.createContainer(containerId, new KieContainerResource(containerId, RELEASE_ID_1));
        assertThat(response).isNull();
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        response = fireAndForgetClient.updateReleaseId(containerId, RELEASE_ID_2);
        assertThat(response).isNull();
        KieServerSynchronization.waitForContainerWithReleaseId(client, RELEASE_ID_2);

        response = fireAndForgetClient.disposeContainer(containerId);
        assertThat(response).isNull();
        KieServerSynchronization.waitForKieServerSynchronization(client, 0);
    }

    @Test
    public void testScannerWithAsyncResponseHandler() {
        String containerId = "asyncScannerContainer";
        createContainer(containerId, RELEASE_ID_1);

        ServiceResponse<?> response = asyncClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.STARTED, 10000L));
        assertThat(response).isNull();
        KieScannerResource scanner = responseCallback.get(KieScannerResource.class);
        assertThat(scanner.getStatus()).isEqualTo(KieScannerStatus.STARTED);

        response = asyncClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.STOPPED, 10000L));
        assertThat(response).isNull();
        scanner = responseCallback.get(KieScannerResource.class);
        assertThat(scanner.getStatus()).isEqualTo(KieScannerStatus.STOPPED);

        response = asyncClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        assertThat(response).isNull();
        scanner = responseCallback.get(KieScannerResource.class);
        assertThat(scanner.getStatus()).isEqualTo(KieScannerStatus.DISPOSED);
    }

    @Test
    public void testScannerWithFireAndForgetResponseHandler() throws Exception {
        String containerId = "fireAndForgetScannerContainer";
        createContainer(containerId, RELEASE_ID_1);

        ServiceResponse<?> response = fireAndForgetClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.STARTED, 10000L));
        assertThat(response).isNull();
        KieServerSynchronization.waitForContainerWithScannerStatus(client, KieScannerStatus.STARTED);

        response = asyncClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.STOPPED, 10000L));
        assertThat(response).isNull();
        KieServerSynchronization.waitForContainerWithScannerStatus(client, KieScannerStatus.STOPPED);

        response = asyncClient.updateScanner(containerId, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        assertThat(response).isNull();
        KieServerSynchronization.waitForContainerWithScannerStatus(client, KieScannerStatus.DISPOSED);
    }

}

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

package org.kie.server.router.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.After;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieContainerInstanceImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;

public class KieServerRouterEventListenerRetryTest {

    private WireMockServer wireMockServer = null;
    private KieServerRouterEventListener client = null;
    private KieContainerInstance containerInstance = new KieContainerInstanceImpl("test-container", KieContainerStatus.STARTED, null, new ReleaseId("org.test", "test-kjar", "1.0"));

    public static int findFreePort() {
        int port = 0;
        try {
            ServerSocket server = new ServerSocket( 0 );
            port = server.getLocalPort();
            server.close();
        } catch ( IOException e ) {
            // failed to dynamically allocate port, try to use hard coded one
            port = 9789;
        }
        return port;
    }

    @After
    public void cleanup() {
        if (client != null) {
            client.close();
        }
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test(timeout = 10000)
    public void testRouterContainerStartedRetry() throws Exception {
        final CountDownLatch failureLatch = new CountDownLatch(4);
        final CountDownLatch successLatch = new CountDownLatch(2);

        int routerPort = findFreePort();

        client = new KieServerRouterEventListener("test-server", "http://localhost:8080/kie-server", "http://localhost:" + routerPort, 2);
        client.setObserver(new KieServerRouterEventListener.RouterConnectionObserver(){
            @Override
            public void onSuccess(String url) {
                successLatch.countDown();
            }

            @Override
            public void onFailure(String url) {
                failureLatch.countDown();
            }
        });
        wireMockServer = createMockServer(routerPort, "/admin/add");

        client.afterContainerStarted(null, containerInstance);
        failureLatch.await();

        wireMockServer.start();
        successLatch.await();

        wireMockServer.verify(2, postRequestedFor(urlEqualTo("/admin/add")));
    }

    @Test(timeout = 10000)
    public void testRouterContainerStoppedRetry() throws Exception {
        final CountDownLatch failureLatch = new CountDownLatch(4);
        final CountDownLatch successLatch = new CountDownLatch(2);

        int routerPort = findFreePort();

        client = new KieServerRouterEventListener("test-server", "http://localhost:8080/kie-server", "http://localhost:" + routerPort, 2);
        client.setObserver(new KieServerRouterEventListener.RouterConnectionObserver(){
            @Override
            public void onSuccess(String url) {
                successLatch.countDown();
            }

            @Override
            public void onFailure(String url) {
                failureLatch.countDown();
            }
        });
        wireMockServer = createMockServer(routerPort, "/admin/remove");

        client.afterContainerStopped(null, containerInstance);
        failureLatch.await();

        wireMockServer.start();
        successLatch.await();

        wireMockServer.verify(2, postRequestedFor(urlEqualTo("/admin/remove")));
    }

    @Test(timeout = 10000)
    public void testRouterServerStoppedRetry() throws Exception {
        final CountDownLatch failureLatch = new CountDownLatch(2);
        final CountDownLatch successLatch = new CountDownLatch(2);

        int routerPort = findFreePort();

        client = new KieServerRouterEventListener("test-server", "http://localhost:8080/kie-server", "http://localhost:" + routerPort, 2);
        client.setObserver(new KieServerRouterEventListener.RouterConnectionObserver(){
            @Override
            public void onSuccess(String url) {
                successLatch.countDown();
            }

            @Override
            public void onFailure(String url) {
                failureLatch.countDown();
            }
        });
        wireMockServer = createMockServer(routerPort, "/admin/remove");

        client.beforeServerStopped(new KieServer() {
            @Override
            public ServiceResponse<KieServerInfo> getInfo() {
                return null;
            }

            @Override
            public ServiceResponse<KieContainerResource> createContainer(String containerId, KieContainerResource container) {
                return null;
            }

            @Override
            public ServiceResponse<KieContainerResourceList> listContainers(KieContainerResourceFilter containerFilter) {
                KieContainerResource containerResource = new KieContainerResource(containerInstance.getContainerId(), containerInstance.getResource().getReleaseId(), containerInstance.getStatus());

                List<KieContainerResource> containers = new ArrayList<KieContainerResource>();
                containers.add(containerResource);
                KieContainerResourceList list = new KieContainerResourceList(containers);

                return new ServiceResponse<KieContainerResourceList>(ServiceResponse.ResponseType.SUCCESS, "", list);
            }

            @Override
            public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
                return null;
            }

            @Override
            public ServiceResponse<Void> disposeContainer(String containerId) {
                return null;
            }

            @Override
            public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
                return null;
            }

            @Override
            public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
                return null;
            }

            @Override
            public ServiceResponse<ReleaseId> getContainerReleaseId(String id) {
                return null;
            }

            @Override
            public ServiceResponse<ReleaseId> updateContainerReleaseId(String id, ReleaseId releaseId) {
                return null;
            }

            @Override
            public ServiceResponse<KieServerStateInfo> getServerState() {
                return null;
            }

            @Override
            public void addServerMessage(Message message) {

            }

            @Override
            public void addContainerMessage(String containerId, Message message) {

            }
        });
        failureLatch.await();

        wireMockServer.start();
        boolean met = successLatch.await(4L, TimeUnit.SECONDS);
        assertFalse("On success should not be invoked", met);

        wireMockServer.verify(0, postRequestedFor(urlEqualTo("/admin/remove")));
    }

    protected WireMockServer createMockServer(int port, String url) {
        WireMockServer wireMockServer = new WireMockServer(port);
        wireMockServer.stubFor(post(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        return wireMockServer;
    }

}

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

package org.kie.server.integrationtests.router.rest;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

public class KieServerRouterAuthTest {

    private KieServerRouter router;
    private File repository;

    private static String serverUrl;

    @Before
    public void startStandaloneRouter() {
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());
        System.setProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED, "true");
        System.setProperty(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER, "mock");


        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter(true, "mock");
        router.start("localhost", port);

        serverUrl = "http://localhost:" + port;
    }

    @After
    public void stopStandaloneRouter() {
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        System.clearProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED);
        System.clearProperty(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
    }

    @Test
    public void testValidAuthMgmtKieServerRouter() throws Exception {
        try (KieServerRouterClient routerClient = new KieServerRouterClient(serverUrl)) {
            routerClient.setCredentials("mockUser", "mockPassword");
            Configuration configuration = routerClient.getRouterConfig();
            Assert.assertNotNull(configuration);
        }
    }

    @Test
    public void testInvalidAuthMgmtKieServerRouter() throws Exception {
        Assertions.assertThatExceptionOfType(KieServerControllerHTTPClientException.class).isThrownBy(
                              () -> {
                                  try (KieServerRouterClient routerClient = new KieServerRouterClient(serverUrl)) {
                                      routerClient.setCredentials("invalidUser", "mockPassword");
                                                                                                              routerClient.getRouterConfig();
                                  }
                              }
        );
    }

    private static int allocatePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            return 9783;
        }
    }
}

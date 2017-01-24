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

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.QUERY_URI;
import static org.kie.server.api.rest.RestURI.build;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

public class KieServerRouterRestIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static KieServerRouter router;
    private static File repository;

    private static String serverUrl;

    @BeforeClass
    public static void startStandaloneRouter(){
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());

        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter();
        router.start("localhost", port);

        serverUrl = "http://localhost:" + port;
    }

    @AfterClass
    public static void stopStandaloneRouter(){
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
    }

    @Test
    public void testGetProcessInstancesStandaloneKieServerRouter() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, "some-container");
        valuesMap.put(PROCESS_ID, "some-process");

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(build(serverUrl, QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap));
            logger.info( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
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

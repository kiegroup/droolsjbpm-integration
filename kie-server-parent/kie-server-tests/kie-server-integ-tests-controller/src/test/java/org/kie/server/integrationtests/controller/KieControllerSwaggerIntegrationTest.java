/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import static org.junit.Assert.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.kie.server.integrationtests.config.TestConfig;


import static org.assertj.core.api.Assertions.assertThat;

public class KieControllerSwaggerIntegrationTest extends KieControllerManagementBaseTest {

    @Test
    public void testSwaggerDocs() throws Exception {
        Client httpClient = ClientBuilder.newClient();
        
        String docsUri = TestConfig.getControllerHttpUrl() + "/../../docs/";
        WebTarget clientRequest = httpClient.target(docsUri);
        Response response = clientRequest.request().get();
        
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        String html = response.readEntity(String.class);
        
        assertThat(Jsoup.parse(html).title()).isEqualTo("Controller Documentation");
     }
}

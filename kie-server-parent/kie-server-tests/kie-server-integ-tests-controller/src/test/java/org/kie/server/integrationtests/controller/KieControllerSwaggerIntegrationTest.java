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

import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.kie.server.integrationtests.config.TestConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class KieControllerSwaggerIntegrationTest extends KieControllerManagementBaseTest {

    Client httpClient = ClientBuilder.newClient();
    
    @Test
    public void testSwaggerDocs() throws Exception {
        String html = invokeGet(getContextRoot()+"docs/");
        
        assertThat(Jsoup.parse(html).title()).isIn("Controller Documentation", "Business Central Documentation");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSwaggerJson() throws Exception {
        String responseStr = invokeGet(getContextRoot()+"rest/swagger.json");
        
        ObjectMapper om = new ObjectMapper();
        HashMap<String, Object> hm = (HashMap<String, Object>) om.readValue(responseStr, HashMap.class);
        assertEquals("2.0", hm.get("swagger"));
     }

    protected String getContextRoot() {
        //Navigate to parent path twice to get context root
        String url = TestConfig.getControllerHttpUrl();
        int pos = url.length();
        for (int i = 0; i < 2; i++) {
            pos = url.lastIndexOf('/', pos - 1);
        }
        return url.substring(0, pos + 1);
    }
    
    protected String invokeGet(String docsUri) {
        WebTarget clientRequest = httpClient.target(docsUri);
        Response response = clientRequest.request().get();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        return response.readEntity(String.class);
    }
}

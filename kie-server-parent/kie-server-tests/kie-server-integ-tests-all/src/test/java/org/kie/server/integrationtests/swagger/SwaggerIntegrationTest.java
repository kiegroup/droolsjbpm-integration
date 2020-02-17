/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.swagger;

import java.util.HashMap;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SwaggerIntegrationTest {

    private static Client httpClient;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        httpClient = ClientBuilder.newClient();
    }

    @AfterClass
    public static void closeHttpClient() {
        if (httpClient != null) {
            httpClient.close();
            httpClient = null;
        }
    }

    @Test
    public void testSwaggerDocs() throws Exception {
        String html = invokeGet(getContextRoot()+"docs/");
        
        assertThat(Jsoup.parse(html).title()).isEqualTo("Execution Server Documentation");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSwaggerJson() throws Exception {
        String responseStr = invokeGet(getContextRoot()+"services/rest/server/swagger.json");
        
        ObjectMapper om = new ObjectMapper();
        HashMap<String, Object> hm = (HashMap<String, Object>) om.readValue(responseStr, HashMap.class);
        assertNotNull(hm.get("swagger"));
        assertNotNull(hm.get("info"));
        assertEquals("KIE Server", ((HashMap<String, Object>) hm.get("info")).get("title"));
     }

    protected String getContextRoot() {
        //Navigate to parent path 3 times to get context root
        String url = TestConfig.getKieServerHttpUrl();
        int pos = url.length();
        for (int i = 0; i < 3; i++) {
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

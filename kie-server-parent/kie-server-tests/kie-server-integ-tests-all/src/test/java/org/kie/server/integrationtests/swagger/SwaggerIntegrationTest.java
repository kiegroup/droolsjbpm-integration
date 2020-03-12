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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SwaggerIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(SwaggerIntegrationTest.class);
    private static Client httpClient;
    private String responseStr;

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
        Response response = invokeGet(getContextRoot(3)+"docs/");
        
        if (response.getStatus()!=200) {
            //Springboot Swagger Docs is located in other URL
            response = invokeGet(getContextRoot(1)+"api-docs?url="+getContextRoot(1)+"swagger.json");
        }

        assertResponse(response);
        responseStr = response.readEntity(String.class);
        response.close();
        assertThat(Jsoup.parse(responseStr).title()).isIn("Execution Server Documentation", "Swagger UI");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSwaggerJson() throws Exception {
        Response response = invokeGet(TestConfig.getKieServerHttpUrl()+"/swagger.json");
        assertResponse(response);
        responseStr = response.readEntity(String.class);
        response.close();

        ObjectMapper om = new ObjectMapper();
        HashMap<String, Object> hm = (HashMap<String, Object>) om.readValue(responseStr, HashMap.class);
        assertNotNull(hm.get("swagger"));
        assertNotNull(hm.get("info"));
        assertEquals("KIE Server", ((HashMap<String, Object>) hm.get("info")).get("title"));
     }

    protected String getContextRoot(int foldersUp) {
        //Navigate to parent path N folders up to get context root
        String url = TestConfig.getKieServerHttpUrl();
        int pos = url.length();
        for (int i = 0; i < foldersUp; i++) {
            pos = url.lastIndexOf('/', pos - 1);
        }
        return url.substring(0, pos + 1);
    }

    protected Response invokeGet(String docsUri) {
        logger.debug("[GET] " + docsUri);
        WebTarget clientRequest = httpClient.target(docsUri);
        return clientRequest.request().get();
    }

    private void assertResponse(Response response) {
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }
}

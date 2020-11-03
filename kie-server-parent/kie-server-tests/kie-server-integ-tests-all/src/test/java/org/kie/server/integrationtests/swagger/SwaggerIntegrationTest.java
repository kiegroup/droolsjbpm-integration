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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SwaggerIntegrationTest {

    protected static final List<String> validStatusGet = Arrays.asList(Integer.toString(OK.getStatusCode()));
    
    protected static final List<String> validStatusPost = Arrays.asList(Integer.toString(OK.getStatusCode()),
                                                                        Integer.toString(CREATED.getStatusCode()));
    
    private static Logger logger = LoggerFactory.getLogger(SwaggerIntegrationTest.class);
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
        Response response = invokeGet(getContextRoot(3)+"docs/");
        
        if (response.getStatus()!=200) {
            //Springboot Swagger Docs is located in other URL
            response = invokeGet(getContextRoot(1)+"api-docs?url="+getContextRoot(1)+"swagger.json");
        }

        assertResponse(response);
        String responseStr = response.readEntity(String.class);
        response.close();
        assertThat(Jsoup.parse(responseStr).title()).isIn("Execution Server Documentation", "Swagger UI");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSwaggerJson() throws Exception {
        String swaggerStr = getSwaggerJson();
        
        ObjectMapper om = new ObjectMapper();
        HashMap<String, Object> hm = (HashMap<String, Object>) om.readValue(swaggerStr, HashMap.class);
        assertNotNull(hm.get("swagger"));
        assertNotNull(hm.get("info"));
        assertEquals("KIE Server", ((HashMap<String, Object>) hm.get("info")).get("title"));
     }
    
    @Test
    public void testSwaggerJsonContainsSchemaInResponse() {
        String swaggerStr = getSwaggerJson();
        
        Swagger swagger = new SwaggerParser().parse(swaggerStr);
        
        swagger.getPaths().forEach((key, item) -> {
            assertNonNullSchema(item.getGet(), validStatusGet, key);
            assertNonNullSchema(item.getPost(), validStatusPost, key);
        });
    }
    
    protected String getSwaggerJson() {
        Response response = invokeGet(TestConfig.getKieServerHttpUrl()+"/swagger.json");
        assertResponse(response);
        String responseStr = response.readEntity(String.class);
        response.close();
        return responseStr;
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
    
    private void assertNonNullSchema(Operation operation, List<String> validStatus, String path) {
        if (operation != null && operation.getResponses() != null) {
            operation.getResponses().entrySet().stream()
            .filter(e -> validStatus.contains(e.getKey()))
            .map(m -> m.getValue().getResponseSchema())
            .forEach(r-> assertNotNull("Path ["+path+"] should have a schema in the response, check swagger annotations", r));
        }
    }
}

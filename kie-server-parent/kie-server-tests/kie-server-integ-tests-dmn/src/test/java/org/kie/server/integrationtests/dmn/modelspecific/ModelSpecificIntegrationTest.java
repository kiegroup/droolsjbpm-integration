/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.dmn.modelspecific;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test performs basics integrity check for the DMN model-specific endpoints, using the simple DMN models contained in `ms-endpoints` kjar.
 *  - show test strategy applied to previously existing endpoints (non model-specific)
 *  - basic consistency test per each endpoint
 *  - demonstrate error headers for non dmnresult endpoint
 *  - demonstrate decision service coercion as specified in requirements, for non dmn result endpoint
 *  - validate OAS produced for the containerId
 *  - demonstrate model-specific endpoints are consistent with InputData definitions, such as JSON date/time.
 */
public class ModelSpecificIntegrationTest extends KieServerBaseIntegrationTest {

    private static final String X_KOGITO_DECISION_MESSAGES = "X-Kogito-decision-messages";

    private static final Logger LOG = LoggerFactory.getLogger(ModelSpecificIntegrationTest.class);

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "ms-endpoints", "1.0.0.Final");

    private static final String CONTAINER_ID = "ms-endpoints";
    private static final String MODEL_NAME = "input-data-string";
    private static final String MODEL_NAMESPACE = "https://github.com/kiegroup/kie-dmn/input-data-string";

    private static final ObjectMapper mapper = new ObjectMapper();

    private CloseableHttpClient httpClient;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/ms-endpoints");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, kjar1);

        // the new model-specific endpoint do not rely on the Kie-Server-Client Java API, hence using standard HttpClient
        int timeout = 3;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    @After
    public void disposeHttpClient() throws Exception {
        httpClient.close();
    }

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        return null; // intentional, as the new model-specific endpoint do not rely on the Kie-Server-Client Java API.
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // no extra classes.
    }

    private String baseContainerPath() {
        return TestConfig.getKieServerHttpUrl() + "/containers/" + CONTAINER_ID;
    }

    private HttpResponse makePOST(String pathInContainer, String payload) throws Exception {
        return rawPOST(baseContainerPath() + pathInContainer, payload);
    }

    private HttpResponse rawPOST(String path, String payload) throws Exception {
        HttpPost httpPost = new HttpPost(path);
        httpPost.setEntity(new StringEntity(payload));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(TestConfig.getUsername(), TestConfig.getPassword());
        httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

        HttpResponse response = httpClient.execute(httpPost);
        return response;
    }

    private HttpResponse makeGET(String pathInContainer, String accept) throws Exception {
        return rawGET(baseContainerPath() + pathInContainer, accept);
    }

    private HttpResponse rawGET(String path, String accept) throws Exception {
        HttpGet httpGet = new HttpGet(path);
        httpGet.setHeader("Accept", accept);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(TestConfig.getUsername(), TestConfig.getPassword());
        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet, null));

        HttpResponse response = httpClient.execute(httpGet);
        return response;
    }

    private String responseAsString(HttpResponse response) throws Exception {
        String asString = EntityUtils.toString(response.getEntity());
        LOG.debug("Response:\n\n{}\n\n", asString);
        return asString;
    }

    /**
     * Testing the PREVIOUS endpoint, but manually with this style of tests.
     */
    @Test
    public void test_previousEndpoints_evaluateAll() throws Exception {
        HttpResponse response = makePOST("/dmn",
                                         "{ \"model-name\": \"" + MODEL_NAME + "\", \"model-namespace\": \"" + MODEL_NAMESPACE + "\", \"dmn-context\":  { \"Full Name\" : \"John Doe\" } }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).extracting("result").extracting("dmn-evaluation-result").extracting("dmn-context").extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    /* --- NEW ENDPOINTs tests: --- */
    @Test
    public void test_getModel() throws Exception {
        HttpResponse response = makeGET("/dmn/models/" + MODEL_NAME,
                                        "application/xml");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).doesNotContain("Hello ");
    }

    @Test
    public void test_getModel_ERR_noContainer() throws Exception {
        HttpResponse response = rawGET(TestConfig.getKieServerHttpUrl() + "/containers/" + "unexistent" + "/dmn/models/" + MODEL_NAME,
                                       "application/xml");
        assertThat(response.getStatusLine().getStatusCode(), is(500));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("Container 'unexistent' is not instantiated or cannot find container for alias 'unexistent'");
    }

    @Test
    public void test_getModel_ERR_noModel() throws Exception {
        HttpResponse response = makeGET("/dmn/models/" + "unexistent",
                                        "application/xml");
        assertThat(response.getStatusLine().getStatusCode(), is(404));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("No model identifies with modelId: unexistent");
    }

    @Test
    public void test_evaluateModel() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME,
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    @Test
    public void test_evaluateModel_ERR_noContainer() throws Exception {
        HttpResponse response = rawPOST(TestConfig.getKieServerHttpUrl() + "/containers/" + "unexistent" + "/dmn/models/" + MODEL_NAME,
                                        "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(500));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("Container 'unexistent' is not instantiated or cannot find container for alias 'unexistent'");
    }

    @Test
    public void test_evaluateModel_ERR_noModel() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + "unexistent",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(404));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("No model identifies with modelId: unexistent");
    }

    @Test
    public void test_evaluateModel_ERR() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME,
                                         "{ \"asd\" : 123 }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        Header[] headers = response.getHeaders(X_KOGITO_DECISION_MESSAGES);
        Assertions.assertThat(headers).hasSize(1);
        Assertions.assertThat(headers[0].getValue()).contains("Required dependency 'Full Name' not found");
    }

    @Test
    public void test_evaluateModelAsDmnResult() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME + "/dmnresult",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).extracting("dmnContext").extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    @Test
    public void test_evaluateModelDS() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME + "/dsGreetings",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).isEqualTo("Hello John Doe");
    }

    @Test
    public void test_evaluateModelDS_ERR_noDS() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME + "/unexistent",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(404));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("No decisionService found: unexistent");
    }

    @Test
    public void test_evaluateModelDS_2outputs() throws Exception {
        HttpResponse response = makePOST("/dmn/models/multiple-greetings-ds/ds1",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).asInstanceOf(InstanceOfAssertFactories.MAP).containsKeys("formal", "less formal");
    }

    @Test
    public void test_evaluateModelDSAsDmnResult() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME + "/dsGreetings/dmnresult",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).extracting("dmnContext").extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    @Test
    public void allTypes() {
        final String JSON = "{\n" +
                            "    \"InputBoolean\": true,\n" +
                            "    \"InputDate\": \"2020-04-02\",\n" +
                            "    \"InputDTDuration\": \"P1D\",\n" +
                            "    \"InputDateAndTime\": \"2020-04-02T09:00:00\",\n" +
                            "    \"InputNumber\": 1,\n" +
                            "    \"InputString\": \"John Doe\",\n" +
                            "    \"InputTime\": \"09:00\",\n" +
                            "    \"InputYMDuration\": \"P1M\"\n" +
                            "}";
        RestAssured.given()
                   .auth().preemptive().basic(TestConfig.getUsername(), TestConfig.getPassword())
                   .contentType(ContentType.JSON)
                   .accept(ContentType.JSON)
                   .body(JSON)
                   .post(baseContainerPath() + "/dmn/models/" + "OneOfEachType")
                   .then()
                   .statusCode(200)
                   .body("DecisionBoolean", is(Boolean.FALSE))
                   .body("DecisionDate", is("2020-04-03")) // as JSON is not schema aware, here we assert the RAW string
                   .body("DecisionDTDuration", is("PT48H"))
                   .body("DecisionDateAndTime", is("2020-04-02T10:00:00"))
                   .body("DecisionNumber", is(2))
                   .body("DecisionString", is("Hello, John Doe"))
                   .body("DecisionTime", is("10:00:00"))
                   .body("DecisionYMDuration", is("P2M"));
    }

    @Test
    public void test_getOAS() throws Exception {
        HttpResponse response = makeGET("/dmn/openapi",
                                        "application/json");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String responseAsString = responseAsString(response);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(responseAsString, null, parseOptions);

        assertThat(result.getMessages()).isEmpty();
        assertOnSwaggerResults(result);
    }

    @Test
    public void test_getOAS_ERR_noContainer() throws Exception {
        HttpResponse response = rawGET(TestConfig.getKieServerHttpUrl() + "/containers/" + "unexistent" + "/dmn/openapi",
                                       "application/json");
        assertThat(response.getStatusLine().getStatusCode(), is(500));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).contains("Container 'unexistent' is not instantiated or cannot find container for alias 'unexistent'");
    }

    private static void assertOnSwaggerResults(SwaggerParseResult result) {
        OpenAPI openAPI = result.getOpenAPI();
        PathItem p1 = openAPI.getPaths().get("/server/containers/ms-endpoints/dmn/models/" + MODEL_NAME);
        assertThat(p1).isNotNull();
        assertThat(p1.getGet()).isNotNull();
        assertThat(p1.getPost()).isNotNull();
        PathItem p2 = openAPI.getPaths().get("/server/containers/ms-endpoints/dmn/models/" + MODEL_NAME + "/dmnresult");
        assertThat(p2).isNotNull();
        assertThat(p2.getPost()).isNotNull(); // only POST for ../dmnresult expected.
    }

    @Test
    public void test_getOASYAML() throws Exception {
        HttpResponse response = makeGET("/dmn/openapi.yaml",
                                        "application/yaml");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        
        // assertions on the OAS/swagger definitions were maintained only for the JSON variant,
        // to avoid issues with underlying YAML parsing library.
    }

}
/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ModelSpecificIntegrationTest extends KieServerBaseIntegrationTest {

    private static final String X_KOGITO_DECISION_MESSAGES = "X-Kogito-decision-messages";

    private static final Logger LOG = LoggerFactory.getLogger(ModelSpecificIntegrationTest.class);

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "ms-endpoints", "1.0.0.Final");

    private static final String CONTAINER_ID = "ms-endpoints";
    private static final String MODEL_NAME = "input-data-string";
    private static final String MODEL_NAMESPACE = "https://github.com/kiegroup/kie-dmn/input-data-string";

    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/ms-endpoints");
    }
    
    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, kjar1);
    }

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        return null;
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // no extra classes.
    }

    private HttpResponse makePOST(String pathInContainer, String payload) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(TestConfig.getKieServerHttpUrl() + "/containers/" + CONTAINER_ID + pathInContainer);

        httpPost.setEntity(new StringEntity(payload));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(TestConfig.getUsername(), TestConfig.getPassword());
        httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

        HttpResponse response = client.execute(httpPost);
        client.close();
        return response;
    }

    private HttpResponse makeGET(String pathInContainer, String accept) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(TestConfig.getKieServerHttpUrl() + "/containers/" + CONTAINER_ID + pathInContainer);

        httpGet.setHeader("Accept", accept);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(TestConfig.getUsername(), TestConfig.getPassword());
        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet, null));

        HttpResponse response = client.execute(httpGet);
        client.close();
        return response;
    }

    private String responseAsString(HttpResponse response) throws Exception {
        String asString = EntityUtils.toString(response.getEntity());
        LOG.info("Response:\n\n{}\n\n", asString);
        return asString;
    }

    @Test
    public void test_previousEndpoints_evaluateAll() throws Exception {
        HttpResponse response = makePOST("/dmn",
                                         "{ \"model-name\": \"" + MODEL_NAME + "\", \"model-namespace\": \"" + MODEL_NAMESPACE + "\", \"dmn-context\":  { \"Full Name\" : \"John Doe\" } }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        Assertions.assertThat(readValue).extracting("result").extracting("dmn-evaluation-result").extracting("dmn-context").extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    /*    
    
            mvn clean install -DskipTests -pl :kie-server-api,:kie-server-rest-dmn,:kie-server-services-dmn,:kie-server-wars,:kie-server,:kie-server-distribution
    
    */
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
    public void test_getModel() throws Exception {
        HttpResponse response = makeGET("/dmn/models/" + MODEL_NAME,
                                        "application/xml");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String responseAsString = responseAsString(response);
        Assertions.assertThat(responseAsString).doesNotContain("Hello ");
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

}
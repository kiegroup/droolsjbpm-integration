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

package org.kie.server.integrationtests.dmn;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
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

    private static final Logger LOG = LoggerFactory.getLogger(ModelSpecificIntegrationTest.class);

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "input-data-string",
            "1.0.0.Final" );

    private static final String CONTAINER_ID  = "input-data-string";
    
    private static final String MODEL_NAMESPACE = "https://github.com/kiegroup/kie-dmn/input-data-string";
    private static final String MODEL_NAME = "input-data-string";

    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/input-data-string");
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

    private String responseAsString(HttpResponse response) throws Exception {
        String jsonString = EntityUtils.toString(response.getEntity());
        LOG.info("Response: {}", jsonString);
        return jsonString;
    }

    @Test
    public void test_previousEndpoints_evaluateAll() throws Exception {
        HttpResponse response = makePOST("/dmn",
                                         "{ \"dmn-context\":  { \"Full Name\" : \"John Doe\" } }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> readValue = mapper.readValue(jsonString, Map.class);
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
        @SuppressWarnings("unchecked")
        Map<String, Object> readValue = mapper.readValue(jsonString, Map.class);
        Assertions.assertThat(readValue).extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

    @Test
    public void test_evaluateModelAsDmnResult() throws Exception {
        HttpResponse response = makePOST("/dmn/models/" + MODEL_NAME + "/dmnresult",
                                         "{ \"Full Name\" : \"John Doe\" }");
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> readValue = mapper.readValue(jsonString, Map.class);
        Assertions.assertThat(readValue).extracting("dmnContext").extracting("Greeting Message").isEqualTo("Hello John Doe");
    }

}
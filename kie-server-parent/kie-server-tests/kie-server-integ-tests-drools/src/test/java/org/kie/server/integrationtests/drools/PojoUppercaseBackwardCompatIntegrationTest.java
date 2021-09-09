package org.kie.server.integrationtests.drools;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
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

public class PojoUppercaseBackwardCompatIntegrationTest extends KieServerBaseIntegrationTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(PojoUppercaseBackwardCompatIntegrationTest.class);

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "pojo-uppercase", "1.0.0.Final");

    private static ClassLoader kjarClassLoader;

    private static final String CONTAINER_ID = "pojouppercase";
    
    /**
     * DROOLS-6592 one specific ad-hoc test to ensure backward compatibility, uses directly a snapshot of the payload for JSON.
     */
    private CloseableHttpClient httpClient;
    /**
     * Intentionally neutral (no modules) mapper.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/pojo-uppercase");
        
        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }
    
    @Before
    public void cleanupContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
        
        int timeout = 3;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }
    
    @After
    public void disposeHttpClient() throws Exception {
        httpClient.close();
    }
    
    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        // nothing needed on this test.
    }
    
    private String baseContainerPath() {
        return TestConfig.getKieServerHttpUrl() + "/containers/instances/" + CONTAINER_ID;
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
    
    private String responseAsString(HttpResponse response) throws Exception {
        String asString = EntityUtils.toString(response.getEntity());
        LOG.info("Response:\n\n{}\n\n", asString);
        return asString;
    }
    
    /*
     * Backward compatible Response payload snapshot
     * '{
  "type" : "SUCCESS",
  "msg" : "Container pojouppercase successfully called.",
  "result" : {
    "execution-results" : {
      "results" : [ {
        "value" : {"com.myspace.demo20210908applicant.Application":{
  "family" : [ {
    "applicantID" : 1,
    "name" : "Jim",
    "address" : "444 St",
    "ssn" : null,
    "uscitizen" : false
  } ],
  "programName" : "pojouppercase"
}},
        "key" : "application"
      }, {
        "value" : 1,
        "key" : "numberOfFiredRules"
      } ],
      "facts" : [ {
        "value" : {"org.drools.core.common.DefaultFactHandle":{
  "external-form" : "0:1:1715783674:1715783674:1:DEFAULT:NON_TRAIT:com.myspace.demo20210908applicant.Application"
}},
        "key" : "application"
      } ]
    }
  }
}' 
     */
    @Test
    public void testBackwardCompatibility() throws Exception {
        // DROOLS-6592 Backward compatibility for Kie Server Marshalling changes Pojo field UpperCase begin and JSON
        final String RHDM7110_JSON = "{\n" + 
                "  \"lookup\" : \"ksession1\",\n" + 
                "  \"commands\" : [ {\n" + 
                "    \"insert\" : {\n" + 
                "      \"object\" : {\"com.myspace.demo20210908applicant.Application\":{\n" + 
                "  \"family\" : [ {\n" + 
                "    \"applicantID\" : 1,\n" + 
                "    \"name\" : \"Jim\",\n" + 
                "    \"address\" : \"444 St\",\n" + 
                "    \"ssn\" : \"SSNJim\",\n" + 
                "    \"uscitizen\" : true\n" + 
                "  } ],\n" + 
                "  \"programName\" : \"pojouppercase\"\n" + 
                "}},\n" + 
                "      \"out-identifier\" : \"application\",\n" + 
                "      \"return-object\" : true,\n" + 
                "      \"entry-point\" : \"DEFAULT\",\n" + 
                "      \"disconnected\" : false\n" + 
                "    }\n" + 
                "  }, {\n" + 
                "    \"fire-all-rules\" : {\n" + 
                "      \"max\" : -1,\n" + 
                "      \"out-identifier\" : \"numberOfFiredRules\"\n" + 
                "    }\n" + 
                "  } ]\n" + 
                "}";
        HttpResponse response = makePOST("", RHDM7110_JSON);
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        String jsonString = responseAsString(response);
        Object readValue = mapper.readValue(jsonString, Object.class);
        assertThat(readValue).extracting("result")
            .extracting("execution-results")
            .extracting("results")
            .asList().element(0)
            .extracting("value")
            .asInstanceOf(InstanceOfAssertFactories.MAP).extractingByKey("com.myspace.demo20210908applicant.Application") // can't use extracting due to the dots characters here.
            .extracting("family")
            .asList().element(0)
            .hasFieldOrPropertyWithValue("name", "Jim")
            .hasFieldOrPropertyWithValue("address", "444 St")
            .hasFieldOrPropertyWithValue("ssn", "SSNJim")
            .hasFieldOrPropertyWithValue("uscitizen", true);
    }

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        return null; // intentional, as the new model-specific endpoint do not rely on the Kie-Server-Client Java API.
    }
}

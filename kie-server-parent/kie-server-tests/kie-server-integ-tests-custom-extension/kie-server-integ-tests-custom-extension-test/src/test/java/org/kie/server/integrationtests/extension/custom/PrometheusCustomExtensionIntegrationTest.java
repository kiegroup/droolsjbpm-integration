/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.extension.custom;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PrometheusCustomExtensionIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "function-definition", "1.0.0.Final");

    private static final String CONTAINER_ID  = "function-definition";

    private static Client httpClient;

    private DMNServicesClient dmnClient;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/function-definition");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        KieServerBaseIntegrationTest.createContainer(CONTAINER_ID, releaseId);
    }

    @AfterClass
    public static void disposeContainers() {
        disposeAllContainers();
    }

    @AfterClass
    public static void closeHttpClient() {
        if (httpClient != null) {
            httpClient.close();
            httpClient = null;
        }
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        dmnClient = client.getServicesClient( DMNServicesClient.class );
    }

    @Test
    public void test_evaluateAll() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_ID, dmnContext);
        KieServerAssert.assertSuccess(evaluateAll);

        DMNResult dmnResult = evaluateAll.getResult();

        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        Assertions.assertThat(mathInCtx).containsEntry("Sum", BigDecimal.valueOf( 15 ));

        Map<String, Object> dr0 = (Map<String, Object>) dmnResult.getDecisionResultByName("Math").getResult();
        Assertions.assertThat(dr0).containsEntry("Sum", BigDecimal.valueOf( 15 ));

        assertThat(getMetrics()).contains("random_gauge_nanosecond");
    }

    private String getMetrics() {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder().readTimeout(10, TimeUnit.SECONDS).build();
        }

        WebTarget webTarget = httpClient.target(URI.create(TestConfig.getKieServerHttpUrl()).resolve("../rest/metrics"));
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));

        try (Response response = webTarget.request(MediaType.TEXT_PLAIN).get()) {
            assertEquals(200, response.getStatus());
            return response.readEntity(String.class);
        }
    }
}

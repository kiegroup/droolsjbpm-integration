/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.CustomExtensionClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.common.rest.Authenticator;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.category.WildflyOnly;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomExtensionIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0");

    private static final String CONTAINER_ID = "stateless-kjar1";
    private static final String KIE_SESSION = "stateless";

    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_SURNAME_FIELD = "surname";

    private static final String JOHN_NAME = "john";
    private static final String JOHN_SURNAME = "doe";
    private static final String JOHN_EXPECTED_SURNAME = "smith";
    private static final String JOHN_OUT_IDENTIFIER = "john doe";
    private static final String MARY_NAME = "mary";
    private static final String MARY_SURNAME = "doe";
    private static final String MARY_OUT_IDENTIFIER = "mary doe";

    private CustomExtensionClient customClient;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/stateless-session-kjar");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        KieServerBaseIntegrationTest.createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void closeClient() throws Exception {
        customClient.close();
    }

    @AfterClass
    public static void disposeContainers() {
        disposeAllContainers();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        customClient = client.getServicesClient(CustomExtensionClient.class);
    }

    @Test
    public void testStatelessCall() throws Exception {
        Object john = createPersonInstance(JOHN_NAME, JOHN_SURNAME, kieContainer.getClassLoader());
        Object mary = createPersonInstance(MARY_NAME, MARY_SURNAME, kieContainer.getClassLoader());
        List<Object> persons = new ArrayList<>(Arrays.asList(john, mary));

        ExecutionResults result = customClient.insertFireReturn(CONTAINER_ID, KIE_SESSION, persons);

        Object john2 = result.getValue(JOHN_OUT_IDENTIFIER);
        assertThat(KieServerReflections.valueOf(john2, PERSON_SURNAME_FIELD)).isEqualTo(JOHN_EXPECTED_SURNAME);
        Object mary2 = result.getValue(MARY_OUT_IDENTIFIER);
        assertThat(KieServerReflections.valueOf(mary2, PERSON_SURNAME_FIELD)).isEqualTo(MARY_SURNAME);
    }

    private Object createPersonInstance(String firstname, String surname, ClassLoader loader) {
        return KieServerReflections.createInstance(PERSON_CLASS_NAME, loader, firstname, surname);
    }

    @Test
    @Category({JEEOnly.class, WildflyOnly.class})
    public void testCustomJsonResourceEndpoint() {
        // RHPAM-4876
        if (configuration.getMarshallingFormat() != MarshallingFormat.JSON) {
            return;
        }

        Client httpClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .socketTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .register(new Authenticator(configuration.getUserName(), configuration.getPassword()))
                .register(new ResteasyJacksonProvider())
                .build();

        Map<String, Object> testPayload = new HashMap<>();
        testPayload.put("key1", "value1");
        testPayload.put("key2", 123);

        String endpoint = configuration.getServerUrl() + "/containers/custom-json/1";

        Response response = httpClient.target(endpoint)
                .request()
                .post(Entity.entity(testPayload, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Object entity = response.readEntity(Map.class);
        assertThat(entity).isNotNull()
                .isInstanceOf(Map.class);
        Map<String, Object> responseMap = (Map<String, Object>) entity;
        assertThat(responseMap).containsEntry("key1", "value1")
                .containsEntry("key2", 123)
                .containsEntry("key3", "value3");

        httpClient.close();
    }
}

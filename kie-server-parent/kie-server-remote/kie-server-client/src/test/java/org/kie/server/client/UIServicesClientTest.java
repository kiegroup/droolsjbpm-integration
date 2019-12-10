/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.rest.RestURI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.FORM_URI;
import static org.kie.server.api.rest.RestURI.TASK_FORM_GET_URI;
import static org.kie.server.api.rest.RestURI.build;

public class UIServicesClientTest extends BaseKieServicesClientTest {

    private final String dummyFormContent = "form content";
    
    private static final String TEST_CONTAINER_ID = "test";

    private static final long TEST_TASK_ID = 1L;

    private UIServicesClient uiClient;

    @Before
    public void createClient() {

        config.setCapabilities(Arrays.asList(KieServerConstants.CAPABILITY_BPM_UI));

        uiClient = KieServicesFactory.newKieServicesClient(config).getServicesClient(UIServicesClient.class);
    }

    @Test
    public void testGetTaskForm() {

        String queryString = "?type=ANY&marshallContent=true&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        String form = uiClient.getTaskForm(TEST_CONTAINER_ID, TEST_TASK_ID);
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskFormByType() {

        String queryString = "?type=FTL&marshallContent=true&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        String form = uiClient.getTaskFormByType(TEST_CONTAINER_ID, TEST_TASK_ID, UIServicesClient.FREE_MARKER_TYPE);
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskFormWithLang() {

        String queryString = "?type=ANY&marshallContent=true&lang=en&filter=true";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        String form = uiClient.getTaskForm(TEST_CONTAINER_ID, TEST_TASK_ID, "en");
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskRawForm() {

        String queryString = "?type=ANY&marshallContent=false&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        String form = uiClient.getTaskRawForm(TEST_CONTAINER_ID, TEST_TASK_ID);
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskRawFormAsUserBypassingAuthUser() {

        String userId = "user";

        //url with user on queryString
        String queryString = "?user=" + userId + "&type=ANY&marshallContent=false&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        setBypassAuthUserConfig(Boolean.TRUE);
        String form = uiClient.getTaskRawFormAsUser(TEST_CONTAINER_ID, TEST_TASK_ID, userId);
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskFormAsUserWithoutBypassingAuthUser() {

        String userId = "user";

        String queryString = "?type=ANY&marshallContent=true&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        setBypassAuthUserConfig(Boolean.FALSE);
        String form = uiClient.getTaskFormAsUser(TEST_CONTAINER_ID, TEST_TASK_ID, null, userId);
        assertEquals(dummyFormContent, form);
    }

    @Test
    public void testGetTaskFormAsUserBypassingAuthUser() {

        String userId = "user";

        //url with user on queryString
        String queryString = "?user=" + userId + "&type=ANY&marshallContent=true&filter=false";

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, TEST_CONTAINER_ID);
        valuesMap.put(RestURI.TASK_INSTANCE_ID, TEST_TASK_ID);
        String url = build("", FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + queryString;

        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(dummyFormContent)));

        setBypassAuthUserConfig(Boolean.TRUE);
        String form = uiClient.getTaskFormAsUser(TEST_CONTAINER_ID, TEST_TASK_ID, "", userId);
        assertEquals(dummyFormContent, form);
    }

    /**
     * Uses reflection to set the value of BYPASS_AUTH_USER config on client
     * class
     *
     * @param newValue
     */
    private void setBypassAuthUserConfig(Boolean newValue) {

        try {
            Field field = uiClient.getClass().getSuperclass().getDeclaredField("BYPASS_AUTH_USER");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(uiClient, newValue);

            field.setAccessible(true);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            // Tests will misbehave
            throw new RuntimeException(ex);
        }
    }
}

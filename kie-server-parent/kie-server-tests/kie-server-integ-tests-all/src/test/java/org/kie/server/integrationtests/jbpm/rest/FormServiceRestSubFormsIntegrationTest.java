/*
 * Copyright 2016 JBoss by Red Hat.
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
package org.kie.server.integrationtests.jbpm.rest;

import static org.junit.Assert.*;
import static org.kie.server.api.rest.RestURI.*;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FormServiceRestSubFormsIntegrationTest extends RestJbpmBaseIntegrationTest {
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing",
                                                       "ticket-support-project",
                                                        "1.0.0.Final");

    private static final String CONTAINER_ID = "ticket-support-project";
    private static final String TICKETSUPPORT_PROCESS_ID = "ticket-support";

    private org.jboss.resteasy.client.ClientResponse<?> response = null;

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/ticket-support-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void releaseConnection() {
        if (response != null) {
            response.releaseConnection();
        }
    }

    @Test
    public void testGetProcessFormWithSubForm() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, TICKETSUPPORT_PROCESS_ID);

        ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap))
                .header("Content-Type", getMediaType().toString())
                .header("Accept", getMediaType().toString());
        logger.info("[GET] " + clientRequest.getUri());

        response = clientRequest.get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String result = response.getEntity(String.class);
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // process form has two sub-forms
        // make sure fields from the two sub-forms are included in the result
        // checking for json only (same will apply for xml as well)
        if(getMediaType().getSubtype().equals("json")) {
            JSONObject resultJSON = new org.json.JSONObject(result);
            assertNotNull(resultJSON);

            JSONObject formKey = (JSONObject) resultJSON.get("form");
            JSONArray allFormFields = (JSONArray) formKey.get("field");

            assertNotNull(allFormFields);
            // two subforms
            assertEquals(2, allFormFields.length());
            assertEquals("component-ticket.form", ((JSONObject) allFormFields.get(0)).get("defaultSubform"));
            assertEquals("issue-subform.form", ((JSONObject) allFormFields.get(1)).get("defaultSubform"));


            JSONArray allFormInfo = (JSONArray) formKey.get("form");
            assertNotNull(allFormInfo);
            // two subform info
            assertEquals(2, allFormInfo.length());

            assertEquals(2, ((JSONArray) ((JSONObject) allFormInfo.get(0)).get("field")).length());
            assertEquals(5, ((JSONArray) ((JSONObject) allFormInfo.get(1)).get("field")).length());
        } else if(getMediaType().getSubtype().equals("xml")) {
            try (ByteArrayInputStream stream = new java.io.ByteArrayInputStream(result.getBytes("UTF-8"))) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
                assertNotNull(doc);
                assertNotNull(doc.getDocumentElement());
                assertEquals(2,doc.getDocumentElement().getElementsByTagName("form").getLength());

                NodeList forms = doc.getDocumentElement().getElementsByTagName("form");
                Node firstForm = forms.item(0);
                // 2 subform fields and 4 properties
                assertEquals(6, ((DeferredElementImpl) firstForm).getChildNodes().getLength());

                Node secondForm = forms.item(1);
                // 5 subform fields and 4 properties
                assertEquals(9, ((DeferredElementImpl) secondForm).getChildNodes().getLength());
            }
        }
    }

}

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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FormServiceRestSubFormsIntegrationTest extends RestJbpmBaseIntegrationTest {
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing",
                                                       "ticket-support-project",
                                                        "1.0.0.Final");

    private static final String CONTAINER_ID = "ticket-support-project";
    private static final String TICKETSUPPORT_PROCESS_ID = "ticket-support";

    private Response response = null;

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/ticket-support-project");

        createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void releaseConnection() {
        if (response != null) {
            response.close();
        }
    }

    @Test
    public void testGetProcessFormWithSubForm() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, TICKETSUPPORT_PROCESS_ID);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap));
        logger.info("[GET] " + clientRequest.getUri());

        response = clientRequest.request(getMediaType()).get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String result = response.readEntity(String.class);
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // process form has two sub-forms
        // make sure fields from the two sub-forms are included in the result
        // checking for json only (same will apply for xml as well)
        if(getMediaType().getSubtype().equals("json")) {
            JSONObject resultJSON = new JSONObject(result);
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
                // 2 subform fields, 3 properties and 1 dataHolder
                assertFormChildElements(firstForm, 6, 2, 3, 1);

                Node secondForm = forms.item(1);
                // 5 subform fields, 3 properties and 1 dataHolder
                assertFormChildElements(secondForm, 9, 5, 3, 1);
            }
        }
    }

    private void assertFormChildElements(Node parentNode, int expectedElementsCount, int expectedFieldsCount, int expectedPropertiesCount, int expectedDataHoldersCount) {
        NodeList childNodes = parentNode.getChildNodes();
        int elementCount = 0;
        int fieldsCount = 0;
        int propertiesCount = 0;
        int dataHoldersCount = 0;

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child instanceof Element) {
                elementCount++;
                switch (((Element) child).getTagName()) {
                    case "field":
                        fieldsCount++;
                        break;
                    case "property":
                        propertiesCount++;
                        break;
                    case "dataHolder":
                        dataHoldersCount++;
                        break;
                }
            }
        }

        assertEquals("Wrong count of expected elemntes", expectedElementsCount, elementCount);
        assertEquals("Wrong count of expected fields", expectedFieldsCount, fieldsCount);
        assertEquals("Wrong count of expected properties", expectedPropertiesCount, propertiesCount);
        assertEquals("Wrong count of expected dataHolders", expectedDataHoldersCount, dataHoldersCount);
    }
}

/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model;

import java.io.StringReader;
import java.util.Collections;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class DMNContextKSMarshallingTest {

    private static final Set<Class<?>> classes = Collections.singleton(DMNContextKS.class);
    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    private static final String NAMESPACE = "foo";
    private static final String MODEL_NAME = "bar";
    private static final String DECISION_ID = "baz";
    private static final String DECISION_NAME = "qux";
    private static final String DECISION_SERVICE_NAME = "ds";
    private static final String DMN_CONTEXT_KEY = "quux";
    private static final String DMN_CONTEXT_VALUE = "corge";

    private static final String NAMESPACE_XPATH = "/dmn-evaluation-context/model-namespace";
    private static final String MODEL_NAME_XPATH = "/dmn-evaluation-context/model-name";
    private static final String DECISION_ID_XPATH = "/dmn-evaluation-context/decision-id";
    private static final String DECISION_NAME_XPATH = "/dmn-evaluation-context/decision-name";
    private static final String DECISION_SERVICE_NAME_XPATH = "/dmn-evaluation-context/decision-service-name";
    private static final String DMN_CONTEXT_ENTRY_XPATH_XSTREAM = "/dmn-evaluation-context/dmn-context/entry/string";
    private static final String DMN_CONTEXT_KEY_XPATH_JAXB = "/dmn-evaluation-context/dmn-context/element/@key";
    private static final String DMN_CONTEXT_VALUE_XPATH_JAXB = "/dmn-evaluation-context/dmn-context/element/value";

    private static DMNContextKS BEAN;
    private static String XSTREAM;
    private static String JAXB;
    private static String JSON;

    private static Marshaller xStreamMarshaller;
    private static Marshaller jaxbMarshaller;
    private static Marshaller jsonMarshaller;

    @BeforeClass
    public static void setup() {
        BEAN = new DMNContextKS();
        BEAN.setNamespace(NAMESPACE);
        BEAN.setModelName(MODEL_NAME);
        BEAN.setDecisionIds(Collections.singletonList(DECISION_ID));
        BEAN.setDecisionNames(Collections.singletonList(DECISION_NAME));
        BEAN.setDecisionServiceName(DECISION_SERVICE_NAME);
        BEAN.setDmnContext(Collections.singletonMap(DMN_CONTEXT_KEY, DMN_CONTEXT_VALUE));
        XSTREAM = "<dmn-evaluation-context>\n" +
                "  <model-namespace>foo</model-namespace>\n" +
                "  <model-name>bar</model-name>\n" +
                "  <decision-name>qux</decision-name>\n" +
                "  <decision-id>baz</decision-id>\n" +
                "  <decision-service-name>ds</decision-service-name>\n" +
                "  <dmn-context class=\"singleton-map\">\n" +
                "    <entry>\n" +
                "      <string>quux</string>\n" +
                "      <string>corge</string>\n" +
                "    </entry>\n" +
                "  </dmn-context>\n" +
                "</dmn-evaluation-context>";
        JAXB = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<dmn-evaluation-context>\n" +
                "    <model-namespace>foo</model-namespace>\n" +
                "    <model-name>bar</model-name>\n" +
                "    <decision-name>qux</decision-name>\n" +
                "    <decision-id>baz</decision-id>\n" +
                "    <decision-service-name>ds</decision-service-name>\n" +
                "    <dmn-context xsi:type=\"jaxbListWrapper\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "        <type>MAP</type>\n" +
                "        <element xsi:type=\"jaxbStringObjectPair\" key=\"quux\">\n" +
                "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">corge</value>\n" +
                "        </element>\n" +
                "    </dmn-context>\n" +
                "</dmn-evaluation-context>\n";
        JSON = String.format("{%n" +
                "  \"model-namespace\" : \"foo\",%n" +
                "  \"model-name\" : \"bar\",%n" +
                "  \"decision-name\" : \"qux\",%n" +
                "  \"decision-id\" : \"baz\",%n" +
                "  \"decision-service-name\" : \"ds\",%n" +
                "  \"dmn-context\" : {\"quux\" : \"corge\"}%n" +
                "}");
        xStreamMarshaller = MarshallerFactory.getMarshaller(classes, MarshallingFormat.XSTREAM, DMNContextKS.class.getClassLoader());
        jaxbMarshaller = MarshallerFactory.getMarshaller(classes, MarshallingFormat.JAXB, DMNContextKS.class.getClassLoader());
        jsonMarshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, DMNContextKS.class.getClassLoader());
    }

    @Test
    public void testXStreamMarshalling() throws XPathExpressionException {
        final String result = xStreamMarshaller.marshall(BEAN);
        assertNotNull(result);
        checkXPath(result, NAMESPACE_XPATH, NAMESPACE);
        checkXPath(result, MODEL_NAME_XPATH, MODEL_NAME);
        checkXPath(result, DECISION_ID_XPATH, DECISION_ID);
        checkXPath(result, DECISION_NAME_XPATH, DECISION_NAME);
        checkXPath(result, DECISION_SERVICE_NAME_XPATH, DECISION_SERVICE_NAME);
        checkXPath(result, DMN_CONTEXT_ENTRY_XPATH_XSTREAM, DMN_CONTEXT_KEY, DMN_CONTEXT_VALUE);
    }

    private void checkXPath(String source, String xPath, String... expectedValues) {
        try (StringReader reader = new StringReader(source)) {
            final InputSource src = new InputSource(reader);
            NodeList nodeList = (NodeList) xpath.evaluate(xPath, src, XPathConstants.NODESET);
            assertEquals(nodeList.getLength(), expectedValues.length);
            for (int i = 0; i < expectedValues.length; i++) {
                assertEquals(nodeList.item(i).getTextContent(), expectedValues[i]);
            }
        } catch (XPathExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testXStreamUnmarshalling() {
        final DMNContextKS result = xStreamMarshaller.unmarshall(XSTREAM, DMNContextKS.class);
        checkBean(result);
    }

    private static void checkBean(DMNContextKS bean) {
        assertNotNull(bean);
        assertEquals(bean.getNamespace(), NAMESPACE);
        assertEquals(bean.getModelName(), MODEL_NAME);
        assertNotNull(bean.getDecisionIds());
        assertEquals(bean.getDecisionIds().size(), 1);
        assertEquals(bean.getDecisionIds().get(0), DECISION_ID);
        assertNotNull(bean.getDecisionNames());
        assertEquals(bean.getDecisionNames().size(), 1);
        assertEquals(bean.getDecisionNames().get(0), DECISION_NAME);
        assertEquals(bean.getDecisionServiceName(), DECISION_SERVICE_NAME);
        assertNotNull(bean.getDmnContext());
        assertEquals(bean.getDmnContext().size(), 1);
        assertEquals(bean.getDmnContext().get(DMN_CONTEXT_KEY), DMN_CONTEXT_VALUE);
    }

    @Test
    public void testJaxbMarshalling() {
        final String result = jaxbMarshaller.marshall(BEAN);
        assertNotNull(result);
        checkXPath(result, NAMESPACE_XPATH, NAMESPACE);
        checkXPath(result, MODEL_NAME_XPATH, MODEL_NAME);
        checkXPath(result, DECISION_ID_XPATH, DECISION_ID);
        checkXPath(result, DECISION_NAME_XPATH, DECISION_NAME);
        checkXPath(result, DECISION_SERVICE_NAME_XPATH, DECISION_SERVICE_NAME);
        checkXPath(result, DMN_CONTEXT_KEY_XPATH_JAXB, DMN_CONTEXT_KEY);
        checkXPath(result, DMN_CONTEXT_VALUE_XPATH_JAXB, DMN_CONTEXT_VALUE);
    }

    @Test
    public void testJaxbUnmarshalling() {
        final DMNContextKS result = jaxbMarshaller.unmarshall(JAXB, DMNContextKS.class);
        checkBean(result);
    }

    @Test
    public void testJsonMarshalling() {
        final String result = jsonMarshaller.marshall(BEAN);
        assertEquals(JSON, result); // TODO: jsonpath?
    }

    @Test
    public void testJsonUnmarshalling() {
        final DMNContextKS result = jsonMarshaller.unmarshall(JSON, DMNContextKS.class);
        checkBean(result);
    }

    @AfterClass
    public static void teardown() {
        xStreamMarshaller.dispose();
        jaxbMarshaller.dispose();
        jsonMarshaller.dispose();
    }
}

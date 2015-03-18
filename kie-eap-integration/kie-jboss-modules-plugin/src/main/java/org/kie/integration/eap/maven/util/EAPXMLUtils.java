/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class EAPXMLUtils {

    private InputStream inputFile;
    private Document document;
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    public EAPXMLUtils() throws Exception {

    }

    public EAPXMLUtils(InputStream inputFile) throws Exception {
        this.inputFile = inputFile;
        DocumentBuilder builder = init();
        if (inputFile != null) document = builder.parse(inputFile);
    }

    private DocumentBuilder init() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        return documentBuilderFactory.newDocumentBuilder();
    }

    private void newDocument() throws Exception {
        DocumentBuilder builder = init();
        document = builder.newDocument();
    }

    public static EAPXMLUtils newInstance() throws Exception {
        EAPXMLUtils result = new EAPXMLUtils();
        result.newDocument();
        return result;
    }

    public Element createElement(String name, Map<String, String> attributes, Element parent) {
        Element element= document.createElement(name);
        if (attributes != null) {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                String attrName = attribute.getKey();
                String attrValue = attribute.getValue();
                element.setAttribute(attrName, attrValue);
            }
        }

        if (parent != null) parent.appendChild(element);

        return element;
    }

    public static void printInputStreamContent(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
    }

    public static String getAttributeValue(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(attrName);
            if (attr != null) return attr.getNodeValue();
        }
        return null;
    }

    public Node getXPathNode(String expr) throws XPathExpressionException {
        return (Node) XPATH.compile(expr).evaluate(getDocument(), XPathConstants.NODE);
    }

    public Document getDocument() {
        return document;
    }
}

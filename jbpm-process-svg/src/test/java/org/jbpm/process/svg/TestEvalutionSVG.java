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

package org.jbpm.process.svg;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class TestEvalutionSVG {

    private XPath xpath = XPathFactory.newInstance().newXPath();

    @Test
    public void test() throws Exception {
        List<String> completed = new ArrayList<String>();
        completed.add("_343B16DA-961A-49BF-8697-9A86DEAFBAF4");
        List<String> active = new ArrayList<String>();
        active.add("_6063D302-9D81-4C86-920B-E808A45377C2");
        String svg = SVGImageProcessor.transform(TestEvalutionSVG.class.getResourceAsStream("/evaluation-svg.svg"), completed, active);

        // verify transformation
        Document svgDocument = readSVG(svg);
        validateNodesMarkedAsActive(svgDocument, active);
        validateNodesMarkedAsCompleted(svgDocument, completed);
    }

    @Test
    public void testByName() throws Exception {
        List<String> completedID = new ArrayList<String>();
        completedID.add("_6063D302-9D81-4C86-920B-E808A45377C2");
        List<String> activeID = new ArrayList<String>();
        activeID.add("_AE5BF0DC-B720-4FDE-9499-5ED89D41FB1A");

        List<String> completed = new ArrayList<String>();
        completed.add("Self Evaluation");
        List<String> active = new ArrayList<String>();
        active.add("PM Evaluation");
        String svg = SVGImageProcessor.transformByName(TestEvalutionSVG.class.getResourceAsStream("/evaluation-svg.svg"), completed, active);

        // verify transformation
        Document svgDocument = readSVG(svg);
        validateNodesMarkedAsActive(svgDocument, activeID);
        validateNodesMarkedAsCompleted(svgDocument, completedID);
    }

    @Test
    public void testCompletedAndActive() throws Exception {
        List<String> completed = new ArrayList<String>();
        completed.add("_343B16DA-961A-49BF-8697-9A86DEAFBAF4");
        completed.add("_6063D302-9D81-4C86-920B-E808A45377C2");
        List<String> active = new ArrayList<String>();
        active.add("_6063D302-9D81-4C86-920B-E808A45377C2");
        String svg = SVGImageProcessor.transform(TestEvalutionSVG.class.getResourceAsStream("/evaluation-svg.svg"), completed, active);

        // verify transformation
        Document svgDocument = readSVG(svg);
        validateNodesMarkedAsActive(svgDocument, active);
        // remove it as it should be not considered completed and was already asserted as active
        completed.remove("_6063D302-9D81-4C86-920B-E808A45377C2");
        validateNodesMarkedAsCompleted(svgDocument, completed);
    }

    @Test
    public void testSubProcessLink() throws Exception {
        List<String> completed = new ArrayList<String>();
        List<String> active = new ArrayList<String>();
        active.add("_35262208-8B3E-457E-8D8A-798E70CC280D");

        Map<String, String> links = new HashMap<>();
        links.put("_35262208-8B3E-457E-8D8A-798E70CC280D", "http://localhost/processes/1");
        String svg = SVGImageProcessor.transform(TestEvalutionSVG.class.getResourceAsStream("/call-activity-svg.svg"), completed, active, links);

        // verify transformation
        Document svgDocument = readSVG(svg);
        validateNodesMarkedAsActive(svgDocument, active);
        validateCallActivityLinked(svgDocument, active, links);
    }

    // helper methods for verifying svg transformation

    private void validateNodesMarkedAsActive(Document svgDocument, List<String> activeNodes) throws XPathExpressionException {
        for (String activeNode : activeNodes) {

            XPathExpression expr = xpath.compile("//*[@bpmn2nodeid='" + activeNode + "']");
            Element element = (Element) expr.evaluate(svgDocument, XPathConstants.NODE);

            if (element == null) {
                fail("Active element " + activeNode + " not found in the document");
            }
            String svgId = element.getAttribute("id");

            Element border = svgDocument.getElementById(svgId + "bg_frame");

            String marker = border.getAttribute("stroke");
            assertNotNull(marker);
            assertEquals("#FF0000", marker);
            String markerWidth = border.getAttribute("stroke-width");
            assertNotNull(markerWidth);
            assertEquals("2", markerWidth);

        }
    }

    private void validateNodesMarkedAsCompleted(Document svgDocument, List<String> completedNodes) throws XPathExpressionException {

        for (String completedNode : completedNodes) {
            XPathExpression expr = xpath.compile("//*[@bpmn2nodeid='" + completedNode + "']");
            Element element = (Element) expr.evaluate(svgDocument, XPathConstants.NODE);

            if (element == null) {
                fail("Completed element " + completedNode + " not found in the document");
            }
            String svgId = element.getAttribute("id");
            Element background = svgDocument.getElementById(svgId + "fill_el");

            String marker = background.getAttribute("stop-color");
            assertNotNull(marker);
            assertEquals("#C0C0C0", marker);

        }
    }

    private void validateCallActivityLinked(Document svgDocument, List<String> activeNodes, Map<String, String> links) throws XPathExpressionException {
        for (String activeNode : activeNodes) {

            XPathExpression expr = xpath.compile("//*[@bpmn2nodeid='" + activeNode + "']");
            Element element = (Element) expr.evaluate(svgDocument, XPathConstants.NODE);

            if (element == null) {
                fail("Active element " + activeNode + " not found in the document");
            }
            String svgId = element.getAttribute("id");

            Element border = svgDocument.getElementById(svgId + "pimg");

            String onclick = border.getAttribute("onclick");
            assertNotNull(onclick);
            assertEquals("", onclick);
            String link = border.getAttributeNS("http://www.w3.org/1999/xlink", "href");
            assertNotNull(link);
            assertEquals(links.get(activeNode), link);
            String target = border.getAttribute("target");
            assertNotNull(target);
            assertEquals("_blank", target);

        }
    }

    private Document readSVG(String svgContent) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        factory.setValidating(false);
        Document svgDocument = factory.createDocument("http://jbpm.org", new StringReader(svgContent));

        return svgDocument;
    }
}

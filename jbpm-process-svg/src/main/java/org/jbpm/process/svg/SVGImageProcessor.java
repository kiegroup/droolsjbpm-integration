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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMTSpanElement;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jbpm.process.svg.model.NodeSummary;
import org.jbpm.process.svg.model.SVGSummary;
import org.jbpm.process.svg.model.SetBackgroundColorTransformation;
import org.jbpm.process.svg.model.SetBorderColorTransformation;
import org.jbpm.process.svg.model.SetSubProcessLinkTransformation;
import org.jbpm.process.svg.model.Transformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGImageProcessor {

    private Document svgDocument;
    private SVGSummary summary = new SVGSummary();
    private boolean mapById = true;

    public SVGImageProcessor(InputStream svg) {
        this(svg, true);
    }
    
    public SVGImageProcessor(InputStream svg, boolean mapById) {
        this.mapById = mapById;
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            factory.setValidating(false);
            svgDocument = factory.createDocument("http://jbpm.org", svg);
            processNodes(svgDocument.getChildNodes());
        } catch (IOException e) {
            throw new RuntimeException("Could not parse svg", e);
        }
    }

    public static String transform(InputStream svg, List<String> completed, List<String> active) {
        return transform(svg, completed, active, null);
    }

    public static String transform(InputStream svg, List<String> completed, List<String> active, Map<String, String> subProcessLinks) {
        SVGImageProcessor processor = new SVGImageProcessor(svg);
        for (String nodeId : completed) {
            if (!active.contains(nodeId)) {
                processor.defaultCompletedTransformation(nodeId);
            }
        }
        for (String nodeId : active) {
            processor.defaultActiveTransformation(nodeId);
        }

        if (subProcessLinks != null) {

            for (Map.Entry<String, String> subProcessLink : subProcessLinks.entrySet()) {
                processor.defaultSubProcessLinkTransformation(subProcessLink.getKey(), subProcessLink.getValue());
            }
        }
        return processor.getSVG();
    }

    public static String transformByName(InputStream svg, List<String> completed, List<String> active) {
        SVGImageProcessor processor = new SVGImageProcessor(svg, false);
        for (String nodeId : completed) {
            if (!active.contains(nodeId)) {
                processor.defaultCompletedTransformation(nodeId);
            }
        }
        for (String nodeId : active) {
            processor.defaultActiveTransformation(nodeId);

        }
        return processor.getSVG();
    }

    public void transform(Transformation t) {
        t.transform(summary);
    }

    public void defaultCompletedTransformation(String nodeId) {
        transform(new SetBackgroundColorTransformation(nodeId, "#C0C0C0"));
    }

    public void defaultActiveTransformation(String nodeId) {
        transform(new SetBorderColorTransformation(nodeId, "#FF0000"));
    }

    public void defaultSubProcessLinkTransformation(String nodeId, String link) {
        transform(new SetSubProcessLinkTransformation(nodeId, link));
    }

    public String getSVG() {
        try {
            DOMSource domSource = new DOMSource(svgDocument.getFirstChild());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Could not transform svg", e);
        }
    }

    private void processNodes(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node svgIdNode = attributes.getNamedItem("id");
                if (svgIdNode != null) {
                    String svgId = svgIdNode.getNodeValue();
                    if (mapById) {
                        Node nodeIdNode = attributes.getNamedItem("bpmn2nodeid");
                        if (nodeIdNode != null) {
                            String nodeId = nodeIdNode.getNodeValue();
                            Element border = null;
                            Element background = null;
                            Element subProcessLink = null;
                            if (nodeId != null) {
                                background = svgDocument.getElementById(svgId + "fill_el");
                                border = svgDocument.getElementById(svgId + "bg_frame");
                                Element borderSubProcess = svgDocument.getElementById(svgId + "frame");

                                subProcessLink = svgDocument.getElementById(svgId + "pimg");
                                summary.addNode(new NodeSummary(nodeId, border, background, borderSubProcess, subProcessLink));
                            }
                        }
                    } else {
                        // map by name
                        if (svgId.endsWith("text_name")) {
                            svgId = svgId.substring(0, svgId.length() - 9);
                            StringBuilder taskLabel = new StringBuilder();
                            for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                                if (node.getChildNodes().item(j) instanceof SVGOMTSpanElement) {
                                    SVGOMTSpanElement spanElement = (SVGOMTSpanElement)node.getChildNodes().item(j);
                                    taskLabel.append(spanElement.getFirstChild().getNodeValue());
                                }
                            }
                            String name = taskLabel.toString();
                            // filtering out nodes with no name
                            if (!name.trim().isEmpty()) {
                                Element background = svgDocument.getElementById(svgId + "fill_el");
                                Element border = svgDocument.getElementById(svgId + "bg_frame");
                                Element borderSubProcess = svgDocument.getElementById(svgId + "frame");

                                Element subProcessLink = svgDocument.getElementById(svgId + "pimg");
                                summary.addNode(new NodeSummary(name, border, background, borderSubProcess, subProcessLink));
                            }
                        }
                    }
                }
            }
            processNodes(node.getChildNodes());
        }
    }
    
}

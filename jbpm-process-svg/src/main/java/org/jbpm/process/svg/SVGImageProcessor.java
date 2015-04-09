package org.jbpm.process.svg;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jbpm.process.svg.model.NodeSummary;
import org.jbpm.process.svg.model.SVGSummary;
import org.jbpm.process.svg.model.SetBackgroundColorTransformation;
import org.jbpm.process.svg.model.SetBorderColorTransformation;
import org.jbpm.process.svg.model.Transformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGImageProcessor {

    private Document svgDocument;
    private SVGSummary summary = new SVGSummary();

    public SVGImageProcessor(InputStream svg) {
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
        SVGImageProcessor processor = new SVGImageProcessor(svg);
        for (String nodeId : completed) {
            processor.defaultCompletedTransformation(nodeId);
        }
        for (String nodeId : active) {
            if (!completed.contains(nodeId)) {
                processor.defaultActiveTransformation(nodeId);
            }
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
                    Node nodeIdNode = attributes.getNamedItem("bpmn2nodeid");
                    if (nodeIdNode != null) {
                        String nodeId = nodeIdNode.getNodeValue();
                        Element border = null;
                        Element background = null;
                        if (nodeId != null) {
                            background = svgDocument.getElementById(svgId + "fill_el");
                            border = svgDocument.getElementById(svgId + "bg_frame");
                            summary.addNode(new NodeSummary(nodeId, border, background));
                        }
                    }
                }
            }
            processNodes(node.getChildNodes());
        }
    }

}

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.proxy.aggragate;

import static org.kie.server.router.utils.Helper.read;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class XMLResponseAggregator implements ResponseAggregator {

    private static final Logger log = Logger.getLogger(XMLResponseAggregator.class);

    private static final String SORT_XSLT = read(XMLResponseAggregator.class.getResourceAsStream("/sort.xsl"));
    private static final String PAGE_XSLT = read(XMLResponseAggregator.class.getResourceAsStream("/page.xsl"));

    public XMLResponseAggregator() {
    }

    protected boolean supports(String expectedType, Object... acceptTypes) {
        for (Object acceptType : acceptTypes ) {
            if (acceptType == null) {
                continue;
            }
            boolean found = acceptType.toString().toLowerCase().contains(expectedType);
            if (found) {
                return true;
            }
        }

        return false;
    }

    public String aggregate(List<String> data) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }
            List<String> nodes = knownNames();
            Document document = data.stream().map(xml -> {
                return newDoc(xml);
            })
                    .filter(d -> d != null)
                    .reduce((source, target) -> {
                        deepMerge(source, target, nodes, target);
                        return target;
                    }).get();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(out);
            Source input = new DOMSource(document);

            transformer.transform(input, output);

            return new String(out.toByteArray());
        } catch (Exception e) {
            log.errorf("Failed to aggregate xml responses of %s", data, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String aggregate(List<String> data, String sortBy, boolean ascending, Integer page, Integer pageSize) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }

            List<String> nodes = knownNames();
            Document document = data.stream().map(xml -> {
                return newDoc(xml);
            })
                    .filter(d -> d != null)
                    .reduce((source, target) -> {
                        deepMerge(source, target, nodes, target);
                        return target;
                    }).get();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = null;

            String root = document.getDocumentElement().getNodeName();
            String sortNode = getElementLevel(root);
            if (sortBy != null && !sortBy.trim().isEmpty()) {

                transformer = sort(getRootNode(root), sortNode, sortBy, ascending, document);

                DOMResult toutput = new DOMResult();
                Source input = new DOMSource(document);
                transformer.transform(input, toutput);

                document = (Document) toutput.getNode();

            }
            transformer = page(getRootNode(root), sortNode, page, pageSize, document);

            Result output = new StreamResult(out);
            transformer.transform(new DOMSource(document), output);

            return new String(out.toByteArray());
        } catch (Exception e) {
            log.errorf("Failed to aggregate xml responses of %s", data, e);
            throw new RuntimeException(e);
        }
    }

    protected Transformer sort(String root, String level, String fieldName, boolean ascending, Document source) throws Exception {
        String order = "ascending";
        if (!ascending) {
            order = "descending";
        }

        StreamSource xsltSort = new StreamSource(new StringReader(SORT_XSLT
                .replaceAll("KIE_ROOT", root)
                .replaceAll("LEVEL", level)
                .replaceAll("KIE_SORT_BY", sortBy(fieldName))
                .replaceAll("KIE_ORDER", order)));

        return TransformerFactory.newInstance().newTransformer(xsltSort);

    }

    protected Transformer page(String root, String node, Integer start, Integer size, Document source) throws Exception {

        Integer actualStart = start;
        Integer actualSize = size;
        if (actualStart > 0) {
            actualStart = actualStart * size;

            actualSize = actualStart + size;
        }

        log.debug("Start " + actualStart + " size " + actualSize);
        StreamSource xsltSort = new StreamSource(new StringReader(PAGE_XSLT
                .replaceAll("LEVEL", node)
                .replaceAll("START", actualStart.toString())
                .replaceAll("SIZE", actualSize.toString())));

        return TransformerFactory.newInstance().newTransformer(xsltSort);

    }

    public Document newDoc(String xml) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            return doc;
        } catch (Exception e) {
            log.errorf("Failed to create xml document of %s", xml, e);
            return null;
        }
    }

    protected void deepMerge(Node sourceNode, Node targetNode, List<String> nodes, Document target) {
        if (targetNode == null) {
            return;
        }
        NodeList sourceNodeList = sourceNode.getChildNodes();
        NodeList targetNodeList = targetNode.getChildNodes();

        for (int i = 0; i < sourceNodeList.getLength(); i++) {
            sourceNode = sourceNodeList.item(i);
            targetNode = targetNodeList.item(i);

            if (!nodes.contains(sourceNode.getNodeName())) {
                deepMerge(sourceNode, targetNode, nodes, target);
            }
            if (targetNode == null) {
                return;
            }
            // found correct node to copy it's content
            NodeList children = sourceNode.getChildNodes();
            copyNodes(children, target, targetNode);
        }
    }

    protected abstract void copyNodes(NodeList children, Document target, Node targetNode);

    protected abstract List<String> knownNames();

    protected abstract String getElementLevel(String rootNode);

    protected abstract String getRootNode(String rootNode);

    protected abstract String sortBy(String fieldName);

}

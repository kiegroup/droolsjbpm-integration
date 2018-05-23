package org.jbpm.process.svg.processor;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbpm.process.svg.model.NodeSummary;
import org.jbpm.process.svg.model.RenderType;
import org.jbpm.process.svg.model.SetSubProcessLinkTransformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StunnerSVGProcessor extends AbstractSVGProcessor {

    public StunnerSVGProcessor(Document svgDocument) {
        super(svgDocument, true);
    }

    @Override
    public void defaultCompletedTransformation(String nodeId) {
        transform((summary) -> {
            Optional.ofNullable(summary.getNode(nodeId)).ifPresent(node -> {
                Optional.ofNullable(node.getBackground()).ifPresent(background -> {
                    background.setAttribute("fill", COMPLETED_COLOR);
                    setNodeBorderColor(node.getRenderType(), node.getBorder(), COMPLETED_BORDER_COLOR);
                });
            });
        });
    }

    @Override
    public void defaultActiveTransformation(String nodeId) {
        transform((summary) -> {
            Optional.ofNullable(summary.getNode(nodeId)).ifPresent(node -> {
                Optional.ofNullable(node.getBorder()).ifPresent(border -> {
                    setNodeBorderColor(node.getRenderType(), border, ACTIVE_COLOR);
                });
            });
        });
    }

    private void setNodeBorderColor(Optional<RenderType> renderType, Element border, String color) {
        final RenderType render = renderType.orElse(RenderType.STROKE);
        switch (render) {
            case STROKE:
                border.setAttribute("stroke-width", "2");
                border.setAttribute("stroke", color);
                break;
            case FILL:
                border.setAttribute("fill", color);
                break;
        }
    }

    @Override
    public void defaultSubProcessLinkTransformation(String nodeId, String link) {
        transform(new SetSubProcessLinkTransformation(nodeId, link));
    }

    private void processNode(final Node parent, final String nodeId) {
        final NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            final NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                final Node svgIdNode = attributes.getNamedItem("id");
                if (svgIdNode != null) {
                    final String value = svgIdNode.getNodeValue();
                    if (Objects.nonNull(value)) {
                        Map<String, String> parameters =
                                Stream.of(value.substring(value.indexOf("?") + 1))
                                        .filter(Objects::nonNull)
                                        .map(str -> Collections.list(new StringTokenizer(str, "&")))
                                        .flatMap(list -> list.stream())
                                        .map(String::valueOf)
                                        .filter(str -> str.split("=").length == 2)
                                        .collect(Collectors.toMap(v -> v.split("=")[0], v -> v.split("=")[1]));

                        NodeSummary nodeSummary = summary.getNodesMap().getOrDefault(nodeId, new NodeSummary(nodeId, null, null, null, null, null));
                        Element border = Objects.equals(parameters.get("shapeType"), "BORDER") ? (Element) node : nodeSummary.getBorder();
                        Element background = Objects.equals(parameters.get("shapeType"), "BACKGROUND") ? (Element) node : nodeSummary.getBackground();
                        RenderType renderType = RenderType.valueOf(Optional.ofNullable(parameters.get("renderType")).orElse(nodeSummary.getRenderType().orElse(RenderType.STROKE).name()));

                        summary.addNode(new NodeSummary(nodeId, border, background, null, null, renderType));
                    }
                    break;
                }
            }
            processNode(node, nodeId);
        }
    }

    @Override
    public void processNodes(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node svgIdNode = attributes.getNamedItem("id");
                if (svgIdNode != null) {
                    Node nodeIdNode = attributes.getNamedItem("bpmn2nodeid");
                    if (nodeIdNode != null) {
                        String nodeId = nodeIdNode.getNodeValue();
                        if (nodeId != null) {
                            //process bpmn2 node to parse the attributes
                            processNode(node, nodeId);
                        }
                    }
                }
            }
            processNodes(node.getChildNodes());
        }
    }
}

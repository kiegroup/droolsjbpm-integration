package org.jbpm.process.svg.model;

import org.w3c.dom.Element;

public class SetBorderColorTransformation extends NodeTransformation {

    private String color;

    public SetBorderColorTransformation(String nodeId, String color) {
        super(nodeId);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void transform(SVGSummary summary) {
        NodeSummary node = summary.getNode(getNodeId());
        if (node != null) {
            Element border = node.getBorder();
            if (border != null) {
                border.setAttribute("stroke", color);
            }
        }
    }

}

package org.jbpm.process.svg.model;

import org.w3c.dom.Element;

public class SetBackgroundColorTransformation extends NodeTransformation {

    private String color;

    public SetBackgroundColorTransformation(String nodeId, String color) {
        super(nodeId);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void transform(SVGSummary summary) {
        NodeSummary node = summary.getNode(getNodeId());
        if (node != null) {
            Element background = node.getBackground();
            if (background != null) {
                background.setAttribute("stop-color", color);
            }
        }
    }

}

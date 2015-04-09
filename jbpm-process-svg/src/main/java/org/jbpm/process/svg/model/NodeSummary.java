package org.jbpm.process.svg.model;

import org.w3c.dom.Element;

public class NodeSummary {

    private String nodeId;
    private Element border;
    private Element background;

    public NodeSummary(String nodeId, Element border, Element background) {
        this.nodeId = nodeId;
        this.border = border;
        this.background = background;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Element getBorder() {
        return border;
    }

    public Element getBackground() {
        return background;
    }

}

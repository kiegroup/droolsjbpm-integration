package org.jbpm.process.svg.model;

public abstract class NodeTransformation implements Transformation {

    private String nodeId;

    public NodeTransformation(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

}

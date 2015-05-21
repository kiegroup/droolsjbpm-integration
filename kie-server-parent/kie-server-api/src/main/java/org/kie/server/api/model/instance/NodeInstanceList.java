package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "node-instance-list")
public class NodeInstanceList {

    @XmlElement(name="node-instance")
    private NodeInstance[] nodeInstances;

    public NodeInstanceList() {
    }

    public NodeInstanceList(NodeInstance[] nodeInstances) {
        this.nodeInstances = nodeInstances;
    }

    public NodeInstanceList(List<NodeInstance> nodeInstances) {
        this.nodeInstances = nodeInstances.toArray(new NodeInstance[nodeInstances.size()]);
    }

    public NodeInstance[] getNodeInstances() {
        return nodeInstances;
    }

    public void setNodeInstances(NodeInstance[] nodeInstances) {
        this.nodeInstances = nodeInstances;
    }
}

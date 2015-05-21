package org.kie.server.api.model.instance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.type.JaxbMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "work-item-instance")
public class WorkItemInstance {

    @XmlElement(name="work-item-id")
    private Long id;
    @XmlElement(name="work-item-name")
    private String name;
    @XmlElement(name="work-item-state")
    private Integer state = 0;
    @XmlElement(name="work-item-params")
    private JaxbMap parameters;
    @XmlElement(name="process-instance-id")
    private Long processInstanceId;
    @XmlElement(name="container-id")
    private String containerId;
    @XmlElement(name="node-instance-id")
    private Long nodeInstanceId;
    @XmlElement(name="node-id")
    private Long nodeId;

    public WorkItemInstance() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Map<String, Object> getParameters() {
        if (parameters != null) {
            return parameters.unwrap();
        }
        return Collections.emptyMap();
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = new JaxbMap(parameters);
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Long getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(Long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public static class Builder {

        private WorkItemInstance workItemInstance = new WorkItemInstance();

        public WorkItemInstance build() {
            return workItemInstance;
        }

        public Builder id(Long id) {
            workItemInstance.setId(id);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            workItemInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder nodeInstanceId(Long nodeInstanceId) {
            workItemInstance.setNodeInstanceId(nodeInstanceId);
            return this;
        }

        public Builder nodeId(Long nodeId) {
            workItemInstance.setNodeId(nodeId);
            return this;
        }

        public Builder name(String name) {
            workItemInstance.setName(name);
            return this;
        }

        public Builder containerId(String containerId) {
            workItemInstance.setContainerId(containerId);
            return this;
        }

        public Builder state(Integer state) {
            workItemInstance.setState(state);
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            workItemInstance.setParameters(parameters);
            return this;
        }
    }
}

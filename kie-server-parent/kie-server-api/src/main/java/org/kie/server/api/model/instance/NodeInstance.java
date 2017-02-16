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

package org.kie.server.api.model.instance;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "node-instance")
public class NodeInstance {

    @XmlElement(name="node-instance-id")
    private Long id;
    @XmlElement(name="node-name")
    private String name;
    @XmlElement(name="process-instance-id")
    private Long processInstanceId;
    @XmlElement(name="work-item-id")
    private Long workItemId;
    @XmlElement(name="container-id")
    private String containerId;
    @XmlElement(name="start-date")
    private Date date;
    @XmlElement(name="node-id")
    private String nodeId;
    @XmlElement(name="node-type")
    private String nodeType;
    @XmlElement(name="node-connection")
    private String connection;
    @XmlElement(name="node-completed")
    private Boolean completed;
    @XmlElement(name="reference-id")
    private Long referenceId;

    public NodeInstance() {
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

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "NodeInstance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", processInstanceId=" + processInstanceId +
                ", workItemId=" + workItemId +
                ", containerId='" + containerId + '\'' +
                ", date=" + date +
                ", nodeId='" + nodeId + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", completed=" + completed +
                '}';
    }

    public static class Builder {
        private NodeInstance processInstance = new NodeInstance();

        public NodeInstance build() {
            return processInstance;
        }

        public Builder id(Long id) {
            processInstance.setId(id);
            return this;
        }

        public Builder nodeId(String nodeId) {
            processInstance.setNodeId(nodeId);
            return this;
        }

        public Builder name(String name) {
            processInstance.setName(name);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            processInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder workItemId(Long workItemId) {
            processInstance.setWorkItemId(workItemId);
            return this;
        }

        public Builder containerId(String containerId) {
            processInstance.setContainerId(containerId);
            return this;
        }

        public Builder nodeType(String nodeType) {
            processInstance.setNodeType(nodeType);
            return this;
        }

        public Builder date(Date date) {
            processInstance.setDate(date == null ? date : new Date(date.getTime()));
            return this;
        }

        public Builder connection(String connection) {
            processInstance.setConnection(connection);
            return this;
        }

        public Builder completed(Boolean completed) {
            processInstance.setCompleted(completed);
            return this;
        }

        public Builder referenceId(Long referenceId) {
            processInstance.setReferenceId(referenceId);
            return this;
        }

    }
}

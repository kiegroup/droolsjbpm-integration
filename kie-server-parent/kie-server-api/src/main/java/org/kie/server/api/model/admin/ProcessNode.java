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

package org.kie.server.api.model.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-node")
public class ProcessNode {

    @XmlElement(name="name")
    private String nodeName;
    @XmlElement(name="id")
    private long nodeId;
    @XmlElement(name="type")
    private String nodeType;
    @XmlElement(name="process-id")
    private String processId;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ProcessNode processNode = new ProcessNode();

        public ProcessNode build() {
            return processNode;
        }

        public Builder nodeName(String name) {
            processNode.setNodeName(name);
            return this;
        }

        public Builder nodeId(long id) {
            processNode.setNodeId(id);
            return this;
        }

        public Builder nodeType(String type) {
            processNode.setNodeType(type);
            return this;
        }

        public Builder processId(String processId) {
            processNode.setProcessId(processId);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ProcessNode{" +
                "nodeName='" + nodeName + '\'' +
                ", nodeId=" + nodeId +
                ", nodeType='" + nodeType + '\'' +
                ", processId='" + processId + '\'' +
                '}';
    }
}

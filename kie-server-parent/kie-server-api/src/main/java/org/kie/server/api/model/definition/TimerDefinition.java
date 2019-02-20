/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "timer-definition")
public class TimerDefinition {

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "node-name")
    private String nodeName;

    @XmlElement(name = "unique-id")
    private String uniqueId;

    @XmlElement(name = "nodeId")
    private Long nodeId;

    public TimerDefinition() {
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "TimerDefinition{" +
                "id=" + id +
                ", nodeName='" + nodeName + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }

    public static final class Builder {

        private TimerDefinition timerDefinition;

        private Builder() {
            timerDefinition = new TimerDefinition();
        }

        public Builder id(Long id) {
            timerDefinition.setId(id);
            return this;
        }

        public Builder nodeName(String nodeName) {
            timerDefinition.setNodeName(nodeName);
            return this;
        }

        public Builder uniqueId(String uniqueId) {
            timerDefinition.setUniqueId(uniqueId);
            return this;
        }

        public Builder nodeId(Long nodeId) {
            timerDefinition.setNodeId(nodeId);
            return this;
        }

        public TimerDefinition build() {
            return timerDefinition;
        }
    }
}

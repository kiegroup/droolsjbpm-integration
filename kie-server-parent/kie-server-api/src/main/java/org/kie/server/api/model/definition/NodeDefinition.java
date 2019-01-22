/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
@XmlRootElement(name = "node-definition")
public class NodeDefinition {

    @XmlElement(name="id")
    private Long id;

    @XmlElement(name="name")
    private String name;

    @XmlElement(name="unique-id")
    private String uniqueId;

    @XmlElement(name="type")
    private String type;

    public NodeDefinition() {
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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "NodeDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public static final class Builder {

        private NodeDefinition nodeDefinition;

        private Builder() {
            nodeDefinition = new NodeDefinition();
        }

        public Builder id(Long id) {
            nodeDefinition.setId(id);
            return this;
        }

        public Builder name(String name) {
            nodeDefinition.setName(name);
            return this;
        }

        public Builder uniqueId(String uniqueId) {
            nodeDefinition.setUniqueId(uniqueId);
            return this;
        }

        public Builder type(String type) {
            nodeDefinition.setType(type);
            return this;
        }

        public NodeDefinition build() {
            return nodeDefinition;
        }
    }
}

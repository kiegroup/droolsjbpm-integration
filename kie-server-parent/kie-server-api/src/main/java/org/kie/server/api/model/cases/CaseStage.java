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

package org.kie.server.api.model.cases;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.instance.NodeInstance;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-stage")
public class CaseStage {

    @XmlElement(name="stage-name")
    private String name;
    @XmlElement(name="stage-id")
    private String identifier;
    @XmlElement(name="stage-status")
    private String status;
    @XmlElement(name="adhoc-fragments")
    private List<CaseAdHocFragment> adHocFragments;
    @XmlElement(name="active-nodes")
    private List<NodeInstance> activeNodes;

    public CaseStage() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CaseAdHocFragment> getAdHocFragments() {
        return adHocFragments;
    }

    public void setAdHocFragments(List<CaseAdHocFragment> adHocFragments) {
        this.adHocFragments = adHocFragments;
    }

    public List<NodeInstance> getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(List<NodeInstance> activeNodes) {
        this.activeNodes = activeNodes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseStage stage = new CaseStage();

        public CaseStage build() {
            return stage;
        }

        public Builder id(String id) {
            stage.setIdentifier(id);
            return this;
        }

        public Builder name(String name) {
            stage.setName(name);
            return this;
        }

        public Builder status(String status) {
            stage.setStatus(status);
            return this;
        }

        public Builder adHocFragments(List<CaseAdHocFragment> adHocFragments) {
            stage.setAdHocFragments(adHocFragments);
            return this;
        }

        public Builder activeNodes(List<NodeInstance> activeNodes) {
            stage.setActiveNodes(activeNodes);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseStage{" +
                "name='" + name + '\'' +
                ", id=" + identifier +
                ", status=" + status +
                '}';
    }
}

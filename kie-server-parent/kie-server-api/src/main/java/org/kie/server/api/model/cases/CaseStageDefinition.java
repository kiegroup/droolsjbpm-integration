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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-stage-def")
public class CaseStageDefinition {

    @XmlElement(name = "stage-name")
    private String name;
    @XmlElement(name = "stage-id")
    private String identifier;
    @XmlElement(name = "adhoc-fragments")
    private List<CaseAdHocFragment> adHocFragments;

    public CaseStageDefinition() {

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

    public List<CaseAdHocFragment> getAdHocFragments() {
        return adHocFragments;
    }

    public void setAdHocFragments(List<CaseAdHocFragment> adHocFragments) {
        this.adHocFragments = adHocFragments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseStageDefinition stage = new CaseStageDefinition();

        public CaseStageDefinition build() {
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

        public Builder adHocFragments(List<CaseAdHocFragment> adHocFragments) {
            stage.setAdHocFragments(adHocFragments);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseStageDefinition{" +
                "name='" + name + '\'' +
                ", id=" + identifier +
                '}';
    }
}

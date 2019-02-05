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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-milestone-def")
public class CaseMilestoneDefinition {

    @XmlElement(name = "milestone-name")
    private String name;
    @XmlElement(name = "milestone-id")
    private String identifier;
    @XmlElement(name = "milestone-mandatory")
    private boolean mandatory;

    public CaseMilestoneDefinition() {

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

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseMilestoneDefinition milestone = new CaseMilestoneDefinition();

        public CaseMilestoneDefinition build() {
            return milestone;
        }

        public Builder id(String id) {
            milestone.setIdentifier(id);
            return this;
        }

        public Builder name(String name) {
            milestone.setName(name);
            return this;
        }

        public Builder mandatory(boolean mandatory) {
            milestone.setMandatory(mandatory);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseMilestoneDefinition{" +
                "name='" + name + '\'' +
                ", mandatory=" + mandatory +
                '}';
    }
}

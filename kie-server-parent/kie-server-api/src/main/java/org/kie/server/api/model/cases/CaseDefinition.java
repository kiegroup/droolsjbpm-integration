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
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-definition")
public class CaseDefinition {

    @XmlElement(name="name")
    private String name;
    @XmlElement(name="id")
    private String identifier;
    @XmlElement(name="version")
    private String version;
    @XmlElement(name="case-id-prefix")
    private String caseIdPrefix;
    @XmlElement(name="container-id")
    private String containerId;
    @XmlElement(name="adhoc-fragments")
    private List<CaseAdHocFragment> adHocFragments;
    @XmlElement(name="roles")
    private Map<String, Integer> roles;
    @XmlElement(name="milestones")
    private List<CaseMilestoneDefinition> milestones;
    @XmlElement(name="stages")
    private List<CaseStageDefinition> caseStages;

    public CaseDefinition() {

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

    public String getCaseIdPrefix() {
        return caseIdPrefix;
    }

    public void setCaseIdPrefix(String caseIdPrefix) {
        this.caseIdPrefix = caseIdPrefix;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public List<CaseAdHocFragment> getAdHocFragments() {
        return adHocFragments;
    }

    public void setAdHocFragments(List<CaseAdHocFragment> adHocFragments) {
        this.adHocFragments = adHocFragments;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Integer> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Integer> roles) {
        this.roles = roles;
    }

    public List<CaseStageDefinition> getCaseStages() {
        return caseStages;
    }

    public void setCaseStages(List<CaseStageDefinition> caseStages) {
        this.caseStages = caseStages;
    }

    public List<CaseMilestoneDefinition> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<CaseMilestoneDefinition> milestones) {
        this.milestones = milestones;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseDefinition caseDefinition = new CaseDefinition();

        public CaseDefinition build() {
            return caseDefinition;
        }

        public Builder id(String id) {
            caseDefinition.setIdentifier(id);
            return this;
        }

        public Builder name(String name) {
            caseDefinition.setName(name);
            return this;
        }

        public Builder version(String version) {
            caseDefinition.setVersion(version);
            return this;
        }

        public Builder caseIdPrefix(String caseIdPrefix) {
            caseDefinition.setCaseIdPrefix(caseIdPrefix);
            return this;
        }

        public Builder containerId(String containerId) {
            caseDefinition.setContainerId(containerId);
            return this;
        }

        public Builder adHocFragments(List<CaseAdHocFragment> adHocFragments) {
            caseDefinition.setAdHocFragments(adHocFragments);
            return this;
        }

        public Builder stages(List<CaseStageDefinition> stages) {
            caseDefinition.setCaseStages(stages);
            return this;
        }

        public Builder roles(Map<String, Integer> roles) {
            caseDefinition.setRoles(roles);
            return this;
        }

        public Builder milestones(List<CaseMilestoneDefinition> milestones) {
            caseDefinition.setMilestones(milestones);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseDefinition{" +
                "name='" + name + '\'' +
                ", identifier='" + identifier + '\'' +
                ", version='" + version + '\'' +
                ", caseIdPrefix='" + caseIdPrefix + '\'' +
                ", containerId='" + containerId + '\'' +
                '}';
    }
}

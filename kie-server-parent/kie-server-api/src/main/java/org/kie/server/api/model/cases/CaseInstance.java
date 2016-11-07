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

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-instance")
public class CaseInstance {

    @XmlElement(name="case-id")
    private String caseId;

    @XmlElement(name="case-description")
    private String caseDescription;

    @XmlElement(name="case-owner")
    private String caseOwner;

    @XmlElement(name="case-status")
    private Integer caseStatus;

    @XmlElement(name="case-definition-id")
    private String caseDefinitionId;

    @XmlElement(name="container-id")
    private String containerId;

    @XmlElement(name="case-started-at")
    private Date startedAt;

    @XmlElement(name="case-completed-at")
    private Date completedAt;

    @XmlElement(name="case-completion-msg")
    private String completionMessage;

    @XmlElement(name="case-file")
    private CaseFile caseFile;

    @XmlElement(name="case-milestones")
    private List<CaseMilestone> milestones;

    @XmlElement(name="case-stages")
    private List<CaseStage> stages;

    @XmlElement(name="case-roles")
    private List<CaseRoleAssignment> roleAssignments;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseDescription() {
        return caseDescription;
    }

    public void setCaseDescription(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    public String getCaseOwner() {
        return caseOwner;
    }

    public void setCaseOwner(String caseOwner) {
        this.caseOwner = caseOwner;
    }

    public Integer getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(Integer caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public void setCaseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletionMessage() {
        return completionMessage;
    }

    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }

    public CaseFile getCaseFile() {
        return caseFile;
    }

    public void setCaseFile(CaseFile caseFile) {
        this.caseFile = caseFile;
    }

    public List<CaseMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<CaseMilestone> milestones) {
        this.milestones = milestones;
    }

    public List<CaseStage> getStages() {
        return stages;
    }

    public void setStages(List<CaseStage> stages) {
        this.stages = stages;
    }

    public List<CaseRoleAssignment> getRoleAssignments() {
        return roleAssignments;
    }

    public void setRoleAssignments(List<CaseRoleAssignment> roleAssignments) {
        this.roleAssignments = roleAssignments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseInstance caseInstance = new CaseInstance();

        public CaseInstance build() {
            return caseInstance;
        }

        public Builder caseId(String caseId) {
            caseInstance.setCaseId(caseId);
            return this;
        }

        public Builder caseDescription(String description) {
            caseInstance.setCaseDescription(description);
            return this;
        }

        public Builder caseOwner(String owner) {
            caseInstance.setCaseOwner(owner);
            return this;
        }

        public Builder caseStatus(Integer status) {
            caseInstance.setCaseStatus(status);
            return this;
        }

        public Builder caseDefinitionId(String definitionId) {
            caseInstance.setCaseDefinitionId(definitionId);
            return this;
        }

        public Builder containerId(String containerId) {
            caseInstance.setContainerId(containerId);
            return this;
        }

        public Builder startedAt(Date startedAt) {
            caseInstance.setStartedAt(startedAt);
            return this;
        }

        public Builder completedAt(Date completedAt) {
            caseInstance.setCompletedAt(completedAt);
            return this;
        }

        public Builder completionMessage(String completionMessage) {
            caseInstance.setCompletionMessage(completionMessage==null?"":completionMessage);
            return this;
        }

        public Builder caseFile(CaseFile caseFile) {
            caseInstance.setCaseFile(caseFile);
            return this;
        }

        public Builder milestones(List<CaseMilestone> milestones) {
            caseInstance.setMilestones(milestones);
            return this;
        }

        public Builder stages(List<CaseStage> stages) {
            caseInstance.setStages(stages);
            return this;
        }

        public Builder roleAssignments(List<CaseRoleAssignment> roleAssignments) {
            caseInstance.setRoleAssignments(roleAssignments);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseInstance{" +
                "caseId='" + caseId + '\'' +
                ", caseDescription='" + caseDescription + '\'' +
                ", caseOwner='" + caseOwner + '\'' +
                ", caseStatus=" + caseStatus +
                ", caseDefinitionId='" + caseDefinitionId + '\'' +
                ", containerId='" + containerId + '\'' +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", completionMessage='" + completionMessage + '\'' +
                '}';
    }
}

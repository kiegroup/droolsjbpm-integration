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
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-instance")
public class TaskInstance {

    @XmlElement(name = "task-id")
    private Long id;
    @XmlElement(name = "task-priority")
    private Integer priority;
    @XmlElement(name = "task-name")
    private String name;
    @XmlElement(name = "task-subject")
    private String subject;
    @XmlElement(name = "task-description")
    private String description;
    @XmlElement(name = "task-type")
    private String taskType;
    @XmlElement(name = "task-form")
    private String formName;
    @XmlElement(name = "task-status")
    private String status;
    @XmlElement(name = "task-actual-owner")
    private String actualOwner;
    @XmlElement(name = "task-created-by")
    private String createdBy;
    @XmlElement(name = "task-created-on")
    private Date createdOn;
    @XmlElement(name = "task-activation-time")
    private Date activationTime;
    @XmlElement(name = "task-expiration-time")
    private Date expirationDate;
    @XmlElement(name = "task-skippable")
    private Boolean skipable;
    @XmlElement(name = "task-workitem-id")
    private Long workItemId;
    @XmlElement(name = "task-process-instance-id")
    private Long processInstanceId;
    @XmlElement(name = "task-parent-id")
    private Long parentId;
    @XmlElement(name = "task-process-id")
    private String processId;
    @XmlElement(name = "task-container-id")
    private String containerId;

    @XmlElementWrapper(name = "potential-owners")
    @XmlElement(name = "task-pot-owners")
    private List<String> potentialOwners;

    @XmlElementWrapper(name = "excluded-owners")
    @XmlElement(name = "task-excl-owners")
    private List<String> excludedOwners;

    @XmlElementWrapper(name = "business-admins")
    @XmlElement(name = "task-business-admins")
    private List<String> businessAdmins;

    @XmlElement(name = "task-input-data")
    private Map<String, Object> inputData;

    @XmlElement(name = "task-output-data")
    private Map<String, Object> outputData;

    public TaskInstance() {
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getSkipable() {
        return skipable;
    }

    public void setSkipable(Boolean skipable) {
        this.skipable = skipable;
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public List<String> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(List<String> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public List<String> getExcludedOwners() {
        return excludedOwners;
    }

    public void setExcludedOwners(List<String> excludedOwners) {
        this.excludedOwners = excludedOwners;
    }

    public List<String> getBusinessAdmins() {
        return businessAdmins;
    }

    public void setBusinessAdmins(List<String> businessAdmins) {
        this.businessAdmins = businessAdmins;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }

    public Map<String, Object> getOutputData() {
        return outputData;
    }

    public void setOutputData(Map<String, Object> outputData) {
        this.outputData = outputData;
    }

    @Override
    public String toString() {
        return "TaskInstance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", actualOwner='" + actualOwner + '\'' +
                ", processInstanceId=" + processInstanceId +
                ", processId='" + processId + '\'' +
                ", containerId='" + containerId + '\'' +
                '}';
    }

    public static class Builder {

        private TaskInstance taskInstance = new TaskInstance();

        public TaskInstance build() {
            return taskInstance;
        }

        public Builder id(Long id) {
            taskInstance.setId(id);
            return this;
        }

        public Builder priority(Integer priority) {
            taskInstance.setPriority(priority);
            return this;
        }

        public Builder name(String name) {
            taskInstance.setName(name);
            return this;
        }

        public Builder subject(String subject) {
            taskInstance.setSubject(subject);
            return this;
        }

        public Builder description(String description) {
            taskInstance.setDescription(description);
            return this;
        }

        public Builder taskType(String taskType) {
            taskInstance.setTaskType(taskType);
            return this;
        }

        public Builder formName(String formName) {
            taskInstance.setFormName(formName);
            return this;
        }

        public Builder status(String status) {
            taskInstance.setStatus(status);
            return this;
        }

        public Builder actualOwner(String actualOwner) {
            taskInstance.setActualOwner(actualOwner);
            return this;
        }

        public Builder createdBy(String createdBy) {
            taskInstance.setCreatedBy(createdBy);
            return this;
        }

        public Builder createdOn(Date createdOn) {
            taskInstance.setCreatedOn(createdOn == null ? createdOn : new Date(createdOn.getTime()));
            return this;
        }

        public Builder activationTime(Date activationTime) {
            taskInstance.setActivationTime(activationTime == null ? activationTime : new Date(activationTime.getTime()));
            return this;
        }

        public Builder expirationTime(Date expirationTime) {
            taskInstance.setExpirationDate(expirationTime == null ? expirationTime : new Date(expirationTime.getTime()));
            return this;
        }

        public Builder skippable(Boolean skippable) {
            taskInstance.setSkipable(skippable);
            return this;
        }

        public Builder workItemId(Long workItemId) {
            taskInstance.setWorkItemId(workItemId);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            taskInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder parentId(Long parentId) {
            taskInstance.setParentId(parentId);
            return this;
        }

        public Builder processId(String processId) {
            taskInstance.setProcessId(processId);
            return this;
        }

        public Builder containerId(String containerId) {
            taskInstance.setContainerId(containerId);
            return this;
        }

        public Builder potentialOwners(List<String> potOwners) {
            taskInstance.setPotentialOwners(potOwners);
            return this;
        }

        public Builder excludedOwners(List<String> exclOwners) {
            taskInstance.setExcludedOwners(exclOwners);
            return this;
        }

        public Builder businessAdmins(List<String> businessAdmins) {
            taskInstance.setBusinessAdmins(businessAdmins);
            return this;
        }

        public Builder inputData(Map<String, Object> inputData) {
            taskInstance.setInputData(inputData);
            return this;
        }

        public Builder outputData(Map<String, Object> outputData) {
            taskInstance.setOutputData(outputData);
            return this;
        }
    }
}

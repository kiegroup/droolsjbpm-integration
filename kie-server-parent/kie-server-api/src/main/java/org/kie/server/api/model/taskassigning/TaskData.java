/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.taskassigning;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.internal.jaxb.LocalDateTimeXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-assigning-task-data")
public class TaskData {

    @XmlElement(name = "task-id")
    private Long taskId;

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    @XmlElement(name = "created-on")
    private LocalDateTime createdOn;

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    @XmlElement(name = "last-modification-date")
    private LocalDateTime lastModificationDate;

    @XmlElement(name = "proc-inst-id")
    private Long processInstanceId;

    @XmlElement(name = "proc-id")
    private String processId;

    @XmlElement(name = "container-id")
    private String containerId;

    @XmlElement(name = "status")
    private String status;

    @XmlElement(name = "priority")
    private Integer priority;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "actual-owner")
    private String actualOwner;

    @XmlElementWrapper(name = "task-potential-owners")
    @XmlElement(name = "pot-owners")
    private Set<OrganizationalEntity> potentialOwners;

    @XmlElement(name = "task-input-data")
    private Map<String, Object> inputData;

    @XmlElement(name = "planning-task")
    private PlanningTask planningTask;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(LocalDateTime lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public Set<OrganizationalEntity> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(Set<OrganizationalEntity> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }

    public PlanningTask getPlanningTask() {
        return planningTask;
    }

    public void setPlanningTask(PlanningTask planningTask) {
        this.planningTask = planningTask;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TaskData taskData = new TaskData();

        private Builder() {
        }

        public Builder taskId(Long taskId) {
            taskData.setTaskId(taskId);
            return this;
        }

        public Builder createdOn(LocalDateTime createdOn) {
            taskData.setCreatedOn(createdOn);
            return this;
        }

        public Builder lastModificationDate(LocalDateTime lastModificationDate) {
            taskData.setLastModificationDate(lastModificationDate);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            taskData.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder processId(String processId) {
            taskData.setProcessId(processId);
            return this;
        }

        public Builder containerId(String containerId) {
            taskData.setContainerId(containerId);
            return this;
        }

        public Builder status(String status) {
            taskData.setStatus(status);
            return this;
        }

        public Builder priority(Integer priority) {
            taskData.setPriority(priority);
            return this;
        }

        public Builder name(String name) {
            taskData.setName(name);
            return this;
        }

        public Builder actualOwner(String actualOwner) {
            taskData.setActualOwner(actualOwner);
            return this;
        }

        public Builder potentialOwners(Set<OrganizationalEntity> potentialOwners) {
            taskData.setPotentialOwners(potentialOwners);
            return this;
        }

        public Builder inputData(Map<String, Object> inputData) {
            taskData.setInputData(inputData);
            return this;
        }

        public Builder planningTask(PlanningTask planningTask) {
            taskData.setPlanningTask(planningTask);
            return this;
        }

        public TaskData build() {
            return taskData;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskData)) {
            return false;
        }
        TaskData taskData = (TaskData) o;
        return Objects.equals(taskId, taskData.taskId) &&
                Objects.equals(createdOn, taskData.createdOn) &&
                Objects.equals(lastModificationDate, taskData.lastModificationDate) &&
                Objects.equals(processInstanceId, taskData.processInstanceId) &&
                Objects.equals(processId, taskData.processId) &&
                Objects.equals(containerId, taskData.containerId) &&
                Objects.equals(status, taskData.status) &&
                Objects.equals(priority, taskData.priority) &&
                Objects.equals(name, taskData.name) &&
                Objects.equals(actualOwner, taskData.actualOwner) &&
                Objects.equals(potentialOwners, taskData.potentialOwners) &&
                Objects.equals(inputData, taskData.inputData) &&
                Objects.equals(planningTask, taskData.planningTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, createdOn, lastModificationDate, processInstanceId, processId, containerId, status, priority, name, actualOwner, potentialOwners, inputData, planningTask);
    }

    @Override
    public String toString() {
        return "TaskData{" +
                "taskId=" + taskId +
                ", createdOn=" + createdOn +
                ", lastModificationDate=" + lastModificationDate +
                ", processInstanceId=" + processInstanceId +
                ", processId='" + processId + '\'' +
                ", containerId='" + containerId + '\'' +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                ", name='" + name + '\'' +
                ", actualOwner='" + actualOwner + '\'' +
                ", potentialOwners=" + potentialOwners +
                ", inputData=" + inputData +
                ", planningTask=" + planningTask +
                '}';
    }
}

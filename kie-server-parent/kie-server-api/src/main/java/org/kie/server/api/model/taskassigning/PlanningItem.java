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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the information for executing a planning into the jBPM engine.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-assigning-planning-item")
public class PlanningItem {

    @XmlElement(name = "container-id")
    private String containerId;

    @XmlElement(name = "task-id")
    private Long taskId;

    @XmlElement(name = "proc-inst-id")
    private Long processInstanceId;

    @XmlElement(name = "planning-task")
    private PlanningTask planningTask;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public PlanningTask getPlanningTask() {
        return planningTask;
    }

    public void setPlanningTask(PlanningTask planningTask) {
        this.planningTask = planningTask;
    }

    @Override
    public String toString() {
        return "PlanningItem{" +
                "containerId='" + containerId + '\'' +
                ", taskId=" + taskId +
                ", processInstanceId=" + processInstanceId +
                ", planningTask=" + planningTask +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private PlanningItem item = new PlanningItem();

        private Builder() {
        }

        public PlanningItem build() {
            return item;
        }

        public Builder taskId(Long taskId) {
            item.setTaskId(taskId);
            return this;
        }

        public Builder containerId(String containerId) {
            item.setContainerId(containerId);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            item.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder planningTask(PlanningTask planningTask) {
            item.setPlanningTask(planningTask);
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanningItem)) {
            return false;
        }
        PlanningItem that = (PlanningItem) o;
        return Objects.equals(containerId, that.containerId) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(processInstanceId, that.processInstanceId) &&
                Objects.equals(planningTask, that.planningTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId, taskId, processInstanceId, planningTask);
    }
}

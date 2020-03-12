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
 * Defines the information configured/assigned by OptaPlanner when a solution is planned into the jBPM runtime.
 * This information is good enough for restoring the solution and start the solver.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-assigning-planning-task")
public class PlanningTask {

    @XmlElement(name = "task-id")
    private Long taskId;

    @XmlElement(name = "assigned-user")
    private String assignedUser;

    @XmlElement(name = "index")
    private Integer index;

    @XmlElement(name = "published")
    private Boolean published;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public boolean isPublished() {
        return published != null && published;
    }

    @Override
    public String toString() {
        return "PlanningTask{" +
                "taskId=" + taskId +
                ", assignedUser='" + assignedUser + '\'' +
                ", index=" + index +
                ", published=" + published +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private PlanningTask task = new PlanningTask();

        private Builder() {
        }

        public Builder taskId(Long taskId) {
            task.setTaskId(taskId);
            return this;
        }

        public Builder assignedUser(String assignedUser) {
            task.setAssignedUser(assignedUser);
            return this;
        }

        public Builder index(Integer index) {
            task.setIndex(index);
            return this;
        }

        public Builder published(Boolean published) {
            task.setPublished(published);
            return this;
        }

        public PlanningTask build() {
            return task;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanningTask)) {
            return false;
        }
        PlanningTask that = (PlanningTask) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(assignedUser, that.assignedUser) &&
                Objects.equals(index, that.index) &&
                Objects.equals(published, that.published);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, assignedUser, index, published);
    }
}

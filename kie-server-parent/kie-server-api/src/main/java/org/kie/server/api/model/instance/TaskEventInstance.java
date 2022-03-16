/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.instance;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-event-instance")
public class TaskEventInstance {

    @XmlElement(name = "task-event-id")
    private Long id;

    @XmlElement(name = "task-id")
    private Long taskId;

    @XmlElement(name = "task-event-type")
    private String type;

    @XmlElement(name = "task-event-user")
    private String userId;

    @XmlElement(name = "task-event-date")
    private Date logTime;

    @XmlElement(name = "task-process-instance-id")
    private Long processInstanceId;

    @XmlElement(name = "task-work-item-id")
    private Long workItemId;

    @XmlElement(name = "task-event-message")
    private String message;
    

    @XmlElement(name = "correlation-key")
    private String correlationKey;

    @XmlElement(name = "process-type")
    private Integer processType;

    @XmlElement(name = "assigned-owner")
    private String assignedOwner;

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Long getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    
    public String getCorrelationKey() {
        return correlationKey;
    }

    
    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    
    public Integer getProcessType() {
        return processType;
    }

    
    public void setProcessType(Integer processType) {
        this.processType = processType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAssignedOwner() {
        return assignedOwner;
    }

    public void setAssignedOwner(String actualOwner) {
        this.assignedOwner = actualOwner;
    }

    @Override
    public String toString() {
        return "TaskEventInstance [id=" + id + ", taskId=" + taskId + ", type=" + type + ", userId=" + userId + ", logTime=" + logTime + ", processInstanceId=" + processInstanceId + ", workItemId=" + workItemId +
               ", message=" + message + ", correlationKey=" + correlationKey + ", processType=" + processType + ", actualOwner=" + assignedOwner + "]";
    }

    public static class Builder {

        private TaskEventInstance taskEventInstance = new TaskEventInstance();

        public TaskEventInstance build() {
            return taskEventInstance;
        }

        public Builder id(Long id) {
            taskEventInstance.setId(id);
            return this;
        }

        public Builder taskId(Long taskId) {
            taskEventInstance.setTaskId(taskId);
            return this;
        }

        public Builder type(String type) {
            taskEventInstance.setType(type);
            return this;
        }

        public Builder user(String user) {
            taskEventInstance.setUserId(user);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            taskEventInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder workItemId(Long workItemId) {
            taskEventInstance.setWorkItemId(workItemId);
            return this;
        }

        public Builder correlationKey(String correlationKey) {
            taskEventInstance.setCorrelationKey(correlationKey);
            return this;
        }

        public Builder processType (Integer processType) {
            taskEventInstance.setProcessType(processType);
            return this;
        }

        public Builder date(Date date) {
            taskEventInstance.setLogTime(date == null ? date : new Date(date.getTime()));
            return this;
        }

        public Builder message(String message) {
            taskEventInstance.setMessage(message);
            return this;
        }
        
        public Builder assignedOwner (String actualOwner) {
            taskEventInstance.setAssignedOwner(actualOwner);
            return this;
        }
    }
}

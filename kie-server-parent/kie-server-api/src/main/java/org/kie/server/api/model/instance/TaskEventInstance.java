/*
 * Copyright 2015 JBoss Inc
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-event-instance")
public class TaskEventInstance {

    @XmlElement(name="task-event-id")
    private Long id;

    @XmlElement(name="task-id")
    private Long taskId;

    @XmlElement(name="task-event-type")
    private String type;

    @XmlElement(name="task-event-user")
    private String userId;

    @XmlElement(name="task-event-date")
    private Date logTime;

    @XmlElement(name="task-process-instance-id")
    private Long processInstanceId;

    @XmlElement(name="task-work-item-id")
    private Long workItemId;

    public TaskEventInstance() {
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

        public Builder date(Date date) {
            taskEventInstance.setLogTime(date);
            return this;
        }
    }
}

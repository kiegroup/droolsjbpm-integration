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
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-instance")
public class ProcessInstance {

    @XmlElement(name="process-instance-id")
    private Long id;
    @XmlElement(name="process-id")
    private String processId;
    @XmlElement(name="process-name")
    private String processName;
    @XmlElement(name="process-version")
    private String processVersion;
    @XmlElement(name="process-instance-state")
    private Integer state;
    @XmlElement(name="container-id")
    private String containerId;
    @XmlElement(name="initiator")
    private String initiator;
    @XmlElement(name="start-date")
    private Date date;
    @XmlElement(name="process-instance-desc")
    private String processInstanceDescription;
    @XmlElement(name="correlation-key")
    private String correlationKey;
    @XmlElement(name="parent-instance-id")
    private Long parentId;

    @XmlElement(name="active-user-tasks")
    private TaskSummaryList activeUserTasks;

    @XmlElement(name="process-instance-variables")
    private Map<String, Object> variables;

    public ProcessInstance() {
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

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getProcessInstanceDescription() {
        return processInstanceDescription;
    }

    public void setProcessInstanceDescription(String processInstanceDescription) {
        this.processInstanceDescription = processInstanceDescription;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getVariables() {


        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public TaskSummaryList getActiveUserTasks() {
        return activeUserTasks;
    }

    public void setActiveUserTasks(TaskSummaryList activeUserTasks) {
        this.activeUserTasks = activeUserTasks;
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id=" + id +
                ", processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", state=" + state +
                ", containerId='" + containerId + '\'' +
                ", correlationKey='" + correlationKey + '\'' +
                '}';
    }

    public static class Builder {
        private ProcessInstance processInstance = new ProcessInstance();

        public ProcessInstance build() {
            return processInstance;
        }

        public Builder id(Long id) {
            processInstance.setId(id);
            return this;
        }

        public Builder processId(String processId) {
            processInstance.setProcessId(processId);
            return this;
        }

        public Builder processName(String processName) {
            processInstance.setProcessName(processName);
            return this;
        }

        public Builder processVersion(String processVersion) {
            processInstance.setProcessVersion(processVersion);
            return this;
        }

        public Builder state(Integer state) {
            processInstance.setState(state);
            return this;
        }

        public Builder containerId(String containerId) {
            processInstance.setContainerId(containerId);
            return this;
        }

        public Builder initiator(String initiator) {
            processInstance.setInitiator(initiator);
            return this;
        }

        public Builder date(Date date) {
            processInstance.setDate(date);
            return this;
        }

        public Builder processInstanceDescription(String description) {
            processInstance.setProcessInstanceDescription(description);
            return this;
        }

        public Builder correlationKey(String correlationKey) {
            processInstance.setCorrelationKey(correlationKey);
            return this;
        }

        public Builder parentInstanceId(Long parentInstanceId) {
            processInstance.setParentId(parentInstanceId);
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            processInstance.setVariables(variables);
            return this;
        }

        public Builder activeUserTasks(TaskSummaryList activeUserTasks) {
            processInstance.setActiveUserTasks(activeUserTasks);
            return this;
        }
    }
}

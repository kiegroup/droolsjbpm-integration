/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.admin;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "execution-error")
public class ExecutionErrorInstance {

    @XmlElement(name = "id")
    private String errorId;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "container-id")
    private String containerId;
    @XmlElement(name = "process-instance-id")
    private Long processInstanceId;
    @XmlElement(name = "process-id")
    private String processId;
    @XmlElement(name = "activity-id")
    private Long activityId;
    @XmlElement(name = "activity-name")
    private String activityName;
    @XmlElement(name = "job-id")
    private Long jobId;

    @XmlElement(name = "error-msg")
    private String errorMessage;
    @XmlElement(name = "error")
    private String error;

    @XmlElement(name = "acknowledged")
    private boolean acknowledged;
    @XmlElement(name = "acknowledged-by")
    private String acknowledgedBy;
    @XmlElement(name = "acknowledged-at")
    private Date acknowledgedAt;

    @XmlElement(name = "error-date")
    private Date errorDate;

    public ExecutionErrorInstance() {
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public Date getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Date acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Date getErrorDate() {
        return errorDate;
    }

    public void setErrorDate(Date errorDate) {
        this.errorDate = errorDate;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    @Override
    public String toString() {
        return "ExecutionErrorInstance [type=" + type + ", containerId=" + containerId + ", processInstanceId=" + processInstanceId +
                ", processId=" + processId + ", activityId=" + activityId + ", activityName=" + activityName +
                ", errorMessage=" + errorMessage + ", acknowledged=" + acknowledged + ", acknowledgedBy=" + acknowledgedBy +
                ", acknowledgedAt=" + acknowledgedAt + "]";
    }

    public static class Builder {

        private ExecutionErrorInstance error = new ExecutionErrorInstance();

        public ExecutionErrorInstance build() {
            return error;
        }

        public Builder errorId(String errorId) {
            error.setErrorId(errorId);
            return this;
        }

        public Builder type(String type) {
            error.setType(type);
            return this;
        }

        public Builder containerId(String containerId) {
            error.setContainerId(containerId);
            return this;
        }

        public Builder message(String message) {
            error.setErrorMessage(message);
            return this;
        }

        public Builder error(String errorStr) {
            error.setError(errorStr);
            return this;
        }

        public Builder acknowledgedBy(String user) {
            error.setAcknowledgedBy(user);
            return this;
        }

        public Builder processInstanceId(Long piId) {
            error.setProcessInstanceId(piId);
            return this;
        }

        public Builder activityId(Long activityId) {
            error.setActivityId(activityId);
            return this;
        }

        public Builder acknowledged(boolean acknowledged) {
            error.setAcknowledged(acknowledged);
            return this;
        }

        public Builder acknowledgedAt(Date acknowledgedAt) {
            error.setAcknowledgedAt(acknowledgedAt);
            return this;
        }

        public Builder processId(String processId) {
            error.setProcessId(processId);
            return this;
        }

        public Builder activityName(String activityName) {
            error.setActivityName(activityName);
            return this;
        }

        public Builder errorDate(Date errorDate) {
            error.setErrorDate(errorDate);
            return this;
        }

        public Builder jobId(Long jobId) {
            error.setJobId(jobId);
            return this;
        }
    }
}

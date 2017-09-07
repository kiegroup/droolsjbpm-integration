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
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request-info-instance")
public class RequestInfoInstance {

    @XmlElement(name="request-instance-id")
    private Long id;
    @XmlElement(name="request-status")
    private String status;
    @XmlElement(name="request-business-key")
    private String businessKey;
    @XmlElement(name="request-message")
    private String message;
    @XmlElement(name="request-retries")
    private Integer retries;
    @XmlElement(name="request-executions")
    private Integer executions;
    @XmlElement(name="request-command")
    private String commandName;
    @XmlElement(name="request-scheduled-date")
    private Date scheduledDate;
    @XmlElement(name="request-data")
    private Map<String, Object> data;
    @XmlElement(name="response-data")
    private Map<String, Object> responseData;
    @XmlElement(name="request-errors")
    private ErrorInfoInstanceList errors;
    @XmlElement(name="request-container-id")
    private String containerId;

    public RequestInfoInstance() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getExecutions() {
        return executions;
    }

    public void setExecutions(Integer executions) {
        this.executions = executions;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public ErrorInfoInstanceList getErrors() {
        return errors;
    }

    public void setErrors(ErrorInfoInstanceList errors) {
        this.errors = errors;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }

    public void setResponseData(Map<String, Object> responseData) {
        this.responseData = responseData;
    }

    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public String toString() {
        return "RequestInfoInstance{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", businessKey='" + businessKey + '\'' +
                ", retries=" + retries +
                ", executions=" + executions +
                ", commandName='" + commandName + '\'' +
                ", scheduledDate=" + scheduledDate +
                '}';
    }

    public static class Builder {

        private RequestInfoInstance requestInfoInstance = new RequestInfoInstance();

        public RequestInfoInstance build() {
            return requestInfoInstance;
        }

        public Builder id(Long id) {
            requestInfoInstance.setId(id);
            return this;
        }

        public Builder status(String status) {
            requestInfoInstance.setStatus(status);
            return this;
        }

        public Builder businessKey(String businessKey) {
            requestInfoInstance.setBusinessKey(businessKey);
            return this;
        }

        public Builder message(String message) {
            requestInfoInstance.setMessage(message);
            return this;
        }

        public Builder retries(Integer retries) {
            requestInfoInstance.setRetries(retries);
            return this;
        }

        public Builder executions(Integer executions) {
            requestInfoInstance.setExecutions(executions);
            return this;
        }

        public Builder command(String command) {
            requestInfoInstance.setCommandName(command);
            return this;
        }

        public Builder scheduledDate(Date date) {
            requestInfoInstance.setScheduledDate(date == null ? date : new Date(date.getTime()));
            return this;
        }

        public Builder data(Map<String, Object> data) {
            requestInfoInstance.setData(data);
            return this;
        }

        public Builder responseData(Map<String, Object> data) {
            requestInfoInstance.setResponseData(data);
            return this;
        }

        public Builder errors(ErrorInfoInstanceList errors) {
            requestInfoInstance.setErrors(errors);
            return this;
        }
        
        public Builder containerId(String containerId) {
            requestInfoInstance.setContainerId(containerId);
            return this;
        }
    }
}

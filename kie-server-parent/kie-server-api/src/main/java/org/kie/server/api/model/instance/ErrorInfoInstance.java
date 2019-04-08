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
@XmlRootElement(name = "error-info-instance")
public class ErrorInfoInstance {

    @XmlElement(name = "error-instance-id")
    private Long id;

    @XmlElement(name = "request-instance-id")
    private Long requestInfoId;

    @XmlElement(name = "error-message")
    private String message;

    @XmlElement(name = "error-stacktrace")
    private String stacktrace;

    @XmlElement(name = "error-date")
    private Date errorDate;

    public ErrorInfoInstance() {
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

    public Long getRequestInfoId() {
        return requestInfoId;
    }

    public void setRequestInfoId(Long requestInfoId) {
        this.requestInfoId = requestInfoId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public Date getErrorDate() {
        return errorDate;
    }

    public void setErrorDate(Date errorDate) {
        this.errorDate = errorDate;
    }

    @Override
    public String toString() {
        return "ErrorInfoInstance{" +
                "id=" + id +
                ", requestInfoId=" + requestInfoId +
                ", message='" + message + '\'' +
                ", errorDate=" + errorDate +
                '}';
    }

    public static class Builder {

        private ErrorInfoInstance errorInfoInstance = new ErrorInfoInstance();

        public ErrorInfoInstance build() {
            return errorInfoInstance;
        }

        public Builder id(Long id) {
            errorInfoInstance.setId(id);
            return this;
        }

        public Builder requestId(Long id) {
            errorInfoInstance.setRequestInfoId(id);
            return this;
        }

        public Builder stacktrace(String stacktrace) {
            errorInfoInstance.setStacktrace(stacktrace);
            return this;
        }

        public Builder errorDate(Date date) {
            errorInfoInstance.setErrorDate(date == null ? date : new Date(date.getTime()));
            return this;
        }

        public Builder message(String message) {
            errorInfoInstance.setMessage(message);
            return this;
        }
    }
}

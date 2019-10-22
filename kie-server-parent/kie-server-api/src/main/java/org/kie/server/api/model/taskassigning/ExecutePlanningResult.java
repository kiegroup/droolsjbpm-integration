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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tsk-assig-exec-planning-result")
public class ExecutePlanningResult {

    public enum ErrorCode {
        TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR,
        UNEXPECTED_ERROR
    }

    @XmlElement(name = "error")
    private ErrorCode error;

    @XmlElement(name = "error-message")
    private String errorMessage;

    @XmlElement(name = "container-id")
    private String containerId;

    public ErrorCode getError() {
        return error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public boolean hasError() {
        return error != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ExecutePlanningResult result = new ExecutePlanningResult();

        private Builder() {
        }

        public Builder error(ErrorCode error) {
            result.setError(error);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            result.setErrorMessage(errorMessage);
            return this;
        }

        public Builder containerId(String containerId) {
            result.setContainerId(containerId);
            return this;
        }

        public ExecutePlanningResult build() {
            return result;
        }
    }
}

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "migration-report-instance")
public class MigrationReportInstance {

    @XmlElement(name = "migration-successful")
    private boolean successful;

    @XmlElement(name = "migration-start")
    private Date startDate;

    @XmlElement(name = "migration-end")
    private Date endDate;

    @XmlElement(name = "migration-logs")
    private List<String> logs;

    @XmlElement(name = "migration-process-instance")
    private Long processInstanceId;

    public MigrationReportInstance() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getLogs() {
        if (logs == null) {
            this.logs = new ArrayList<String>();
        }
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public static class Builder {

        private MigrationReportInstance reportInstance = new MigrationReportInstance();

        public MigrationReportInstance build() {
            return reportInstance;
        }

        public Builder successful(boolean successful) {
            reportInstance.setSuccessful(successful);
            return this;
        }

        public Builder startDate(Date startDate) {
            reportInstance.setStartDate(startDate);
            return this;
        }

        public Builder endDate(Date endDate) {
            reportInstance.setEndDate(endDate);
            return this;
        }

        public Builder logs(List<String> logs) {
            reportInstance.setLogs(logs);
            return this;
        }

        public Builder addLog(String log) {
            reportInstance.getLogs().add(log);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            reportInstance.setProcessInstanceId(processInstanceId);
            return this;
        }
    }

    @Override
    public String toString() {
        return "MigrationReportInstance{" +
                "successful=" + successful +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", processInstanceId=" + processInstanceId +
                '}';
    }
}

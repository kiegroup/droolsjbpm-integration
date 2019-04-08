/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.cases;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.admin.MigrationReportInstance;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-migration-report-instance")
public class CaseMigrationReportInstance {

    @XmlElement(name = "case-id")
    private String caseId;

    @XmlElement(name = "case-migration-successful")
    private boolean successful;

    @XmlElement(name = "case-migration-start")
    private Date startDate;

    @XmlElement(name = "case-migration-end")
    private Date endDate;

    @XmlElement(name = "case-migration-reports")
    private MigrationReportInstance[] reports;

    public static Builder builder() {
        return new Builder();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
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

    public MigrationReportInstance[] getReports() {
        return reports;
    }

    public void setReports(MigrationReportInstance[] reports) {
        this.reports = reports;
    }

    public static class Builder {

        private CaseMigrationReportInstance reportInstance = new CaseMigrationReportInstance();

        public CaseMigrationReportInstance build() {
            return reportInstance;
        }

        public Builder caseId(String caseId) {
            reportInstance.setCaseId(caseId);
            return this;
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

        public Builder reports(MigrationReportInstance[] reports) {
            reportInstance.setReports(reports);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseMigrationReportInstance [caseId=" + caseId + ", successful=" + successful + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }
}

/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.kie.server.api.model.admin.MigrationReportInstance;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "migration_reports", indexes = {@Index(columnList = "migration_id")})
@NamedQueries({
               @NamedQuery(name = "MigrationReport.findByMigrationId", query = "SELECT p FROM MigrationReport p WHERE p.migrationId = :id")
})
public class MigrationReport implements Serializable {

    private static final long serialVersionUID = 5817223334991683064L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("migration_id")
    @Column(name = "migration_id")
    private Long migrationId;

    @JsonProperty("process_instance_id")
    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @JsonProperty("start_date")
    @Column(name = "start_date")
    private Instant startDate;

    @JsonProperty("end_date")
    @Column(name = "end_date")
    private Instant endDate;

    private Boolean successful;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "log", columnDefinition = "TEXT")
    @CollectionTable(
                     name = "migration_report_logs",
                     joinColumns = @JoinColumn(name = "report_id")
    )
    private List<String> logs;

    public MigrationReport() {}

    public MigrationReport(Long migrationId, MigrationReportInstance reportInstance) {
        this.migrationId = migrationId;
        this.processInstanceId = reportInstance.getProcessInstanceId();
        if (reportInstance.getStartDate() != null) {
            this.startDate = reportInstance.getStartDate().toInstant();
        }
        if (reportInstance.getEndDate() != null) {
            this.endDate = reportInstance.getEndDate().toInstant();
        }
        this.logs = new ArrayList<>(reportInstance.getLogs());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(Long migrationId) {
        this.migrationId = migrationId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

}

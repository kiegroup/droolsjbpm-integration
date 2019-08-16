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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.kie.processmigration.model.Execution.ExecutionStatus;
import org.kie.processmigration.model.Execution.ExecutionType;

@Entity
@Table(name = "migrations")
@SequenceGenerator(name = "migrationIdSeq", sequenceName = "MIGRATION_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "Migration.findAll", query = "SELECT m FROM Migration m"),
    @NamedQuery(name = "Migration.findById", query = "SELECT m FROM Migration m WHERE m.id = :id"),
    @NamedQuery(name = "Migration.findByStatus", query = "SELECT m FROM Migration m WHERE m.status IN :statuses")
})
public class Migration implements Serializable {

    private static final long serialVersionUID = 7212317252498596171L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "migrationIdSeq")
    private Long id;

    @Embedded
    private MigrationDefinition definition;

    @JsonInclude(Include.NON_NULL)
    @Column(name = "created_at")
    private Instant createdAt;

    @JsonInclude(Include.NON_NULL)
    @Column(name = "finished_at")
    private Instant finishedAt;

    @JsonInclude(Include.NON_NULL)
    @Column(name = "started_at")
    private Instant startedAt;

    @JsonInclude(Include.NON_NULL)
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @JsonInclude(Include.NON_NULL)
    @Column(name = "error_message")
    @Lob
    private String errorMessage;

    private ExecutionStatus status;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "migration_id")
    private List<MigrationReport> reports = new ArrayList<>();

    public Migration() {
    }

    public Migration(MigrationDefinition definition) {
        this.definition = definition;
        Instant now = Instant.now();
        createdAt = now;
        if (ExecutionType.ASYNC.equals(definition.getExecution().getType()) &&
            definition.getExecution().getScheduledStartTime() != null &&
            now.isBefore(definition.getExecution().getScheduledStartTime())) {
            status = Execution.ExecutionStatus.SCHEDULED;
        } else {
            status = Execution.ExecutionStatus.CREATED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MigrationDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(MigrationDefinition definition) {
        this.definition = definition;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<MigrationReport> getReports() {
        return reports;
    }

    public void setReports(List<MigrationReport> reports) {
        this.reports = reports;
    }

    public Migration start() {
        startedAt = Instant.now();
        status = ExecutionStatus.STARTED;
        return this;
    }

    public Migration complete(Boolean hasErrors) {
        finishedAt = Instant.now();
        if (Boolean.TRUE.equals(hasErrors)) {
            status = ExecutionStatus.FAILED;
        } else {
            status = ExecutionStatus.COMPLETED;
        }
        return this;
    }

    public Migration cancel() {
        cancelledAt = Instant.now();
        status = ExecutionStatus.CANCELLED;
        return this;
    }

    public Migration fail(Exception e) {
        finishedAt = Instant.now();
        status = ExecutionStatus.FAILED;
        errorMessage = e.toString();
        return this;
    }
}

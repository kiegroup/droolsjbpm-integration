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
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "plans")
@SequenceGenerator(name = "planIdSeq", sequenceName = "PLAN_ID_SEQ")
@NamedQueries({
               @NamedQuery(name = "Plan.findAll", query = "SELECT p FROM Plan p"),
               @NamedQuery(name = "Plan.findById", query = "SELECT p FROM Plan p WHERE p.id = :id")
})
public class Plan implements Serializable {

    private static final long serialVersionUID = 1244535648642365858L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "planIdSeq")
    private long id;

    private String name;

    private String description;

    @Column(name = "source_container_id")
    private String sourceContainerId;

    @Column(name = "source_process_id")
    private String sourceProcessId;

    @Column(name = "target_container_id")
    private String targetContainerId;

    @Column(name = "target_process_id")
    private String targetProcessId;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "source")
    @Column(name = "target")
    @CollectionTable(
                     name = "plan_mappings",
                     joinColumns = @JoinColumn(name = "plan_id")
    )
    private Map<String, String> mappings;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceContainerId() {
        return sourceContainerId;
    }

    public void setSourceContainerId(String sourceContainerId) {
        this.sourceContainerId = sourceContainerId;
    }

    public String getSourceProcessId() {
        return sourceProcessId;
    }

    public void setSourceProcessId(String sourceProcessId) {
        this.sourceProcessId = sourceProcessId;
    }

    public String getTargetContainerId() {
        return targetContainerId;
    }

    public void setTargetContainerId(String targetContainerId) {
        this.targetContainerId = targetContainerId;
    }

    public String getTargetProcessId() {
        return targetProcessId;
    }

    public void setTargetProcessId(String targetProcessId) {
        this.targetProcessId = targetProcessId;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public Plan copy(Plan plan) {
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.sourceContainerId = plan.getSourceContainerId();
        this.sourceProcessId = plan.getSourceProcessId();
        this.targetContainerId = plan.getTargetContainerId();
        this.targetProcessId = plan.getTargetProcessId();
        this.mappings = plan.getMappings();
        return this;
    }
}

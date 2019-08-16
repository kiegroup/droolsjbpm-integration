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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
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

import com.fasterxml.jackson.annotation.JsonInclude;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "containerId", column = @Column(name = "source_container_id")),
        @AttributeOverride(name = "processId", column = @Column(name = "source_process_id")),
    })
    private ProcessRef source;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "containerId", column = @Column(name = "target_container_id")),
        @AttributeOverride(name = "processId", column = @Column(name = "target_process_id")),
    })
    private ProcessRef target;

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

    public ProcessRef getSource() {
        return source;
    }

    public void setSource(ProcessRef source) {
        this.source = source;
    }

    public ProcessRef getTarget() {
        return target;
    }

    public void setTarget(ProcessRef target) {
        this.target = target;
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
        this.source = plan.getSource();
        this.target = plan.getTarget();
        this.mappings = plan.getMappings();
        return this;
    }
}

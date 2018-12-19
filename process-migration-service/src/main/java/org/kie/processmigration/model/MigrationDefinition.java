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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;

@Embeddable
public class MigrationDefinition {

    @Column(name = "plan_id")
    private Long planId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
                     name = "process_instance_ids",
                     joinColumns = @JoinColumn(name = "migration_definition_id")
    )
    private List<Long> processInstanceIds;

    private String kieserverId;

    private String requester;

    @Embedded
    private Execution execution;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public List<Long> getProcessInstanceIds() {
        return processInstanceIds;
    }

    public void setProcessInstanceIds(List<Long> processInstanceIds) {
        if (processInstanceIds != null) {
            this.processInstanceIds = new ArrayList<>(processInstanceIds);
        } else {
            this.processInstanceIds = null;
        }
    }

    public String getKieserverId() {
        return kieserverId;
    }

    public void setKieserverId(String kieserverId) {
        this.kieserverId = kieserverId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

}

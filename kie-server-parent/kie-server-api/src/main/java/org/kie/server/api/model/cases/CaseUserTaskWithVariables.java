/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-task-with-vars")
public class CaseUserTaskWithVariables {

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "actual-owner")
    private String actualOwner;

    @XmlElement(name = "correlation-key")
    private String correlationKey;

    @XmlElement(name = "potential-owners")
    private List<String> potentialOwners;

    @XmlElement(name = "case-definition-id")
    private String caseDefinitionId;

    @XmlElement(name = "process-instance-id")
    private Long processInstanceId;

    @XmlElement(name = "case-id")
    private String caseId;

    @XmlElement(name = "task-instance-input-variables")
    private Map<String, Object> inputVariables;

    @XmlElement(name = "process-instance-variables")
    private Map<String, Object> processVariables;

    @XmlElement(name = "case-instance-variables")
    private Map<String, Object> caseVariables;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public List<String> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(List<String> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }


    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public void setCaseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setCaseVariables(Map<String, Object> caseVariables) {
        this.caseVariables = caseVariables;
    }

    public Map<String, Object> getCaseVariables() {
        return caseVariables;
    }

    public Map<String, Object> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(Map<String, Object> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String toString() {
        return "CaseUserTaskWithVariables [id=" + id + ", name=" + name + ", actualOwner=" + actualOwner + ", correlationKey=" + correlationKey + ", potentialOwners=" + potentialOwners + ", caseDefinitionId=" +
               caseDefinitionId + ", processInstanceId=" + processInstanceId + ", caseId=" + caseId + ", inputVariables=" + inputVariables + ", processVariables=" + processVariables + ", caseVariables=" + caseVariables +
               "]";
    }

}

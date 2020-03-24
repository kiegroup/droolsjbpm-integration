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

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-instance-vars")
public class CaseInstanceCustomVars {

    @XmlElement(name = "process-instance-id")
    private Long processInstanceId;

    @XmlElement(name = "case-id")
    private String caseId;

    @XmlElement(name = "case-definition-id")
    private String caseDefinitionId;

    @XmlElement(name = "process-name")
    private String processName;

    @XmlElement(name = "process-version")
    private String processVersion;

    @XmlElement(name = "process-instance-state")
    private Integer state;

    @XmlElement(name = "container-id")
    private String containerId;

    @XmlElement(name = "initiator")
    private String initiator;

    @XmlElement(name = "correlation-key")
    private String correlationKey;

    @XmlElement(name = "process-instance-variables")
    private Map<String, Object> processVariables;

    @XmlElement(name = "case-instance-variables")
    private Map<String, Object> caseVariables;


    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long id) {
        this.processInstanceId = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public void setCaseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
    }
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    public Map<String, Object> getCaseVariables() {
        return caseVariables;
    }

    public void setCaseVariables(Map<String, Object> caseVariables) {
        this.caseVariables = caseVariables;
    }

    @Override
    public String toString() {
        return "CaseInstanceCustomVars [id=" + processInstanceId + ", caseId=" + caseId + ", caseDefinitionId=" + caseDefinitionId + ", processName=" + processName + ", processVersion=" + processVersion + ", state=" +
               state +
               ", containerId=" + containerId + ", correlationKey=" + correlationKey + "]";
    }

}


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

package org.kie.server.api.model.definition;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static java.util.Collections.emptyList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "variable-filter-spec")
public class SearchQueryFilterSpec {

    @XmlElement(name = "attributes-query-params")
    private List<QueryParam> attributesQueryParams;

    @XmlElement(name = "task-query-params")
    private List<QueryParam> taskVariablesQueryParams;

    @XmlElement(name = "process-query-params")
    private List<QueryParam> processVariablesQueryParams;

    @XmlElement(name = "case-query-params")
    private List<QueryParam> caseVariablesQueryParams;

    @XmlElement(name = "pot-owners")
    private List<String> owners;

    public List<QueryParam> getAttributesQueryParams() {
        if (attributesQueryParams == null) {
            return emptyList();
        }
        return attributesQueryParams;
    }

    public void setAttributesQueryParams(List<QueryParam> attributesQueryParams) {
        this.attributesQueryParams = attributesQueryParams;
    }

    public List<QueryParam> getTaskVariablesQueryParams() {
        if (taskVariablesQueryParams == null) {
            return emptyList();
        }
        return taskVariablesQueryParams;
    }

    public void setTaskVariablesQueryParams(List<QueryParam> taskVariablesQueryParams) {
        this.taskVariablesQueryParams = taskVariablesQueryParams;
    }

    public List<QueryParam> getProcessVariablesQueryParams() {
        if (processVariablesQueryParams == null) {
            return emptyList();
        }
        return processVariablesQueryParams;
    }

    public void setProcessVariablesQueryParams(List<QueryParam> processVariablesQueryParams) {
        this.processVariablesQueryParams = processVariablesQueryParams;
    }

    public List<QueryParam> getCaseVariablesQueryParams() {
        if (caseVariablesQueryParams == null) {
            return emptyList();
        }
        return caseVariablesQueryParams;
    }

    public void setCaseVariablesQueryParams(List<QueryParam> caseVariablesQueryParams) {
        this.caseVariablesQueryParams = caseVariablesQueryParams;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<String> getOwners() {
        if (owners == null) {
            return emptyList();
        }
        return owners;
    }

    @Override
    public String toString() {
        return "VariableQueryFilterSpec{ " +
               " AttributesQueryParams=" + (attributesQueryParams) +
               " TaskVariablesQueryParams=" + (taskVariablesQueryParams) +
               " ProcessVariablesQueryParams=" + (processVariablesQueryParams) +
               " CaseVariablesQueryParams=" + (caseVariablesQueryParams) +
               '}';
    }


}

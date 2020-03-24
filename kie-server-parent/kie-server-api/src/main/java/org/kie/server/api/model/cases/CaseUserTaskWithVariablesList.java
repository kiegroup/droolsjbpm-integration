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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-user-task-variable-instance-list")
public class CaseUserTaskWithVariablesList implements ItemList<CaseUserTaskWithVariables> {


    @XmlElement(name = "case-user-task-with-vars-instance")
    private CaseUserTaskWithVariables[] userTaskWithVariables;

    public CaseUserTaskWithVariablesList() {
    }

    public CaseUserTaskWithVariablesList(CaseUserTaskWithVariables[] variableInstances) {
        this.userTaskWithVariables = variableInstances;
    }

    public CaseUserTaskWithVariablesList(List<CaseUserTaskWithVariables> variableInstances) {
        this.userTaskWithVariables = variableInstances.toArray(new CaseUserTaskWithVariables[variableInstances.size()]);
    }

    public CaseUserTaskWithVariables[] getCaseUserTaskWithVariables() {
        return userTaskWithVariables;
    }

    public void setCaseUserTaskWithVariables(CaseUserTaskWithVariables[] variableInstances) {
        this.userTaskWithVariables = variableInstances;
    }

    @Override
    public List<CaseUserTaskWithVariables> getItems() {
        if (userTaskWithVariables == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(userTaskWithVariables);
    }

    @Override
    public String toString() {
        return "CaseUserTaskWithVariablesList [userTaskWithVariables=" + Arrays.toString(userTaskWithVariables) + "]";
    }
}

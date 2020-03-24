/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "user-task-variable-instance-list")
public class ProcessInstanceUserTaskWithVariablesList implements ItemList<ProcessInstanceUserTaskWithVariables> {

    @XmlElement(name = "user-task-with-vars-instance")
    private ProcessInstanceUserTaskWithVariables[] userTaskWithVariables;

    public ProcessInstanceUserTaskWithVariablesList() {
    }

    public ProcessInstanceUserTaskWithVariablesList(ProcessInstanceUserTaskWithVariables[] variableInstances) {
        this.userTaskWithVariables = variableInstances;
    }

    public ProcessInstanceUserTaskWithVariablesList(List<ProcessInstanceUserTaskWithVariables> variableInstances) {
        this.userTaskWithVariables = variableInstances.toArray(new ProcessInstanceUserTaskWithVariables[variableInstances.size()]);
    }

    public ProcessInstanceUserTaskWithVariables[] getUserTaskWithVariables() {
        return userTaskWithVariables;
    }

    public void setUserTaskWithVariables(ProcessInstanceUserTaskWithVariables[] variableInstances) {
        this.userTaskWithVariables = variableInstances;
    }

    @Override
    public List<ProcessInstanceUserTaskWithVariables> getItems() {
        if (userTaskWithVariables == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(userTaskWithVariables);
    }

    @Override
    public String toString() {
        return "ProcessInstanceUserTaskWithVariablesList [userTaskWithVariables=" + Arrays.toString(userTaskWithVariables) + "]";
    }

}

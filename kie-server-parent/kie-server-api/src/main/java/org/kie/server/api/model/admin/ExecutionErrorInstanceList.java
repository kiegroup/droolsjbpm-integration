/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.admin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "execution-error-list")
public class ExecutionErrorInstanceList implements ItemList<ExecutionErrorInstance> {

    @XmlElement(name = "error-instance")
    private ExecutionErrorInstance[] errorInstances;

    public ExecutionErrorInstanceList() {
    }

    public ExecutionErrorInstanceList(ExecutionErrorInstance[] errorInstances) {
        this.errorInstances = errorInstances;
    }

    public ExecutionErrorInstanceList(List<ExecutionErrorInstance> errorInstances) {
        this.errorInstances = errorInstances.toArray(new ExecutionErrorInstance[errorInstances.size()]);
    }

    public ExecutionErrorInstance[] getErrorInstances() {
        return errorInstances;
    }

    public void setErrorInstances(ExecutionErrorInstance[] errorInstances) {
        this.errorInstances = errorInstances;
    }

    @Override
    public List<ExecutionErrorInstance> getItems() {
        if (errorInstances == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(errorInstances);
    }
}

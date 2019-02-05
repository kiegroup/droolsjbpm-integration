/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model.definition;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-subprocesses")
public class SubProcessesDefinition {

    @XmlElementWrapper(name = "subprocesses")
    private Collection<String> subProcesses;

    public SubProcessesDefinition() {
        this(new ArrayList<String>());
    }

    public SubProcessesDefinition(Collection<String> subprocesses) {
        this.subProcesses = subprocesses;
    }

    public Collection<String> getSubProcesses() {
        return subProcesses;
    }

    public void setSubProcesses(Collection<String> subProcesses) {
        this.subProcesses = subProcesses;
    }

    @Override
    public String toString() {
        return "SubProcessesDefinition{" +
                "subProcesses=" + subProcesses +
                '}';
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-definition")
public class ProcessDefinition {

    @XmlElement(name="process-id")
    private String id;

    @XmlElement(name="process-name")
    private String name;

    @XmlElement(name="process-version")
    private String version;

    @XmlElement(name="package")
    private String packageName;

    @XmlElement(name="container-id")
    private String containerId;

    @XmlElementWrapper(name="associated-entities")
    private Map<String, String[]> associatedEntities;
    @XmlElementWrapper(name="service-tasks")
    private Map<String, String> serviceTasks;
    @XmlElementWrapper(name="process-variables")
    private Map<String, String> processVariables;
    @XmlElementWrapper(name="process-subprocesses")
    private Collection<String> reusableSubProcesses;

    @XmlElement(name="dynamic")
    private boolean dynamic;

    public ProcessDefinition() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Map<String, String[]> getAssociatedEntities() {
        return associatedEntities;
    }

    public void setAssociatedEntities(Map<String, String[]> associatedEntities) {
        this.associatedEntities = associatedEntities;
    }

    public Map<String, String> getServiceTasks() {
        return serviceTasks;
    }

    public void setServiceTasks(Map<String, String> serviceTasks) {
        this.serviceTasks = serviceTasks;
    }

    public Map<String, String> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, String> processVariables) {
        this.processVariables = processVariables;
    }

    public Collection<String> getReusableSubProcesses() {
        return reusableSubProcesses;
    }

    public void setReusableSubProcesses(Collection<String> reusableSubProcesses) {
        this.reusableSubProcesses = reusableSubProcesses;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Override
    public String toString() {
        return "ProcessDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", containerId='" + containerId + '\'' +
                ", dynamic='" + dynamic + '\'' +
                '}';
    }

    public static class Builder {

        private ProcessDefinition definition = new ProcessDefinition();

        public ProcessDefinition build() {
            return definition;
        }

        public Builder id(String id) {
            definition.setId(id);

            return this;
        }

        public Builder name(String name) {
            definition.setName(name);

            return this;
        }

        public Builder version(String version) {
            definition.setVersion(version);

            return this;
        }

        public Builder packageName(String packageName) {
            definition.setPackageName(packageName);

            return this;
        }

        public Builder containerId(String containerId) {
            definition.setContainerId(containerId);

            return this;
        }

        public Builder entitiesAsCollection(Map<String, Collection<String>> entities) {
            Map<String, String[]> data = new HashMap<String, String[]>();

            for (Map.Entry<String, Collection<String>> entry : entities.entrySet()) {
                data.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
            }

            definition.setAssociatedEntities(data);

            return this;
        }

        public Builder entities(Map<String, String[]> entities) {

            definition.setAssociatedEntities(entities);

            return this;
        }

        public Builder serviceTasks(Map<String, String> serviceTasks) {
            definition.setServiceTasks(serviceTasks);

            return this;
        }

        public Builder variables(Map<String, String> variables) {
            definition.setProcessVariables(variables);

            return this;
        }

        public Builder subprocesses(Collection<String> subprocesses) {
            definition.setReusableSubProcesses(subprocesses);

            return this;
        }

        public Builder dynamic(boolean dynamic) {
            definition.setDynamic(dynamic);

            return this;
        }
    }
}

/*
 * Copyright 2015 JBoss Inc
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

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "user-task-definition")
public class UserTaskDefinition {

    @XmlElement(name="task-name")
    private String name;
    @XmlElement(name="task-priority")
    private Integer priority;
    @XmlElement(name="task-comment")
    private String comment;
    @XmlElement(name="task-created-by")
    private String createdBy;
    @XmlElement(name="task-skippable")
    private boolean skippable;

    @XmlElementWrapper(name="associated-entities")
    private String[] associatedEntities;
    @XmlElementWrapper(name="task-inputs")
    private Map<String, String> taskInputMappings;
    @XmlElementWrapper(name="task-outputs")
    private Map<String, String> taskOutputMappings;

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isSkippable() {
        return skippable;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public String[] getAssociatedEntities() {
        return associatedEntities;
    }

    public void setAssociatedEntities(String[] associatedEntities) {
        this.associatedEntities = associatedEntities;
    }

    public Map<String, String> getTaskInputMappings() {
        return taskInputMappings;
    }

    public void setTaskInputMappings(Map<String, String> taskInputMappings) {
        this.taskInputMappings = taskInputMappings;
    }

    public Map<String, String> getTaskOutputMappings() {
        return taskOutputMappings;
    }

    public void setTaskOutputMappings(Map<String, String> taskOutputMappings) {
        this.taskOutputMappings = taskOutputMappings;
    }

    public static class Builder {

        private UserTaskDefinition definition = new UserTaskDefinition();

        public UserTaskDefinition build() {
            return definition;
        }

        public Builder name(String name) {
            definition.setName(name);

            return this;
        }

        public Builder priority(int priority) {
            definition.setPriority(priority);

            return this;
        }

        public Builder comment(String comment) {
            definition.setComment(comment);

            return this;
        }

        public Builder createdBy(String createdBy) {
            definition.setCreatedBy(createdBy);

            return this;
        }

        public Builder skippable(boolean skippable) {
            definition.setSkippable(skippable);

            return this;
        }

        public Builder taskInputs(Map<String, String> taskInputs) {
            definition.setTaskInputMappings(taskInputs);

            return this;
        }

        public Builder taskOutputs(Map<String, String> taskOutputs) {
            definition.setTaskOutputMappings(taskOutputs);

            return this;
        }

        public Builder entities(String[] entities) {

            definition.setAssociatedEntities(entities);

            return this;
        }

    }
}

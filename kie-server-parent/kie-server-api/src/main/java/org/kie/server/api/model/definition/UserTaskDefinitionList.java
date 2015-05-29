package org.kie.server.api.model.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "user-task-definitions")
public class UserTaskDefinitionList {

    @XmlElement(name="task")
    private UserTaskDefinition[] tasks;

    public UserTaskDefinitionList() {
    }

    public UserTaskDefinitionList(UserTaskDefinition[] tasks) {
        this.tasks = tasks;
    }

    public UserTaskDefinition[] getTasks() {
        return tasks;
    }

    public void setTasks(UserTaskDefinition[] tasks) {
        this.tasks = tasks;
    }
}

package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-event-instance-list")
public class TaskEventInstanceList {

    @XmlElement(name="task-event-instance")
    private TaskEventInstance[] taskEvents;

    public TaskEventInstanceList() {
    }

    public TaskEventInstanceList(TaskEventInstance[] taskEvents) {
        this.taskEvents = taskEvents;
    }

    public TaskEventInstanceList(List<TaskEventInstance> taskEvents) {
        this.taskEvents = taskEvents.toArray(new TaskEventInstance[taskEvents.size()]);
    }

    public TaskEventInstance[] getTaskEvents() {
        return taskEvents;
    }

    public void setTaskEvents(TaskEventInstance[] taskEvents) {
        this.taskEvents = taskEvents;
    }
}

package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-summary-list")
public class TaskSummaryList {

    @XmlElement(name="task-summary")
    private TaskSummary[] tasks;

    public TaskSummaryList() {
    }

    public TaskSummaryList(TaskSummary[] tasks) {
        this.tasks = tasks;
    }

    public TaskSummaryList(List<TaskSummary> tasks) {
        this.tasks = tasks.toArray(new TaskSummary[tasks.size()]);
    }

    public TaskSummary[] getTasks() {
        return tasks;
    }

    public void setTasks(TaskSummary[] tasks) {
        this.tasks = tasks;
    }
}

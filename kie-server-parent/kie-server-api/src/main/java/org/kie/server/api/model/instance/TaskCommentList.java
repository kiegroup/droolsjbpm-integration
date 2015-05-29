package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-comment-list")
public class TaskCommentList {

    @XmlElement(name="task-comment")
    private TaskComment[] taskComments;

    public TaskCommentList() {
    }

    public TaskCommentList(TaskComment[] taskComments) {
        this.taskComments = taskComments;
    }

    public TaskCommentList(List<TaskComment> taskComments) {
        this.taskComments = taskComments.toArray(new TaskComment[taskComments.size()]);
    }

    public TaskComment[] getTasks() {
        return taskComments;
    }

    public void setTasks(TaskComment[] taskComments) {
        this.taskComments = taskComments;
    }
}

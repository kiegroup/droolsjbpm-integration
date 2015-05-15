package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-attachment-list")
public class TaskAttachmentList {

    @XmlElement(name="task-attachment")
    private TaskAttachment[] taskAttachments;

    public TaskAttachmentList() {
    }

    public TaskAttachmentList(TaskAttachment[] taskAttachments) {
        this.taskAttachments = taskAttachments;
    }

    public TaskAttachmentList(List<TaskAttachment> taskAttachments) {
        this.taskAttachments = taskAttachments.toArray(new TaskAttachment[taskAttachments.size()]);
    }

    public TaskAttachment[] getTasks() {
        return taskAttachments;
    }

    public void setTasks(TaskAttachment[] taskAttachments) {
        this.taskAttachments = taskAttachments;
    }
}

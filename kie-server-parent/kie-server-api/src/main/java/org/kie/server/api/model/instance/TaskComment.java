package org.kie.server.api.model.instance;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-comment")
public class TaskComment {

    @XmlElement(name="comment-id")
    private Long id;

    @XmlElement(name="comment")
    private String text;

    @XmlElement(name="comment-added-by")
    private String addedBy;

    @XmlElement(name="comment-added-at")
    private Date addedAt;

    public TaskComment() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    @Override public String toString() {
        return "TaskComment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", addedBy='" + addedBy + '\'' +
                ", addedAt=" + addedAt +
                '}';
    }

    public static class Builder {

        private TaskComment comment = new TaskComment();

        public TaskComment build() {
            return comment;
        }

        public Builder id(Long id) {
            comment.setId(id);
            return this;
        }

        public Builder text(String text) {
            comment.setText(text);
            return this;
        }

        public Builder addedBy(String addedBy) {
            comment.setAddedBy(addedBy);
            return this;
        }

        public Builder addedAt(Date addedAt) {
            comment.setAddedAt(addedAt);
            return this;
        }
    }

}

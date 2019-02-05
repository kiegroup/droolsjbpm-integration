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

package org.kie.server.api.model.instance;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-comment")
public class TaskComment {

    @XmlElement(name = "comment-id")
    private Long id;

    @XmlElement(name = "comment")
    private String text;

    @XmlElement(name = "comment-added-by")
    private String addedBy;

    @XmlElement(name = "comment-added-at")
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

    @Override
    public String toString() {
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
            comment.setAddedAt(addedAt == null ? addedAt : new Date(addedAt.getTime()));
            return this;
        }
    }
}

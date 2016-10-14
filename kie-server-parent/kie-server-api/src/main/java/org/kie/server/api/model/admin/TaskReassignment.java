/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.admin;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-reassignment")
public class TaskReassignment {

    @XmlElement(name="id")
    private Long id;
    @XmlElement(name="name")
    private String name;
    @XmlElement(name="reassign-at")
    private Date date;
    @XmlElement(name="users")
    private List<String> users;
    @XmlElement(name="groups")
    private List<String> groups;
    @XmlElement(name="active")
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TaskReassignment taskReassignment = new TaskReassignment();

        public TaskReassignment build() {
            return taskReassignment;
        }

        public Builder name(String name) {
            taskReassignment.setName(name);
            return this;
        }

        public Builder id(Long id) {
            taskReassignment.setId(id);
            return this;
        }

        public Builder reassignAt(Date date) {
            taskReassignment.setDate(date);
            return this;
        }

        public Builder users(List<String> users) {
            taskReassignment.setUsers(users);
            return this;
        }

        public Builder groups(List<String> groups) {
            taskReassignment.setGroups(groups);
            return this;
        }

        public Builder active(Boolean active) {
            taskReassignment.setActive(active);
            return this;
        }
    }

    @Override
    public String toString() {
        return "TaskReassignment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", users=" + users +
                ", groups=" + groups +
                ", active=" + active +
                '}';
    }
}

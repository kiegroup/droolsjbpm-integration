/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "PlanningTask")
public class PlanningTaskImpl {

    @Id
    @Column(name = "taskId")
    private long taskId;
    private String assignedUser;
    @Column(name = "taskIndex")
    private int index;
    private short published = 0;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private java.util.Date lastModificationDate;

    public PlanningTaskImpl() {
        //hibernate required constructor
    }

    public PlanningTaskImpl(long taskId, String assignedUser, int index, boolean published, Date lastModificationDate) {
        this.taskId = taskId;
        this.assignedUser = assignedUser;
        this.index = index;
        this.lastModificationDate = lastModificationDate;
        setPublished(published);
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isPublished() {
        return published == 1;
    }

    public void setPublished(boolean published) {
        this.published = (short) (published ? 1 : 0);
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }
}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-notification-list")
public class TaskNotificationList implements ItemList<TaskNotification> {

    @XmlElement(name="task-notification")
    private TaskNotification[] taskNotifications;

    public TaskNotificationList() {
    }

    public TaskNotificationList(TaskNotification[] taskNotifications) {
        this.taskNotifications = taskNotifications;
    }

    public TaskNotificationList(List<TaskNotification> taskNotifications) {
        this.taskNotifications = taskNotifications.toArray(new TaskNotification[taskNotifications.size()]);
    }

    public TaskNotification[] getTaskNotifications() {
        return taskNotifications;
    }

    public void setTaskNotifications(TaskNotification[] taskNotifications) {
        this.taskNotifications = taskNotifications;
    }

    @Override
    public List<TaskNotification> getItems() {
        if (taskNotifications == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(taskNotifications);
    }
}

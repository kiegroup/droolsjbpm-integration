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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-instance-list")
public class TaskInstanceList implements ItemList<TaskInstance> {

    @XmlElement(name = "task-instance")
    private TaskInstance[] tasks;

    public TaskInstanceList() {
    }

    public TaskInstanceList(TaskInstance[] tasks) {
        this.tasks = tasks;
    }

    public TaskInstanceList(List<TaskInstance> tasks) {
        this.tasks = tasks.toArray(new TaskInstance[tasks.size()]);
    }

    public TaskInstance[] getTasks() {
        return tasks;
    }

    public void setTasks(TaskInstance[] tasks) {
        this.tasks = tasks;
    }

    @Override
    public List<TaskInstance> getItems() {
        if (tasks == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(tasks);
    }
}

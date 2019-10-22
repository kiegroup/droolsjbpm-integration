/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime.query;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.query.QueryResultMapper;
import org.kie.server.api.model.taskassigning.OrganizationalEntity;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;

public class TaskAssigningTaskDataQueryMapper extends AbstractTaskAssigningQueryMapper<TaskData> {

    public static final String NAME = "TaskAssigningTaskDataQueryMapper";

    /**
     * Dedicated for ServiceLoader to create instance, use <code>get()</code> method instead
     */
    public TaskAssigningTaskDataQueryMapper() {
    }

    /**
     * Default access to get instance of the mapper
     */
    public static TaskAssigningTaskDataQueryMapper get() {
        return new TaskAssigningTaskDataQueryMapper();
    }

    @Override
    protected TaskData createInstance() {
        return TaskData.builder().potentialOwners(new HashSet<>()).build();
    }

    @Override
    protected boolean readPotentialOwners() {
        return true;
    }

    @Override
    protected void setInstanceValues(TaskData row, Long taskId, Date createdOn, Long processInstanceId, String processId,
                                     String deploymentId, String status, Integer priority, String taskName,
                                     Date lastModificationDate, String actualOwner,
                                     String assignedUser, Integer taskIndex, Integer published) {
        row.setTaskId(taskId);
        row.setCreatedOn(toLocalDateTime(createdOn));
        row.setProcessInstanceId(processInstanceId);
        row.setProcessId(processId);
        row.setContainerId(deploymentId);
        row.setStatus(status);
        row.setPriority(priority);
        row.setName(taskName);
        row.setLastModificationDate(toLocalDateTime(lastModificationDate));
        row.setActualOwner(actualOwner);
        if (assignedUser != null || taskIndex != null) {
            row.setPlanningTask(PlanningTask.builder()
                                        .taskId(taskId)
                                        .assignedUser(assignedUser)
                                        .index(taskIndex)
                                        .published(published != null && published == 1)
                                        .build());
        }
    }

    @Override
    protected void addPotentialOwner(TaskData taskRow, String potentialOwnerId, String potentialOwnerType) {
        taskRow.getPotentialOwners().add(OrganizationalEntity.builder()
                                                 .name(potentialOwnerId)
                                                 .type(potentialOwnerType)
                                                 .build());
    }

    @Override
    public Class<?> getType() {
        return TaskData.class;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public QueryResultMapper<List<TaskData>> forColumnMapping(Map<String, String> map) {
        return get();
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.query.QueryResultMapper;

public class TaskAssigningTaskDataRawQueryMapper extends AbstractTaskAssigningQueryMapper<List<Object>> {

    public static final String NAME = "TaskAssigningTaskDataRawQueryMapper";

    public TaskAssigningTaskDataRawQueryMapper() {
        // Dedicated for ServiceLoader to create instance, use <code>get()</code> method instead
    }

    /**
     * Default access to get instance of the mapper
     * @return
     */

    public static TaskAssigningTaskDataRawQueryMapper get() {
        return new TaskAssigningTaskDataRawQueryMapper();
    }

    @Override
    protected List<Object> createInstance() {
        return new ArrayList<>();
    }

    @Override
    protected boolean readPotentialOwners() {
        return true;
    }

    @Override
    protected void setInstanceValues(List<Object> row, Long taskId, Date createdOn, Long processInstanceId,
                                     String processId, String deploymentId, String status, Integer priority,
                                     String taskName, Date lastModificationDate, String actualOwner,
                                     String assignedUser, Integer taskIndex, Integer published) {
        row.add(taskId);
        row.add(createdOn);
        row.add(actualOwner);
        row.add(deploymentId);
        row.add(taskName);
        row.add(priority);
        row.add(processId);
        row.add(processInstanceId);
        row.add(status);
        row.add(lastModificationDate);

        List<Object> planningEntityRow = new ArrayList<>();
        row.add(planningEntityRow);
        if (assignedUser != null || taskIndex != null) {
            planningEntityRow.add(taskId);
            planningEntityRow.add(assignedUser);
            planningEntityRow.add(taskIndex);
            planningEntityRow.add(published != null && published == 1);
        }
        if (readPotentialOwners()) {
            //make room for the potential owners.
            row.add(new ArrayList<>());
        }
    }

    @Override
    protected void addPotentialOwner(List<Object> taskRow, String potentialOwnerId, String potentialOwnerType) {
        List<List<Object>> potentialOwners = (List<List<Object>>) taskRow.get(taskRow.size() - 1);
        potentialOwners.add(Arrays.asList(potentialOwnerId, potentialOwnerType));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<?> getType() {
        return List.class;
    }

    @Override
    public QueryResultMapper<List<List<Object>>> forColumnMapping(Map<String, String> map) {
        return get();
    }
}

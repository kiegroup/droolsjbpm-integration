/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.runtime.command;

import java.util.Arrays;

import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItem;

import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;

/**
 * Helper class intended to be used by the TaskAssigningRuntimeServiceBase and planning execution.
 */
public class DelegateAndSaveCommand extends PlanningCommand {

    static final String TASK_MODIFIED_ERROR_MSG = "Task: %s was modified by an external action since the last executed plan," +
            " current status is %s but the expected should be in %s";

    public DelegateAndSaveCommand(PlanningItem planningItem, String userId) {
        super(planningItem);
        this.userId = userId;
    }

    @Override
    public Void execute(Context context) {
        super.execute(context);
        final Task task = taskContext.getPersistenceContext().findTask(planningItem.getTaskId());
        final org.kie.api.task.model.TaskData taskData = task.getTaskData();
        final Status status = taskData.getStatus();
        if (!(Ready == status || Reserved == status)) {
            throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG,
                                                      planningItem.getTaskId(),
                                                      status,
                                                      Arrays.toString(new Status[]{Ready, Reserved})),
                                        planningItem.getContainerId(),
                                        PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
        }

        // the by default jBPM task delegation adds the delegated user as a potential owner of the task, but this is
        // something we don't from the task assigning perspective. So by now we ensure that the tasks assigning
        // api doesn't add it.
        // If provided, the bulk delegation should skipp this automatic adding. (https://issues.redhat.com/browse/JBPM-8924)
        final OrganizationalEntity existingPotentialOwner = findPotentialOwner(task, planningItem.getPlanningTask().getAssignedUser());

        // perform the delegation
        DelegateTaskCommand delegateTaskCommand = createDelegateCommand(planningItem.getTaskId(), getUserId(), planningItem.getPlanningTask().getAssignedUser());
        delegateTaskCommand.execute(context);

        if (existingPotentialOwner == null) {
            // we remove it.
            OrganizationalEntity addedPotentialOwner = findPotentialOwner(task, planningItem.getPlanningTask().getAssignedUser());
            if (addedPotentialOwner != null) {
                task.getPeopleAssignments().getPotentialOwners().remove(addedPotentialOwner);
            }
        }

        saveOrUpdatePlanningTask(planningItem);
        return null;
    }

    DelegateTaskCommand createDelegateCommand(long taskId, String userId, String targetEntityId) {
        return new DelegateTaskCommand(taskId, userId, targetEntityId);
    }

    private static OrganizationalEntity findPotentialOwner(Task task, String potentialOwnerId) {
        if (task.getPeopleAssignments() != null && task.getPeopleAssignments().getPotentialOwners() != null) {
            return task.getPeopleAssignments().getPotentialOwners().stream()
                    .filter(organizationalEntity -> organizationalEntity.getId().equals(potentialOwnerId) && organizationalEntity instanceof User)
                    .findFirst().orElse(null);
        }
        return null;
    }
}
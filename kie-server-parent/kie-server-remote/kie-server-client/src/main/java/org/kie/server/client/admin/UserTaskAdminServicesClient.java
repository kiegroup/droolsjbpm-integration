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

package org.kie.server.client.admin;

import java.util.List;
import java.util.Map;

import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.TaskNotification;
import org.kie.server.api.model.admin.TaskReassignment;

public interface UserTaskAdminServicesClient {

    void addPotentialOwners(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void addExcludedOwners(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void addBusinessAdmins(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void removePotentialOwnerUsers(String containerId, Long taskId, String ...users);

    void removeExcludedOwnerUsers(String containerId, Long taskId, String ...users);

    void removeBusinessAdminUsers(String containerId, Long taskId, String ...users);

    void addPotentialOwners(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void addExcludedOwners(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void addBusinessAdmins(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities);

    void removePotentialOwnerUsers(String userId, String containerId, Long taskId, String ...users);

    void removeExcludedOwnerUsers(String userId, String containerId, Long taskId, String ...users);

    void removeBusinessAdminUsers(String userId, String containerId, Long taskId, String ...users);
    

    void removePotentialOwnerGroups(String containerId, Long taskId, String ...groups);

    void removeExcludedOwnerGroups(String containerId, Long taskId, String ...groups);

    void removeBusinessAdminGroups(String containerId, Long taskId, String ...groups);

    void addTaskInputs(String containerId, Long taskId, Map<String, Object> data);

    void removeTaskInputs(String containerId, Long taskId, String ...inputs);

    void removeTaskOutputs(String containerId, Long taskId, String ...outputs);

    Long reassignWhenNotStarted(String containerId, Long taskId, String expiresAt, OrgEntities orgEntities);

    Long reassignWhenNotCompleted(String containerId, Long taskId, String expiresAt, OrgEntities orgEntities);

    Long notifyWhenNotStarted(String containerId, Long taskId, String expiresAt, EmailNotification emailNotification);

    Long notifyWhenNotCompleted(String containerId, Long taskId, String expiresAt, EmailNotification emailNotification);

    void cancelNotification(String containerId, Long taskId, Long notificationId);

    void cancelReassignment(String containerId, Long taskId, Long reassignmentId);

    List<TaskNotification> getTaskNotifications(String containerId, Long taskId, boolean activeOnly);

    List<TaskReassignment> getTaskReassignments(String containerId, Long taskId, boolean activeOnly);

    List<ExecutionErrorInstance> getTaskErrors(String containerId, boolean includeAcknowledged, Integer page, Integer pageSize);

    List<ExecutionErrorInstance> getErrorsByTaskId(String containerId, Long taskId, boolean includeAcknowledged, Integer page, Integer pageSize);

    List<ExecutionErrorInstance> getErrorsByTaskInfo(String containerId, Long processId, String taskName, boolean includeAcknowledged, Integer page, Integer pageSize);
    
    void acknowledgeError(String containerId, String... errorId);

    ExecutionErrorInstance getError(String containerId, String errorId);

    void removePotentialOwnerGroups(String userId, String containerId, Long taskId, String... groups);

    void removeExcludedOwnerGroups(String userId, String containerId, Long taskId, String... groups);

    void removeBusinessAdminGroups(String userId, String containerId, Long taskId, String... groups);

}

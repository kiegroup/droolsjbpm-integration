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

package org.kie.server.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.jms.ResponseHandler;

public interface UserTaskServicesClient {

    // task operations
    void activateTask(String containerId, Long taskId, String userId);

    void claimTask(String containerId, Long taskId, String userId);

    void completeTask(String containerId, Long taskId, String userId, Map<String, Object> params);

    void completeAutoProgress(String containerId, Long taskId, String userId, Map<String, Object> params);

    void delegateTask(String containerId, Long taskId, String userId, String targetUserId);

    void exitTask(String containerId, Long taskId, String userId);

    void failTask(String containerId, Long taskId, String userId, Map<String, Object> params);

    void forwardTask(String containerId, Long taskId, String userId, String targetEntityId);

    void releaseTask(String containerId, Long taskId, String userId);

    void resumeTask(String containerId, Long taskId, String userId);

    void skipTask(String containerId, Long taskId, String userId);

    void startTask(String containerId, Long taskId, String userId);

    void stopTask(String containerId, Long taskId, String userId);

    void suspendTask(String containerId, Long taskId, String userId);

    void nominateTask(String containerId, Long taskId, String userId, List<String> potentialOwners);

    void setTaskPriority(String containerId, Long taskId, int priority);

    void setTaskExpirationDate(String containerId, Long taskId, Date date);

    void setTaskSkipable(String containerId, Long taskId, boolean skipable);

    void setTaskName(String containerId, Long taskId, String name);

    void setTaskDescription(String containerId, Long taskId, String description);

    Long saveTaskContent(String containerId, Long taskId, Map<String, Object> values);

    Map<String, Object> getTaskOutputContentByTaskId(String containerId, Long taskId);

    Map<String, Object> getTaskInputContentByTaskId(String containerId, Long taskId);

    void deleteTaskContent(String containerId, Long taskId, Long contentId);

    Long addTaskComment(String containerId, Long taskId, String text, String addedBy, Date addedOn);

    void deleteTaskComment(String containerId, Long taskId, Long commentId);

    List<TaskComment> getTaskCommentsByTaskId(String containerId, Long taskId);

    TaskComment getTaskCommentById(String containerId, Long taskId, Long commentId);

    Long addTaskAttachment(String containerId, Long taskId, String userId, String name, Object attachment);

    void deleteTaskAttachment(String containerId, Long taskId, Long attachmentId);

    TaskAttachment getTaskAttachmentById(String containerId, Long taskId, Long attachmentId);

    Object getTaskAttachmentContentById(String containerId, Long taskId, Long attachmentId);

    List<TaskAttachment> getTaskAttachmentsByTaskId(String containerId, Long taskId);

    TaskInstance getTaskInstance(String containerId, Long taskId);

    TaskInstance getTaskInstance(String containerId, Long taskId, boolean withInputs, boolean withOutputs, boolean withAssignments);

    List<TaskEventInstance> findTaskEvents(String containerId, Long taskId, Integer page, Integer pageSize);

    List<TaskEventInstance> findTaskEvents(String containerId, Long taskId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    // task searches
    TaskInstance findTaskByWorkItemId(Long workItemId);

    TaskInstance findTaskById(Long taskId);

    List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, String filter, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize);

    List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize);

    List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize);

    List<TaskSummary> findTasksByVariable(String userId, String variableName, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksByVariableAndValue(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, String filter, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksByVariable(String userId, String variableName, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findTasksByVariableAndValue(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    void setResponseHandler(ResponseHandler responseHandler);
}

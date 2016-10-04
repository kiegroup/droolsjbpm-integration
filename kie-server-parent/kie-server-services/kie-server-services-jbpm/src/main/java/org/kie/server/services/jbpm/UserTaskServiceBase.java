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

package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.locator.ByTaskIdContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserTaskServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(UserTaskServiceBase.class);

    private IdentityProvider identityProvider;
    private UserTaskService userTaskService;

    private MarshallerHelper marshallerHelper;

    private boolean bypassAuthUser = false;

    public UserTaskServiceBase(UserTaskService userTaskService, KieServerRegistry context) {
        this.userTaskService = userTaskService;
        this.identityProvider = context.getIdentityProvider();
        this.marshallerHelper = new MarshallerHelper(context);

        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    protected String getUser(String queryParamUser) {
        if (bypassAuthUser) {
            return queryParamUser;
        }

        return identityProvider.getName();
    }

    public void activate(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to activate task with id '{}' as user '{}'", taskId, userId);
        userTaskService.activate(taskId.longValue(), userId);
    }

    public void claim(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to claim task with id '{}' as user '{}'", taskId, userId);
        userTaskService.claim(taskId.longValue(), userId);

    }

    public void complete(String containerId, Number taskId, String userId, String payload, String marshallerType) {

        userId = getUser(userId);
        
        logger.debug("About to unmarshal task outcome parameters from payload: '{}'", payload, new ByTaskIdContainerLocator(taskId.longValue()));
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallerType, Map.class);

        logger.debug("About to complete task with id '{}' as user '{}' with data {}", taskId, userId, parameters);
        userTaskService.complete(taskId.longValue(), userId, parameters);

    }

    public void completeAutoProgress(String containerId, Number taskId, String userId, String payload, String marshallerType) {

        userId = getUser(userId);
        
        logger.debug("About to unmarshal task outcome parameters from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallerType, Map.class, new ByTaskIdContainerLocator(taskId.longValue()));

        logger.debug("About to complete task with id '{}' as user '{}' with data {}", taskId, userId, parameters);
        userTaskService.completeAutoProgress(taskId.longValue(), userId, parameters);

    }

    public void delegate(String containerId, Number taskId, String userId, String targetUserId) {

        userId = getUser(userId);
        logger.debug("About to delegate task with id '{}' as user '{}' to user '{}'", taskId, userId, targetUserId);
        userTaskService.delegate(taskId.longValue(), userId, targetUserId);

    }

    public void exit(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to exit task with id '{}' as user '{}'", taskId, userId);
        userTaskService.exit(taskId.longValue(), userId);

    }

    public void fail(String containerId, Number taskId, String userId, String payload, String marshallerType) {

        userId = getUser(userId);
        logger.debug("About to unmarshal task failure data from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallerType, Map.class);

        logger.debug("About to fail task with id '{}' as user '{}' with data {}", taskId, userId, parameters);
        userTaskService.fail(taskId.longValue(), userId, parameters);

    }

    public void forward(String containerId, Number taskId, String userId, String targetUserId) {

        userId = getUser(userId);
        logger.debug("About to forward task with id '{}' as user '{}' to user '{}'", taskId, userId, targetUserId);
        userTaskService.forward(taskId.longValue(), userId, targetUserId);
    }

    public void release(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to release task with id '{}' as user '{}'", taskId, userId);
        userTaskService.release(taskId.longValue(), userId);
    }

    public void resume(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to resume task with id '{}' as user '{}'", taskId, userId);
        userTaskService.resume(taskId.longValue(), userId);
    }

    public void skip(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to skip task with id '{}' as user '{}'", taskId, userId);
        userTaskService.skip(taskId.longValue(), userId);
    }


    public void start(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to start task with id '{}' as user '{}'", taskId, userId);
        userTaskService.start(taskId.longValue(), userId);
    }


    public void stop(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to stop task with id '{}' as user '{}'", taskId, userId);
        userTaskService.stop(taskId.longValue(), userId);

    }

    public void suspend(String containerId, Number taskId, String userId) {

        userId = getUser(userId);
        logger.debug("About to suspend task with id '{}' as user '{}'", taskId, userId);
        userTaskService.suspend(taskId.longValue(), userId);
    }

    public void nominate(String containerId, Number taskId, String userId, List<String> potentialOwners) {

        userId = getUser(userId);
        logger.debug("About to nominate task with id '{}' as user '{}' to potential owners", taskId, userId, potentialOwners);
        List<OrganizationalEntity> potOwnerEntities = new ArrayList<OrganizationalEntity>();
        for (String potOwnerId : potentialOwners) {
            potOwnerEntities.add(TaskModelProvider.getFactory().newUser(potOwnerId));
        }
        userTaskService.nominate(taskId.longValue(), userId, potOwnerEntities);
    }

    public void setPriority(String containerId, Number taskId, String priorityPayload, String marshallingType) {

        logger.debug("About to unmarshal task priority from payload: '{}'", priorityPayload);
        Integer priority = marshallerHelper.unmarshal(containerId, priorityPayload, marshallingType, Integer.class);

        logger.debug("About to set priority for a task with id '{}' with value '{}'", taskId, priority);
        userTaskService.setPriority(taskId.longValue(), priority);

    }

    public void setExpirationDate(String containerId, Number taskId, String datePayload, String marshallingType) {

        logger.debug("About to unmarshal task priority from payload: '{}'", datePayload);
        Date expirationDate = marshallerHelper.unmarshal(containerId, datePayload, marshallingType, Date.class);

        logger.debug("About to set expiration date for a task with id '{}' with value '{}'", taskId, expirationDate);
        userTaskService.setExpirationDate(taskId.longValue(), expirationDate);
    }

    public void setSkipable(String containerId, Number taskId, String skipablePayload, String marshallingType) {

        logger.debug("About to unmarshal task skipable from payload: '{}'", skipablePayload);
        Boolean skipable = marshallerHelper.unmarshal(containerId, skipablePayload, marshallingType, Boolean.class);

        logger.debug("About to set skipable attribute for a task with id '{}' with value '{}'", taskId, skipable);
        userTaskService.setSkipable(taskId.longValue(), skipable);

    }

    public void setName(String containerId, Number taskId, String namePayload, String marshallingType) {

        logger.debug("About to unmarshal task name from payload: '{}'", namePayload);
        String name = marshallerHelper.unmarshal(containerId, namePayload, marshallingType, String.class);

        logger.debug("About to set name for a task with id '{}' with value '{}'", taskId, name);
        userTaskService.setName(taskId.longValue(), name);

    }

    public void setDescription(String containerId, Number taskId, String descriptionPayload, String marshallingType) {

        logger.debug("About to unmarshal task description from payload: '{}'", descriptionPayload);
        String description = marshallerHelper.unmarshal(containerId, descriptionPayload, marshallingType, String.class);

        logger.debug("About to set name for a task with id '{}' with value '{}'", taskId, description);
        userTaskService.setDescription(taskId.longValue(), description);

    }

    public String saveContent(String containerId, Number taskId, String payload, String marshallingType) {
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        logger.debug("About to unmarshal task content parameters from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, locator);

        logger.debug("About to set content of a task with id '{}' with data {}", taskId, parameters);
        Long contentId = userTaskService.saveContent(taskId.longValue(), parameters);

        String response = marshallerHelper.marshal(containerId, marshallingType, contentId, locator);

        return response;
    }

    public String getTaskOutputContentByTaskId(String containerId, Number taskId, String marshallingType) {

        Map<String, Object> variables = userTaskService.getTaskOutputContentByTaskId(taskId.longValue());

        logger.debug("About to marshal task '{}' output variables {}", taskId, variables);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, variables, locator);

        return response;
    }

    public String getTaskInputContentByTaskId(String containerId, Number taskId, String marshallingType) {

        Map<String, Object> variables = userTaskService.getTaskInputContentByTaskId(taskId.longValue());

        logger.debug("About to marshal task '{}' input variables {}", taskId, variables);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, variables, locator);

        return response;

    }

    public void deleteContent(String containerId, Number taskId, Number contentId) {

        userTaskService.deleteContent(taskId.longValue(), contentId.longValue());
    }

    public String addComment(String containerId, Number taskId, String payload, String marshallingType) {
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        logger.debug("About to unmarshal task comment from payload: '{}'", payload);
        TaskComment comment = marshallerHelper.unmarshal(containerId, payload, marshallingType, TaskComment.class, locator);

        logger.debug("About to set comment on a task with id '{}' with data {}", taskId, comment);
        Long commentId = userTaskService.addComment(taskId.longValue(), comment.getText(), comment.getAddedBy(), comment.getAddedAt());

        String response = marshallerHelper.marshal(containerId, marshallingType, commentId, locator);

        return response;
    }

    public void deleteComment(String containerId, Number taskId, Number commentId) {

        userTaskService.deleteComment(taskId.longValue(), commentId.longValue());
    }

    public String getCommentsByTaskId(String containerId, Number taskId, String marshallingType) {

        List<Comment> comments = userTaskService.getCommentsByTaskId(taskId.longValue());

        TaskComment[] taskComments = new TaskComment[comments.size()];
        int counter = 0;
        for (Comment comment : comments) {

            TaskComment taskComment = TaskComment.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .addedBy(comment.getAddedBy().getId())
                    .addedAt(comment.getAddedAt())
                    .build();

            taskComments[counter] = taskComment;
            counter++;
        }
        TaskCommentList result = new TaskCommentList(taskComments);

        logger.debug("About to marshal task '{}' comments {}", taskId, result);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, result, locator);

        return response;
    }

    public String getCommentById(String containerId, Number taskId, Number commentId, String marshallingType) {

        Comment comment = userTaskService.getCommentById(taskId.longValue(), commentId.longValue());

        if (comment == null) {
            throw new IllegalStateException("No comment found with id " + commentId + " on task " + taskId);
        }

        TaskComment taskComment = TaskComment.builder()
                .id(comment.getId())
                .text(comment.getText())
                .addedBy(comment.getAddedBy().getId())
                .addedAt(comment.getAddedAt())
                .build();

        logger.debug("About to marshal task '{}' comment {}", taskId, taskComment);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, taskComment, locator);

        return response;
    }

    public String addAttachment(String containerId, Number taskId, String userId, String name, String attachmentPayload, String marshallingType) {
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        logger.debug("About to unmarshal task attachment from payload: '{}'", attachmentPayload);
        Object attachment = marshallerHelper.unmarshal(containerId, attachmentPayload, marshallingType, Object.class, locator);

        logger.debug("About to add attachment on a task with id '{}' with data {}", taskId, attachment);
        Long attachmentId = userTaskService.addAttachment(taskId.longValue(), getUser(userId), name, attachment);

        String response = marshallerHelper.marshal(containerId, marshallingType, attachmentId, locator);

        return response;
    }

    public void deleteAttachment(String containerId, Number taskId, Number attachmentId) {

        userTaskService.deleteAttachment(taskId.longValue(), attachmentId.longValue());
    }

    public String getAttachmentById(String containerId, Number taskId, Number attachmentId, String marshallingType) {

        Attachment attachment = userTaskService.getAttachmentById(taskId.longValue(), attachmentId.longValue());

        TaskAttachment taskAttachment = TaskAttachment.builder()
                .id(attachment.getId())
                .name(attachment.getName())
                .addedBy(attachment.getAttachedBy().getId())
                .addedAt(attachment.getAttachedAt())
                .attachmentContentId(attachment.getAttachmentContentId())
                .contentType(attachment.getContentType())
                .size(attachment.getSize())
                .build();

        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        logger.debug("About to marshal task '{}' attachment {} with content {}", taskId, attachmentId, taskAttachment);
        String response = marshallerHelper.marshal(containerId, marshallingType, taskAttachment, locator);

        return response;

    }

    public String getAttachmentContentById(String containerId, Number taskId, Number attachmentId, String marshallingType) {

        Object attachment = userTaskService.getAttachmentContentById(taskId.longValue(), attachmentId.longValue());

        if (attachment == null) {
            throw new IllegalStateException("No attachment found for id " + attachmentId + " for task " + taskId);
        }

        logger.debug("About to marshal task attachment with id '{}' {}", attachmentId, attachment);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, attachment, locator);

        return response;
    }


    public String getAttachmentsByTaskId(String containerId, Number taskId, String marshallingType) {

        List<Attachment> attachments = userTaskService.getAttachmentsByTaskId(taskId.longValue());

        TaskAttachment[] taskComments = new TaskAttachment[attachments.size()];
        int counter = 0;
        for (Attachment attachment : attachments) {

            TaskAttachment taskComment = TaskAttachment.builder()
                    .id(attachment.getId())
                    .name(attachment.getName())
                    .addedBy(attachment.getAttachedBy().getId())
                    .addedAt(attachment.getAttachedAt())
                    .contentType(attachment.getContentType())
                    .attachmentContentId(attachment.getAttachmentContentId())
                    .size(attachment.getSize())
                    .build();

            taskComments[counter] = taskComment;
            counter++;
        }
        TaskAttachmentList result = new TaskAttachmentList(taskComments);

        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        logger.debug("About to marshal task '{}' attachments {}", taskId, result);
        String response = marshallerHelper.marshal(containerId, marshallingType, result, locator);

        return response;
    }

    public String  getTask(String containerId, Number taskId, boolean withInput, boolean withOutput, boolean withAssignments, String marshallingType) {

        Task task = userTaskService.getTask(taskId.longValue());
        if (task == null) {
            throw new TaskNotFoundException("No task found with id " + taskId);
        }
        TaskInstance.Builder builder = TaskInstance.builder();
        builder
                .id(task.getId())
                .name(task.getName())
                .subject(task.getSubject())
                .description(task.getDescription())
                .priority(task.getPriority())
                .taskType(task.getTaskType())
                .formName(((InternalTask) task).getFormName())
                .status(task.getTaskData().getStatus().name())
                .actualOwner(getOrgEntityIfNotNull(task.getTaskData().getActualOwner()))
                .createdBy(getOrgEntityIfNotNull(task.getTaskData().getCreatedBy()))
                .createdOn(task.getTaskData().getCreatedOn())
                .activationTime(task.getTaskData().getActivationTime())
                .expirationTime(task.getTaskData().getExpirationTime())
                .skippable(task.getTaskData().isSkipable())
                .workItemId(task.getTaskData().getWorkItemId())
                .processInstanceId(task.getTaskData().getProcessInstanceId())
                .parentId(task.getTaskData().getParentId())
                .processId(task.getTaskData().getProcessId())
                .containerId(task.getTaskData().getDeploymentId());

        if (Boolean.TRUE.equals(withInput)) {
            Map<String, Object> variables = userTaskService.getTaskInputContentByTaskId(taskId.longValue());
            builder.inputData(variables);
        }

        if (Boolean.TRUE.equals(withOutput)) {
            Map<String, Object> variables = userTaskService.getTaskOutputContentByTaskId(taskId.longValue());
            builder.outputData(variables);
        }

        if (Boolean.TRUE.equals(withAssignments)) {
            builder.potentialOwners(orgEntityAsList(task.getPeopleAssignments().getPotentialOwners()));

            builder.excludedOwners(orgEntityAsList(((InternalPeopleAssignments) task.getPeopleAssignments()).getExcludedOwners()));

            builder.businessAdmins(orgEntityAsList(task.getPeopleAssignments().getBusinessAdministrators()));
        }

        TaskInstance taskInstance = builder.build();


        logger.debug("About to marshal task '{}' representation {}", taskId, taskInstance);
        ContainerLocator locator = new ByTaskIdContainerLocator(taskId.longValue());
        String response = marshallerHelper.marshal(containerId, marshallingType, taskInstance, locator);

        return response;
    }

    private String getOrgEntityIfNotNull(OrganizationalEntity organizationalEntity) {
        if (organizationalEntity == null) {
            return "";
        }

        return organizationalEntity.getId();
    }

    private List<String> orgEntityAsList(List<OrganizationalEntity> organizationalEntities) {
        ArrayList<String> entities = new ArrayList<String>();
        if (organizationalEntities == null) {
            return entities;
        }

        for (OrganizationalEntity entity : organizationalEntities) {
            entities.add(entity.getId());
        }

        return entities;
    }
}

/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.client.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.UserTaskServicesClient;

import static org.kie.server.api.rest.RestURI.*;

public class UserTaskServicesClientImpl extends AbstractKieServicesClientImpl implements UserTaskServicesClient {

    public UserTaskServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public UserTaskServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public void activateTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_ACTIVATE_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void claimTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_CLAIM_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void completeTask(String containerId, Long taskId, String userId, Map<String, Object> params) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMPLETE_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void delegateTask(String containerId, Long taskId, String userId, String targetUserId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_DELEGATE_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetUserId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void exitTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_EXIT_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void failTask(String containerId, Long taskId, String userId, Map<String, Object> params) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_FAIL_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void forwardTask(String containerId, Long taskId, String userId, String targetEntityId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_FORWARD_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetEntityId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void releaseTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_RELEASE_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void resumeTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_RESUME_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void skipTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_SKIP_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void startTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_START_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void stopTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_STOP_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void suspendTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_SUSPEND_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void nominateTask(String containerId, Long taskId, String userId, List<String> potentialOwners) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_NOMINATE_PUT_URI, getUserAndAdditionalParams(userId, "potOwner", potentialOwners));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskPriority(String containerId, Long taskId, int priority) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_PRIORITY_PUT_URI, valuesMap),
                    priority, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskExpirationDate(String containerId, Long taskId, Date date) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_EXPIRATION_DATE_PUT_URI, valuesMap),
                    date, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskSkipable(String containerId, Long taskId, boolean skipable) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_SKIPABLE_PUT_URI, valuesMap),
                    skipable, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskName(String containerId, Long taskId, String name) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_NAME_PUT_URI, valuesMap),
                    name, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskDescription(String containerId, Long taskId, String description) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_DESCRIPTION_PUT_URI, valuesMap),
                    description, String.class, getHeaders(description));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long saveTaskContent(String containerId, Long taskId, Map<String, Object> values) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object contentId = makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_OUTPUT_DATA_PUT_URI, valuesMap),
                    values, Object.class, getHeaders(null));

            if (contentId instanceof Wrapped) {
                return (Long) ((Wrapped) contentId).unwrap();
            }

            return ((Number) contentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getTaskOutputContentByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_OUTPUT_DATA_GET_URI, valuesMap), Object.class);

            if (variables instanceof Wrapped) {
                return (Map) ((Wrapped) variables).unwrap();
            }

            return (Map) variables;
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getTaskInputContentByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_INPUT_DATA_GET_URI, valuesMap), Object.class);

            if (variables instanceof Wrapped) {
                return (Map) ((Wrapped) variables).unwrap();
            }

            return (Map) variables;
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskContent(String containerId, Long taskId, Long contentId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(CONTENT_ID, contentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_CONTENT_DATA_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long addTaskComment(String containerId, Long taskId, String text, String addedBy, Date addedOn) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskComment taskComment = TaskComment.builder()
                    .text(text)
                    .addedBy(addedBy)
                    .addedAt(addedOn)
                    .build();

            Object commentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_ADD_POST_URI, valuesMap), taskComment, Object.class, getHeaders(taskComment));

            if (commentId instanceof Wrapped) {
                return (Long) ((Wrapped) commentId).unwrap();
            }

            return ((Number) commentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskComment(String containerId, Long taskId, Long commentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskComment> getTaskCommentsByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskCommentList commentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENTS_GET_URI, valuesMap), TaskCommentList.class);

            if (commentList.getTasks() != null) {
                return Arrays.asList(commentList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskComment getTaskCommentById(String containerId, Long taskId, Long commentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            TaskComment taskComment = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_GET_URI, valuesMap), TaskComment.class);

            return taskComment;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long addTaskAttachment(String containerId, Long taskId, String userId, Object attachment) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object attachmentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_ADD_POST_URI, valuesMap) + getUserQueryStr(userId),
                    attachment, Object.class, getHeaders(null));

            if (attachmentId instanceof Wrapped) {
                return (Long) ((Wrapped) attachmentId).unwrap();
            }

            return ((Number) attachmentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskAttachment(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskAttachment getTaskAttachmentById(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            TaskAttachment attachment = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_GET_URI, valuesMap), TaskAttachment.class);

            return attachment;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Object getTaskAttachmentContentById(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            Object result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI, valuesMap), Object.class);

            if (result instanceof Wrapped) {
                return ((Wrapped) result).unwrap();
            }

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskAttachment> getTaskAttachmentsByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskAttachmentList attachmentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENTS_GET_URI, valuesMap), TaskAttachmentList.class);

            if (attachmentList.getTasks() != null) {
                return Arrays.asList(attachmentList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_GET_URI, valuesMap), TaskInstance.class);


            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId, boolean withInputs, boolean withOutputs, boolean withAssignments) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            StringBuilder queryString = new StringBuilder();
            queryString.append("?withInputData").append("=").append(withInputs)
                    .append("&withOutputData").append("=").append(withOutputs)
                    .append("&withAssignments").append("=").append(withAssignments);

            TaskInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_GET_URI, valuesMap) + queryString.toString(), TaskInstance.class);


            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    // task basic queries

    @Override
    public TaskInstance findTaskByWorkItemId(Long workItemId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(WORK_ITEM_ID, workItemId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_BY_WORK_ITEM_ID_GET_URI, valuesMap), TaskInstance.class);


        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance findTaskById(Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_GET_URI, valuesMap), TaskInstance.class);


        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String groupsQuery = getAdditionalParams(statusQuery, "groups", groups);
            String queryString = getPagingQueryString(groupsQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String statusQuery = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_BY_PROCESS_INST_ID_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = getPagingQueryString("", page, pageSize);

            TaskEventInstanceList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_EVENTS_GET_URI, valuesMap) + queryString , TaskEventInstanceList.class);

            if (taskSummaryList != null && taskSummaryList.getTaskEvents() != null) {
                return Arrays.asList(taskSummaryList.getTaskEvents());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }
}

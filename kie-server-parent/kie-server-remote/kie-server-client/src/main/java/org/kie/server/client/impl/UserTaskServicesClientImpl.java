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

package org.kie.server.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
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

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_ACTIVATE_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "activate", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void claimTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_CLAIM_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "claim", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void completeTask(String containerId, Long taskId, String userId, Map<String, Object> params) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMPLETE_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));
        } else {

            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "complete", serialize(safeMap(params)), marshaller.getFormat().getType(), new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void completeAutoProgress(String containerId, Long taskId, String userId, Map<String, Object> params) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String userQuery = getUserQueryStr(userId);
            StringBuilder queryString = new StringBuilder(userQuery);
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            queryString.append("auto-progress=true");
            
            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMPLETE_PUT_URI, valuesMap) + queryString.toString(),
                    params, String.class, getHeaders(null));
        } else {

            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "completeAutoProgress", serialize(safeMap(params)), marshaller.getFormat().getType(), new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void delegateTask(String containerId, Long taskId, String userId, String targetUserId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_DELEGATE_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetUserId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "delegate", new Object[]{containerId, taskId, userId, targetUserId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void exitTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_EXIT_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "exit", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void failTask(String containerId, Long taskId, String userId, Map<String, Object> params) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_FAIL_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));

        } else {

            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "fail", serialize(safeMap(params)), marshaller.getFormat().getType(), new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void forwardTask(String containerId, Long taskId, String userId, String targetEntityId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_FORWARD_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetEntityId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "forward", new Object[]{containerId, taskId, userId, targetEntityId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void releaseTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_RELEASE_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "release", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<TaskOutputsDefinition> response = (ServiceResponse<TaskOutputsDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void resumeTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_RESUME_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "resume", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void skipTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_SKIP_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "skip", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void startTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_START_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "start", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void stopTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_STOP_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "stop", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void suspendTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_SUSPEND_PUT_URI, getUserQueryStr(userId));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "suspend", new Object[]{containerId, taskId, userId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void nominateTask(String containerId, Long taskId, String userId, List<String> potentialOwners) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_URI + "/" + TASK_INSTANCE_NOMINATE_PUT_URI, getUserAndAdditionalParams(userId, "potOwner", potentialOwners));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "nominate", new Object[]{containerId, taskId, userId, potentialOwners}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setTaskPriority(String containerId, Long taskId, int priority) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_PRIORITY_PUT_URI, valuesMap),
                    priority, String.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "setPriority", serialize(priority), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setTaskExpirationDate(String containerId, Long taskId, Date date) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_EXPIRATION_DATE_PUT_URI, valuesMap),
                    serialize(date), String.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "setExpirationDate", serialize(date), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setTaskSkipable(String containerId, Long taskId, boolean skipable) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_SKIPABLE_PUT_URI, valuesMap),
                    serialize(skipable), String.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "setSkipable", serialize(skipable), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setTaskName(String containerId, Long taskId, String name) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_NAME_PUT_URI, valuesMap),
                    serialize(name), String.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "setName", serialize(name), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setTaskDescription(String containerId, Long taskId, String description) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_DESCRIPTION_PUT_URI, valuesMap),
                    serialize(description), String.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "setDescription", serialize(description), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Long saveTaskContent(String containerId, Long taskId, Map<String, Object> values) {
        Object contentId = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            contentId = makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_OUTPUT_DATA_PUT_URI, valuesMap),
                    values, Object.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "saveContent", serialize(values), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            contentId = deserialize(response.getResult(), Object.class);
        }

        if (contentId instanceof Wrapped) {
            return (Long) ((Wrapped) contentId).unwrap();
        }

        return ((Number) contentId).longValue();
    }

    @Override
    public Map<String, Object> getTaskOutputContentByTaskId(String containerId, Long taskId) {
        Object variables = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_OUTPUT_DATA_GET_URI, valuesMap), Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getTaskOutputContentByTaskId", marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            variables = deserialize(response.getResult(), Object.class);
        }
        if (variables instanceof Wrapped) {
            return (Map) ((Wrapped) variables).unwrap();
        }

        return (Map) variables;
    }

    @Override
    public Map<String, Object> getTaskInputContentByTaskId(String containerId, Long taskId) {
        Object variables = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_INPUT_DATA_GET_URI, valuesMap), Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getTaskInputContentByTaskId", marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            variables = deserialize(response.getResult(), Object.class);
        }
        if (variables instanceof Wrapped) {
            return (Map) ((Wrapped) variables).unwrap();
        }

        return (Map) variables;
    }

    @Override
    public void deleteTaskContent(String containerId, Long taskId, Long contentId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(CONTENT_ID, contentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_CONTENT_DATA_DELETE_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "deleteContent", new Object[]{containerId, taskId, contentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Long addTaskComment(String containerId, Long taskId, String text, String addedBy, Date addedOn) {
        Object commentId = null;
        TaskComment taskComment = TaskComment.builder()
                .text(text)
                .addedBy(addedBy)
                .addedAt(addedOn)
                .build();

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);


            commentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMMENT_ADD_POST_URI, valuesMap), taskComment, Object.class, getHeaders(taskComment));


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "addComment", serialize(taskComment), marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            commentId = deserialize(response.getResult(), Object.class);
        }

        if (commentId instanceof Wrapped) {
            return (Long) ((Wrapped) commentId).unwrap();
        }

        return ((Number) commentId).longValue();
    }

    @Override
    public void deleteTaskComment(String containerId, Long taskId, Long commentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "deleteComment", new Object[]{containerId, taskId, commentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<TaskComment> getTaskCommentsByTaskId(String containerId, Long taskId) {
        TaskCommentList commentList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            commentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMMENTS_GET_URI, valuesMap), TaskCommentList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getCommentsByTaskId", marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            commentList = deserialize(response.getResult(), TaskCommentList.class);
        }

        if (commentList.getTasks() != null) {
            return Arrays.asList(commentList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public TaskComment getTaskCommentById(String containerId, Long taskId, Long commentId) {
        TaskComment taskComment = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            taskComment = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_COMMENT_GET_URI, valuesMap), TaskComment.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getCommentById", marshaller.getFormat().getType(), new Object[]{containerId, taskId, commentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskComment = deserialize(response.getResult(), TaskComment.class);
        }

        return taskComment;
    }

    @Override
    public Long addTaskAttachment(String containerId, Long taskId, String userId, String name, Object attachment) {
        Object attachmentId = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            attachmentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_ATTACHMENT_ADD_POST_URI, valuesMap) + getUserAndAdditionalParam(userId, "name", name),
                    attachment, Object.class, getHeaders(null));

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "addAttachment", serialize(attachment), marshaller.getFormat().getType(), new Object[]{containerId, taskId, userId, name}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            attachmentId = deserialize(response.getResult(), Object.class);
        }
        if (attachmentId instanceof Wrapped) {
            return (Long) ((Wrapped) attachmentId).unwrap();
        }

        return ((Number) attachmentId).longValue();
    }

    @Override
    public void deleteTaskAttachment(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_ATTACHMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "UserTaskService", "deleteAttachment", new Object[]{containerId, taskId, attachmentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public TaskAttachment getTaskAttachmentById(String containerId, Long taskId, Long attachmentId) {
        TaskAttachment attachment = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            attachment = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_ATTACHMENT_GET_URI, valuesMap), TaskAttachment.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getAttachmentById", marshaller.getFormat().getType(), new Object[]{containerId, taskId, attachmentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            attachment = deserialize(response.getResult(), TaskAttachment.class);
        }

        return attachment;
    }

    @Override
    public Object getTaskAttachmentContentById(String containerId, Long taskId, Long attachmentId) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI, valuesMap), Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getAttachmentContentById", marshaller.getFormat().getType(), new Object[]{containerId, taskId, attachmentId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return ((Wrapped) result).unwrap();
        }

        return result;

    }

    @Override
    public List<TaskAttachment> getTaskAttachmentsByTaskId(String containerId, Long taskId) {
        TaskAttachmentList attachmentList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            attachmentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_ATTACHMENTS_GET_URI, valuesMap), TaskAttachmentList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getAttachmentsByTaskId", marshaller.getFormat().getType(), new Object[]{containerId, taskId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            attachmentList = deserialize(response.getResult(), TaskAttachmentList.class);;
        }

        if (attachmentList.getTasks() != null) {
            return Arrays.asList(attachmentList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId) {
        TaskInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_GET_URI, valuesMap), TaskInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getTask", marshaller.getFormat().getType(), new Object[]{containerId, taskId, false, false, false}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), TaskInstance.class);
        }

        return result;
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId, boolean withInputs, boolean withOutputs, boolean withAssignments) {
        TaskInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            StringBuilder queryString = new StringBuilder();
            queryString.append("?withInputData").append("=").append(withInputs)
                    .append("&withOutputData").append("=").append(withOutputs)
                    .append("&withAssignments").append("=").append(withAssignments);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), TASK_URI + "/" + TASK_INSTANCE_GET_URI, valuesMap) + queryString.toString(), TaskInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskService", "getTask", marshaller.getFormat().getType(), new Object[]{containerId, taskId, withInputs, withOutputs, withAssignments}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), TaskInstance.class);
        }

        return result;
    }

    // task basic queries

    @Override
    public TaskInstance findTaskByWorkItemId(Long workItemId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(WORK_ITEM_ID, workItemId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASK_BY_WORK_ITEM_ID_GET_URI, valuesMap), TaskInstance.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "QueryService", "getTaskByWorkItemId", new Object[]{workItemId}) ) );
            ServiceResponse<TaskInstance> response = (ServiceResponse<TaskInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public TaskInstance findTaskById(Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASK_GET_URI, valuesMap), TaskInstance.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "QueryService", "getTaskById", new Object[]{taskId}) ) );
            ServiceResponse<TaskInstance> response = (ServiceResponse<TaskInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize) {
        return findTasksAssignedAsBusinessAdministrator(userId, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize) {
        return findTasksAssignedAsBusinessAdministrator(userId, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize) {
        return findTasksAssignedAsPotentialOwner(userId, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize) {
        return findTasksAssignedAsPotentialOwner(userId, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, String filter, List<String> status, Integer page, Integer pageSize) {
        return findTasksAssignedAsPotentialOwner(userId, filter, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize) {
        return findTasksAssignedAsPotentialOwner(userId, groups, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize) {
        return findTasksOwned(userId, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize) {
        return findTasksOwned(userId, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize) {
        return findTasksByStatusByProcessInstanceId(processInstanceId, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize) {
        return findTasks(userId, page, pageSize, "", true);
    }

    @Override
    public List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize) {
        return findTaskEvents(taskId, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksByVariable(String userId, String variableName, List<String> status, Integer page, Integer pageSize) {
        return findTasksByVariable(userId, variableName, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksByVariableAndValue(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize) {
        return findTasksByVariableAndValue(userId, variableName, variableValue, status, page, pageSize, "", true);
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsBusinessAdministratorByStatus", new Object[]{new ArrayList(), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }


        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsBusinessAdministratorByStatus", new Object[]{safeList(status), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsPotentialOwner", new Object[]{new ArrayList(), new ArrayList(), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsPotentialOwner", new Object[]{safeList(status), new ArrayList(), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }

        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, String filter, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder+"&filter="+filter;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsPotentialOwner", new Object[]{safeList(status), new ArrayList(), userId, page, pageSize, sort, sortOrder, filter}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String groupsQuery = getAdditionalParams(statusQuery, "groups", groups);
            String queryString = getPagingQueryString(groupsQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksAssignedAsPotentialOwner", new Object[]{safeList(status), safeList(groups), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }

        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksOwnedByStatus", new Object[]{new ArrayList(), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksOwnedByStatus", new Object[]{safeList(status), userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String statusQuery = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASK_BY_PROCESS_INST_ID_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksByStatusByProcessInstanceId", new Object[]{processInstanceId, safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }
        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getAllAuditTask", new Object[]{userId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }


        if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
            return Arrays.asList(taskSummaryList.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskEventInstanceList taskSummaryList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = getPagingQueryString("?sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_EVENTS_GET_URI, valuesMap) + queryString , TaskEventInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTaskEvents", new Object[]{taskId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskEventInstanceList> response = (ServiceResponse<TaskEventInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            taskSummaryList = response.getResult();
        }


        if (taskSummaryList != null && taskSummaryList.getTaskEvents() != null) {
            return Arrays.asList(taskSummaryList.getTaskEvents());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksByVariable(String userId, String variableName, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_BY_VAR_NAME_GET_URI, valuesMap) + queryString, TaskSummaryList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksByVariables", new Object[]{userId, variableName, "", safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getTasks() != null) {
            return Arrays.asList(result.getTasks());
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskSummary> findTasksByVariableAndValue(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + TASKS_BY_VAR_NAME_GET_URI, valuesMap) + queryString + "&varValue=" + variableValue, TaskSummaryList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getTasksByVariables", new Object[]{userId, variableName, variableValue, safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getTasks() != null) {
            return Arrays.asList(result.getTasks());
        }

        return Collections.emptyList();
    }
}

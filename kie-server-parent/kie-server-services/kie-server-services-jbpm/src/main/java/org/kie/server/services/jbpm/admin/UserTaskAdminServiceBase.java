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

package org.kie.server.services.jbpm.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jbpm.services.api.admin.TaskNotification;
import org.jbpm.services.api.admin.TaskReassignment;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.kie.api.task.model.Email;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.runtime.error.ExecutionError;
import org.kie.internal.task.api.TaskModelFactory;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ExecutionErrorInstanceList;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.locator.ByTaskIdContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.kie.server.services.jbpm.ConvertUtils.buildQueryContext;
import static org.kie.server.services.jbpm.ConvertUtils.convertToErrorInstance;
import static org.kie.server.services.jbpm.ConvertUtils.convertToErrorInstanceList;

public class UserTaskAdminServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskAdminServiceBase.class);

    private UserTaskAdminService userTaskAdminService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    private TaskModelFactory factory = TaskModelProvider.getFactory();

    private Function<String, OrganizationalEntity> mapToUser = id -> factory.newUser(id);
    private Function<String, OrganizationalEntity> mapToGroup = id -> factory.newGroup(id);
    private Function<String, OrganizationalEntity> mapToEmails = id -> factory.newEmail(id);

    private boolean bypassAuthUser;

    public UserTaskAdminServiceBase(UserTaskAdminService userTaskAdminService, KieServerRegistry context) {
        this.userTaskAdminService = userTaskAdminService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    public void addPotentialOwners(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        addPotentialOwners(null, containerId, taskId, removeExisting, payload, marshallingType);
    }
    public void addPotentialOwners(String userId, String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));

        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.addPotentialOwners(containerId, taskId.longValue(), removeExisting, entities);
        } else {
            userTaskAdminService.addPotentialOwners(userId, containerId, taskId.longValue(), removeExisting, entities);
        }
        logger.debug("Potential owners {} added to task {}", entities, taskId);
    }

    public void addExcludedOwners(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        addExcludedOwners(null, containerId, taskId, removeExisting, payload, marshallingType);
    }

    public void addExcludedOwners(String userId, String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);


        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.addExcludedOwners(containerId, taskId.longValue(), removeExisting, entities);
        } else {
            userTaskAdminService.addExcludedOwners(userId, containerId, taskId.longValue(), removeExisting, entities);
        }
        

        logger.debug("Excluded owners {} added to task {}", entities, taskId);
    }

    public void addBusinessAdmins(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        addBusinessAdmins(null, containerId, taskId, removeExisting, payload, marshallingType);
    }

    public void addBusinessAdmins(String userId, String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.addBusinessAdmins(containerId, taskId.longValue(), removeExisting, entities);
        } else {
            userTaskAdminService.addBusinessAdmins(userId, containerId, taskId.longValue(), removeExisting, entities);
        }


        logger.debug("Business admins {} added to task {}", entities, taskId);
    }

    public void removePotentialOwners(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        removePotentialOwners(null, containerId, taskId, orgEntities, isUser);
    }

    public void removePotentialOwners(String userId, String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as potential owners", orgEntities, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.removePotentialOwners(containerId, taskId.longValue(), entities);
        } else {
            userTaskAdminService.removePotentialOwners(userId, containerId, taskId.longValue(), entities);
        }


        logger.debug("Potential owners {} removed task {}", entities, taskId);
    }
    public void removeExcludedOwners(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        removeExcludedOwners(null, containerId, taskId, orgEntities, isUser);
    }

    public void removeExcludedOwners(String userId, String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as excluded owners", orgEntities, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.removeExcludedOwners(containerId, taskId.longValue(), entities);
        } else {
            userTaskAdminService.removeExcludedOwners(userId, containerId, taskId.longValue(), entities);
        }

        logger.debug("Excluded owners {} removed task {}", entities, taskId);
    }

    public void removeBusinessAdmins(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        removeBusinessAdmins(null, containerId, taskId, orgEntities, isUser);
    }

    public void removeBusinessAdmins(String userId, String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as business admins", orgEntities, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        if(!bypassAuthUser || userId == null) {
            userTaskAdminService.removeBusinessAdmins(containerId, taskId.longValue(), entities);
        } else {
            userTaskAdminService.removeBusinessAdmins(userId, containerId, taskId.longValue(), entities);
        }


        logger.debug("Business admins {} removed task {}", entities, taskId);
    }

    public void addTaskInputs(String containerId, Number taskId, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        Map<String, Object> data = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        logger.debug("Task input data to be added to a task {} is {}", taskId, data);
        userTaskAdminService.addTaskInputs(containerId, taskId.longValue(), data);
        logger.debug("Task inputs {} added successfully to task {}", data, taskId);
    }

    public void removeTaskInputs(String containerId, Number taskId, List<String> inputNames) {
        logger.debug("About to remove task inputs {} from task {}", inputNames, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        userTaskAdminService.removeTaskInputs(containerId, taskId.longValue(), inputNames.toArray(new String[inputNames.size()]));
        logger.debug("Task inputs {} removed successfully from task {}", inputNames, taskId);
    }

    public void removeTaskOutputs(String containerId, Number taskId, List<String> outputNames) {
        logger.debug("About to remove task outputs {} from task {}", outputNames, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        userTaskAdminService.removeTaskOutputs(containerId, taskId.longValue(), outputNames.toArray(new String[outputNames.size()]));
        logger.debug("Task outputs {} removed successfully from task {}", outputNames, taskId);
    }

    public String reassignWhenNotStarted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to list of org entities (users/groups)", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        Long id = userTaskAdminService.reassignWhenNotStarted(containerId, taskId.longValue(), timeExpression, entities);
        logger.debug("Reassignment (when not started) to {} successfully created for task {} to fire at {}", entities, taskId, timeExpression);

        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String reassignWhenNotCompleted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to list of org entities (users/groups)", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        Long id = userTaskAdminService.reassignWhenNotCompleted(containerId, taskId.longValue(), timeExpression, entities);
        logger.debug("Reassignment (when not completed) to {} successfully created for task {} to fire at {}", entities, taskId, timeExpression);

        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String notifyWhenNotStarted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to EmailNotification (when not started) of task inputs", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        EmailNotification emailNotification = marshallerHelper.unmarshal(containerId, payload, marshallingType, EmailNotification.class, new ByTaskIdContainerLocator(taskId.longValue()));
        logger.debug("Email notification to be added to a task {} is {}", taskId, emailNotification);

        org.kie.internal.task.api.model.EmailNotification email = buildEmail(emailNotification);

        Long id = userTaskAdminService.notifyWhenNotStarted(containerId, taskId.longValue(), timeExpression, email);
        logger.debug("Email notification (when not started) {} added successfully to task {} to be fired after {}", emailNotification, taskId, timeExpression);

        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String notifyWhenNotCompleted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to EmailNotification (when not completed) of task inputs", payload);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        EmailNotification emailNotification = marshallerHelper.unmarshal(containerId, payload, marshallingType, EmailNotification.class, new ByTaskIdContainerLocator(taskId.longValue()));
        logger.debug("Email notification to be added to a task {} is {}", taskId, emailNotification);

        org.kie.internal.task.api.model.EmailNotification email = buildEmail(emailNotification);

        Long id = userTaskAdminService.notifyWhenNotCompleted(containerId, taskId.longValue(), timeExpression, email);
        logger.debug("Email notification (when not completed) {} added successfully to task {} to be fired after {}", emailNotification, taskId, timeExpression);

        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public void cancelNotification(String containerId, Number taskId, Number notificationId) {
        logger.debug("About to cancel notification {} from task {}", notificationId, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        userTaskAdminService.cancelNotification(containerId, taskId.longValue(), notificationId.longValue());
        logger.debug("Notification {} canceled successfully for task {}", notificationId, taskId);
    }

    public void cancelReassignment(String containerId, Number taskId, Number reassignmentId) {
        logger.debug("About to cancel reassignment {} from task {}", reassignmentId, taskId);

        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        userTaskAdminService.cancelReassignment(containerId, taskId.longValue(), reassignmentId.longValue());
        logger.debug("Reassignment {} canceled successfully for task {}", reassignmentId, taskId);
    }

    public TaskReassignmentList getTaskReassignments(String containerId, Number taskId, boolean activeOnly) {
        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        Collection<TaskReassignment> reassignments = userTaskAdminService.getTaskReassignments(containerId, taskId.longValue(), activeOnly);

        List<org.kie.server.api.model.admin.TaskReassignment> converted = reassignments.stream().map(r -> org.kie.server.api.model.admin.TaskReassignment.builder().id(r.getId()).active(r.isActive()).name(r.getName()).reassignAt(r.getDate()).users(r.getPotentialOwners().stream().filter(oe -> oe instanceof User).map(oe -> oe.getId()).collect(toList())).groups(r.getPotentialOwners().stream().filter(oe -> oe instanceof Group).map(oe -> oe.getId()).collect(toList())).build()).collect(toList());

        return new TaskReassignmentList(converted);
    }

    public TaskNotificationList getTaskNotifications(String containerId, Number taskId, boolean activeOnly) {
        containerId = context.getContainerId(containerId, new ByTaskIdContainerLocator(taskId.longValue()));
        Collection<TaskNotification> notifications = userTaskAdminService.getTaskNotifications(containerId, taskId.longValue(), activeOnly);

        List<org.kie.server.api.model.admin.TaskNotification> converted = notifications.stream()
                                                                                       .map(r -> org.kie.server.api.model.admin.TaskNotification.builder()
                                                                                                                                                .id(r.getId())
                                                                                                                                                .active(r.isActive())
                                                                                                                                                .name(r.getName())
                                                                                                                                                .subject(r.getSubject())
                                                                                                                                                .content(r.getContent())
                                                                                                                                                .notifyAt(r.getDate())
                                                                                                                                                .users(r.getRecipients().stream()
                                                                                                                                                        .filter(oe -> oe instanceof User)
                                                                                                                                                        .map(OrganizationalEntity::getId)
                                                                                                                                                        .collect(toList()))
                                                                                                                                                .groups(r.getRecipients()
                                                                                                                                                         .stream()
                                                                                                                                                         .filter(oe -> oe instanceof Group)
                                                                                                                                                         .map(OrganizationalEntity::getId)
                                                                                                                                                         .collect(toList()))
                                                                                                                                                .emails(r.getRecipients()
                                                                                                                                                         .stream()
                                                                                                                                                         .filter(oe -> oe instanceof Email)
                                                                                                                                                         .map(OrganizationalEntity::getId)
                                                                                                                                                         .collect(toList()))
                                                                                                                                                .build()).collect(toList());

        return new TaskNotificationList(converted);
    }

    public ExecutionErrorInstanceList getExecutionErrorsByTaskId(String containerId, Number taskId, boolean includeAcknowledged, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        logger.debug("About to get execution errors for task id {}", taskId);
        List<ExecutionError> errors = userTaskAdminService.getErrorsByTaskId(taskId.longValue(), includeAcknowledged, buildQueryContext(page, pageSize, sort, sortOrder));

        logger.debug("Found errors {}", errors);
        ExecutionErrorInstanceList errorInstanceList = convertToErrorInstanceList(errors);
        return errorInstanceList;
    }

    public ExecutionErrorInstanceList getExecutionErrorsByTaskName(String containerId, String processId, String taskName, boolean includeAcknowledged, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        logger.debug("About to get execution errors for task name {} in process {} and container {}", taskName, processId, containerId);
        List<ExecutionError> errors = null;
        if (containerId != null && !containerId.isEmpty()) {
            errors = userTaskAdminService.getErrorsByTaskName(containerId, processId, taskName, includeAcknowledged, buildQueryContext(page, pageSize, sort, sortOrder));
        } else if (processId != null && !processId.isEmpty()) {
            errors = userTaskAdminService.getErrorsByTaskName(processId, taskName, includeAcknowledged, buildQueryContext(page, pageSize, sort, sortOrder));
        } else if (taskName != null && !taskName.isEmpty()) {
            errors = userTaskAdminService.getErrorsByTaskName(taskName, includeAcknowledged, buildQueryContext(page, pageSize, sort, sortOrder));
        } else {
            errors = userTaskAdminService.getErrors(includeAcknowledged, buildQueryContext(page, pageSize, sort, sortOrder));
        }

        logger.debug("Found errors {}", errors);
        ExecutionErrorInstanceList errorInstanceList = convertToErrorInstanceList(errors);
        return errorInstanceList;
    }

    public ExecutionErrorInstance getError(String errorId) {
        logger.debug("About to get execution error for {}", errorId);

        ExecutionError error = userTaskAdminService.getError(errorId);
        logger.debug("Found error {} for error id {}", error, errorId);
        return convertToErrorInstance(error);
    }

    public void acknowledgeError(List<String> errorIds) {
        logger.debug("About to acknowledge execution error with id {}", errorIds);
        String[] errors = errorIds.toArray(new String[errorIds.size()]);
        userTaskAdminService.acknowledgeError(errors);
        logger.debug("Error {} successfully acknowledged", errorIds);
    }

    /*
     * Helper methods / only for task onwers
     */

    protected OrganizationalEntity[] convert(List<String> orgEntities, boolean isUser) {
        return orgEntities.stream().map(isUser ? mapToUser : mapToGroup).toArray(size -> new OrganizationalEntity[size]);
    }

    protected org.kie.internal.task.api.model.EmailNotification buildEmail(EmailNotification emailNotification) {

        List<OrganizationalEntity> recipients = new ArrayList<>();
        if (emailNotification.getUsers() != null) {
            recipients.addAll(emailNotification.getUsers().stream().map(mapToUser).collect(toList()));
        }
        if (emailNotification.getGroups() != null) {
            recipients.addAll(emailNotification.getGroups().stream().map(mapToGroup).collect(toList()));
        }
        if (emailNotification.getEmails() != null) {
            recipients.addAll(emailNotification.getEmails().stream().map(mapToEmails).collect(toList()));
        }

        org.kie.internal.task.api.model.EmailNotification email = userTaskAdminService.buildEmailNotification(emailNotification.getSubject(), recipients, emailNotification.getBody(), emailNotification.getFrom(), emailNotification.getReplyTo());

        return email;
    }

    protected OrganizationalEntity[] convert(String containerId, Number taskId, String payload, String marshallingType) {
        OrgEntities orgEntities = marshallerHelper.unmarshal(containerId, payload, marshallingType, OrgEntities.class, new ByTaskIdContainerLocator(taskId.longValue()));
        List<OrganizationalEntity> entities = new ArrayList<>();
        if (orgEntities.getUsers() != null) {
            entities.addAll(orgEntities.getUsers().stream().map(mapToUser).collect(toList()));
        }
        if (orgEntities.getGroups() != null) {
            entities.addAll(orgEntities.getGroups().stream().map(mapToGroup).collect(toList()));
        }

        return entities.toArray(new OrganizationalEntity[entities.size()]);
    }


}
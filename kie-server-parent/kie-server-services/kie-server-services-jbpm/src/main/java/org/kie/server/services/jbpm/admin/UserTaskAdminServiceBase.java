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
import java.util.stream.Collectors;

import org.jbpm.services.api.admin.TaskNotification;
import org.jbpm.services.api.admin.TaskReassignment;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelFactory;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.locator.ByTaskIdContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public class UserTaskAdminServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskAdminServiceBase.class);

    private UserTaskAdminService userTaskAdminService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    private TaskModelFactory factory = TaskModelProvider.getFactory();

    private Function<String, OrganizationalEntity> mapToUser =  id -> factory.newUser(id);
    private Function<String, OrganizationalEntity> mapToGroup =  id -> factory.newGroup(id);

    public UserTaskAdminServiceBase(UserTaskAdminService userTaskAdminService, KieServerRegistry context) {
        this.userTaskAdminService = userTaskAdminService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
    }


    public void addPotentialOwners(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        userTaskAdminService.addPotentialOwners(taskId.longValue(), removeExisting, entities);
        logger.debug("Potential owners {} added to task {}", entities, taskId);
    }

    public void addExcludedOwners(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        userTaskAdminService.addExcludedOwners(taskId.longValue(), removeExisting, entities);
        logger.debug("Excluded owners {} added to task {}", entities, taskId);
    }

    public void addBusinessAdmins(String containerId, Number taskId, boolean removeExisting, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        userTaskAdminService.addBusinessAdmins(taskId.longValue(), removeExisting, entities);
        logger.debug("Business admins {} added to task {}", entities, taskId);
    }

    public void removePotentialOwners(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as potential owners", orgEntities, taskId);
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        userTaskAdminService.removePotentialOwners(taskId.longValue(), entities);
        logger.debug("Potential owners {} removed task {}", entities, taskId);
    }

    public void removeExcludedOwners(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as excluded owners", orgEntities, taskId);
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        userTaskAdminService.removeExcludedOwners(taskId.longValue(), entities);
        logger.debug("Excluded owners {} removed task {}", entities, taskId);
    }

    public void removeBusinessAdmins(String containerId, Number taskId, List<String> orgEntities, boolean isUser) {
        logger.debug("About to remove {} from task {} as business admins", orgEntities, taskId);
        OrganizationalEntity[] entities = convert(orgEntities, isUser);

        userTaskAdminService.removeBusinessAdmins(taskId.longValue(), entities);
        logger.debug("Business admins {} removed task {}", entities, taskId);
    }

    public void addTaskInputs(String containerId, Number taskId, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to map of task inputs", payload);
        Map<String, Object> data = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, new ByTaskIdContainerLocator(taskId.longValue()));

        logger.debug("Task input data to be added to a task {} is {}", taskId, data);
        userTaskAdminService.addTaskInputs(taskId.longValue(), data);
        logger.debug("Task inputs {} added successfully to task {}", data, taskId);
    }

    public void removeTaskInputs(String containerId, Number taskId, List<String> inputNames) {
        logger.debug("About to remove task inputs {} from task {}", inputNames, taskId);

        userTaskAdminService.removeTaskInputs(taskId.longValue(), inputNames.toArray(new String[inputNames.size()]));
        logger.debug("Task inputs {} removed successfully from task {}", inputNames, taskId);
    }

    public void removeTaskOutputs(String containerId, Number taskId, List<String> outputNames) {
        logger.debug("About to remove task outputs {} from task {}", outputNames, taskId);

        userTaskAdminService.removeTaskOutputs(taskId.longValue(), outputNames.toArray(new String[outputNames.size()]));
        logger.debug("Task outputs {} removed successfully from task {}", outputNames, taskId);
    }

    public String reassignWhenNotStarted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to list of org entities (users/groups)", payload);

        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        Long id = userTaskAdminService.reassignWhenNotStarted(taskId.longValue(), timeExpression, entities);
        logger.debug("Reassignment (when not started) to {} successfully created for task {} to fire at {}", entities, taskId, timeExpression);

        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String reassignWhenNotCompleted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to list of org entities (users/groups)", payload);
        OrganizationalEntity[] entities = convert(containerId, taskId, payload, marshallingType);

        Long id = userTaskAdminService.reassignWhenNotCompleted(taskId.longValue(), timeExpression, entities);
        logger.debug("Reassignment (when not completed) to {} successfully created for task {} to fire at {}", entities, taskId, timeExpression);


        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String notifyWhenNotStarted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to EmailNotification (when not started) of task inputs", payload);
        EmailNotification emailNotification = marshallerHelper.unmarshal(containerId, payload, marshallingType, EmailNotification.class, new ByTaskIdContainerLocator(taskId.longValue()));
        logger.debug("Email notification to be added to a task {} is {}", taskId, emailNotification);

        org.kie.internal.task.api.model.EmailNotification email = buildEmail(emailNotification);

        Long id = userTaskAdminService.notifyWhenNotStarted(taskId.longValue(), timeExpression, email);
        logger.debug("Email notification (when not started) {} added successfully to task {} to be fired after {}", emailNotification, taskId, timeExpression);


        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public String notifyWhenNotCompleted(String containerId, Number taskId, String timeExpression, String payload, String marshallingType) {
        logger.debug("About to unmarshall payload '{}' to EmailNotification (when not completed) of task inputs", payload);
        EmailNotification emailNotification = marshallerHelper.unmarshal(containerId, payload, marshallingType, EmailNotification.class, new ByTaskIdContainerLocator(taskId.longValue()));
        logger.debug("Email notification to be added to a task {} is {}", taskId, emailNotification);

        org.kie.internal.task.api.model.EmailNotification email = buildEmail(emailNotification);

        Long id = userTaskAdminService.notifyWhenNotCompleted(taskId.longValue(), timeExpression, email);
        logger.debug("Email notification (when not completed) {} added successfully to task {} to be fired after {}", emailNotification, taskId, timeExpression);


        String response = marshallerHelper.marshal(containerId, marshallingType, id, new ByTaskIdContainerLocator(taskId.longValue()));
        return response;
    }

    public void cancelNotification(String containerId, Number taskId, Number notificationId) {
        logger.debug("About to cancel notification {} from task {}", notificationId, taskId);

        userTaskAdminService.cancelNotification(taskId.longValue(), notificationId.longValue());
        logger.debug("Notification {} canceled successfully for task {}", notificationId, taskId);
    }

    public void cancelReassignment(String containerId, Number taskId, Number reassignmentId) {
        logger.debug("About to cancel reassignment {} from task {}", reassignmentId, taskId);

        userTaskAdminService.cancelReassignment(taskId.longValue(), reassignmentId.longValue());
        logger.debug("Reassignment {} canceled successfully for task {}", reassignmentId, taskId);
    }

    public TaskReassignmentList getTaskReassignments(String containerId, Number taskId, boolean activeOnly) {

        Collection<TaskReassignment> reassignments = userTaskAdminService.getTaskReassignments(taskId.longValue(), activeOnly);

        List<org.kie.server.api.model.admin.TaskReassignment> converted = reassignments.stream
        ().map(r -> org.kie.server.api.model.admin.TaskReassignment.builder()
                        .id(r.getId())
                        .active(r.isActive())
                        .name(r.getName())
                        .reassignAt(r.getDate())
                        .users(r.getPotentialOwners().stream().filter(oe -> oe instanceof User).map(oe -> oe.getId()).collect(toList()))
                        .groups(r.getPotentialOwners().stream().filter(oe -> oe instanceof Group).map(oe -> oe.getId()).collect(toList()))
            .build()
        ).collect(toList());

        return new TaskReassignmentList(converted);
    }

    public TaskNotificationList getTaskNotifications(String containerId, Number taskId, boolean activeOnly) {

        Collection<TaskNotification> notifications = userTaskAdminService.getTaskNotifications(taskId.longValue(), activeOnly);

        List<org.kie.server.api.model.admin.TaskNotification> converted = notifications.stream
                ().map(r -> org.kie.server.api.model.admin.TaskNotification.builder()
                        .id(r.getId())
                        .active(r.isActive())
                        .name(r.getName())
                        .subject(r.getSubject())
                        .content(r.getContent())
                        .notifyAt(r.getDate())
                        .users(r.getRecipients().stream().filter(oe -> oe instanceof User).map(oe -> oe.getId()).collect(toList()))
                        .groups(r.getRecipients().stream().filter(oe -> oe instanceof Group).map(oe -> oe.getId()).collect(toList()))
                        .build()
        ).collect(toList());

        return new TaskNotificationList(converted);
    }

    /*
     * Helper methods
     */

    protected OrganizationalEntity[] convert(List<String> orgEntities, boolean isUser) {
        return orgEntities.stream().map( isUser ? mapToUser : mapToGroup ).toArray(size -> new OrganizationalEntity[size]);
    }

    protected org.kie.internal.task.api.model.EmailNotification buildEmail(EmailNotification emailNotification) {

        List<OrganizationalEntity> recipients = new ArrayList<>();
        if (emailNotification.getUsers() != null) {
            recipients.addAll(emailNotification.getUsers().stream().map(mapToUser).collect(toList()));
        }
        if (emailNotification.getGroups() != null) {
            recipients.addAll(emailNotification.getGroups().stream().map(mapToGroup).collect(toList()));
        }
        org.kie.internal.task.api.model.EmailNotification email = userTaskAdminService.buildEmailNotification(emailNotification.getSubject(),
                recipients,
                emailNotification.getBody(),
                emailNotification.getFrom(),
                emailNotification.getReplyTo());

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

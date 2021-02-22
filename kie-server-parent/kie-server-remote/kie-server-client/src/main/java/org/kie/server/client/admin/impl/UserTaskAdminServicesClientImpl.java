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

package org.kie.server.client.admin.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ExecutionErrorInstanceList;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.TaskNotification;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignment;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.admin.UserTaskAdminServicesClient;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;

import static org.kie.server.api.rest.RestURI.*;

public class UserTaskAdminServicesClientImpl extends AbstractKieServicesClientImpl implements UserTaskAdminServicesClient {

    public UserTaskAdminServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public UserTaskAdminServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public void addPotentialOwners(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        addPotentialOwners(null, containerId, taskId, removeExisting, orgEntities);
    }

    @Override
    public void addPotentialOwners(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?remove=" + removeExisting;
            if(BYPASS_AUTH_USER && userId != null) {
                queryString += "&user=" + userId;
            }

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_POT_OWNERS_USERS_URI, valuesMap) + queryString, orgEntities, null, headers);
        } else {
            Object[] argvs = new Object[]{containerId, taskId, removeExisting};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, removeExisting};
            }
            
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "addPotentialOwners", serialize(orgEntities), marshaller.getFormat().getType(), argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void addExcludedOwners(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        addExcludedOwners(null, containerId, taskId, removeExisting, orgEntities);
    }

    @Override
    public void addExcludedOwners(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?remove=" + removeExisting;
            if(BYPASS_AUTH_USER && userId != null) {
                queryString += "&user=" + userId;
            }
            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_EXL_OWNERS_USERS_URI, valuesMap) + queryString, orgEntities, null, headers);
        } else {
            Object[] argvs = new Object[]{containerId, taskId, removeExisting};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, removeExisting};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "addExcludedOwners", serialize(orgEntities), marshaller.getFormat().getType(), argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void addBusinessAdmins(String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        addBusinessAdmins(null, containerId, taskId, removeExisting, orgEntities);
    }
    @Override
    public void addBusinessAdmins(String userId, String containerId, Long taskId, boolean removeExisting, OrgEntities orgEntities) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?remove=" + removeExisting;
            if(BYPASS_AUTH_USER && userId != null) {
                queryString += "&user=" + userId;
            }
            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_ADMINS_USERS_URI, valuesMap) + queryString, orgEntities, null, headers);
        } else {
            Object[] argvs = new Object[]{containerId, taskId, removeExisting};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, removeExisting};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "addBusinessAdmins", serialize(orgEntities), marshaller.getFormat().getType(), argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removePotentialOwnerUsers(String containerId, Long taskId, String... users) {
        removePotentialOwnerUsers(null, containerId, taskId, users);
    }
    @Override
    public void removePotentialOwnerUsers(String userId, String containerId, Long taskId, String... users) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(users).collect(Collectors.joining(",")));

            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_POT_OWNERS_USERS_DELETE_URI, valuesMap) + queryString, null);
        } else {

            List<String> entities = new ArrayList<>(Arrays.asList(users));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removePotentialOwners", argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeExcludedOwnerUsers(String containerId, Long taskId, String... users) {
        removeExcludedOwnerUsers(null, containerId, taskId, users);
    }
    @Override
    public void removeExcludedOwnerUsers(String userId, String containerId, Long taskId, String... users) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(users).collect(Collectors.joining(",")));

            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);
            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_EXL_OWNERS_USERS_DELETE_URI, valuesMap) + queryString, null);
        } else {
            List<String> entities = new ArrayList<>(Arrays.asList(users));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeExcludedOwners", argvs)));
            ServiceResponse<?> response = (ServiceResponse<MigrationReportInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeBusinessAdminUsers(String containerId, Long taskId, String... users) {
        removeBusinessAdminUsers(null, containerId, taskId, users);
    }
    @Override
    public void removeBusinessAdminUsers(String userId, String containerId, Long taskId, String... users) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(users).collect(Collectors.joining(",")));
            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);
            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_ADMINS_USERS_DELETE_URI, valuesMap) + queryString, null);
        } else {
            List<String> entities = new ArrayList<>(Arrays.asList(users));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeBusinessAdmins", argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removePotentialOwnerGroups(String containerId, Long taskId, String... groups) {
        removePotentialOwnerGroups(null, containerId, taskId, groups);
    }

    @Override
    public void removePotentialOwnerGroups(String userId, String containerId, Long taskId, String... groups) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(groups).collect(Collectors.joining(",")));
            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);
            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_POT_OWNERS_GROUPS_DELETE_URI, valuesMap) + queryString, null);
        } else {
            List<String> entities = new ArrayList<>(Arrays.asList(groups));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removePotentialOwners", argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeExcludedOwnerGroups(String containerId, Long taskId, String... groups) {
        removeExcludedOwnerGroups(null, containerId, taskId, groups);
    }

    @Override
    public void removeExcludedOwnerGroups(String userId, String containerId, Long taskId, String... groups) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(groups).collect(Collectors.joining(",")));
            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);
            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_EXL_OWNERS_GROUPS_DELETE_URI, valuesMap) + queryString, null);
        } else {
            List<String> entities = new ArrayList<>(Arrays.asList(groups));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeExcludedOwners", argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeBusinessAdminGroups(String containerId, Long taskId, String... groups) {
        removeBusinessAdminGroups(null, containerId, taskId, groups);
    }

    @Override
    public void removeBusinessAdminGroups(String userId, String containerId, Long taskId, String... groups) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ENTITY_ID, Arrays.stream(groups).collect(Collectors.joining(",")));
            String queryString = this.getUserQueryStr(BYPASS_AUTH_USER && userId != null ? userId : null);
            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_ADMINS_GROUPS_DELETE_URI, valuesMap) + queryString, null);
        } else {
            List<String> entities = new ArrayList<>(Arrays.asList(groups));
            Object[] argvs = new Object[]{containerId, taskId, entities, true};
            if(BYPASS_AUTH_USER && userId != null) {
                argvs = new Object[]{userId, containerId, taskId, entities, true};
            }
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeBusinessAdmins", argvs)));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }


    @Override
    public void addTaskInputs(String containerId, Long taskId, Map<String, Object> data) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_INPUTS_URI, valuesMap), data, null, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "addTaskInputs", serialize(safeMap(data)), marshaller.getFormat().getType(), new Object[]{containerId, taskId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeTaskInputs(String containerId, Long taskId, String... inputs) {
        List<String> names = new ArrayList<>(Arrays.asList(inputs));
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = getAdditionalParams("", "name", names);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_INPUTS_URI, valuesMap) + queryString, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeTaskInputs", new Object[]{containerId, taskId, names})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeTaskOutputs(String containerId, Long taskId, String... outputs) {
        List<String> names = new ArrayList<>(Arrays.asList(outputs));
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = getAdditionalParams("", "name", names);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_OUTPUTS_URI, valuesMap) + queryString, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "removeTaskOutputs", new Object[]{containerId, taskId, names})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Long reassignWhenNotStarted(String containerId, Long taskId, String expiresAt, OrgEntities orgEntities) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?expiresAt=" + expiresAt+"&whenNotStarted=true";

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_REASSIGNMENTS_URI, valuesMap) + queryString, orgEntities, Object.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "reassignWhenNotStarted", serialize(orgEntities), marshaller.getFormat().getType(), new Object[]{containerId, taskId, expiresAt})));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public Long reassignWhenNotCompleted(String containerId, Long taskId, String expiresAt, OrgEntities orgEntities) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?expiresAt=" + expiresAt+"&whenNotCompleted=true";

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_REASSIGNMENTS_URI, valuesMap) + queryString, orgEntities, Object.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "reassignWhenNotCompleted", serialize(orgEntities), marshaller.getFormat().getType(), new Object[]{containerId, taskId, expiresAt})));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public Long notifyWhenNotStarted(String containerId, Long taskId, String expiresAt, EmailNotification emailNotification) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?expiresAt=" + expiresAt+"&whenNotStarted=true";

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_NOTIFICATIONS_URI, valuesMap) + queryString, emailNotification, Object.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "notifyWhenNotStarted", serialize(emailNotification), marshaller.getFormat().getType(), new Object[]{containerId, taskId, expiresAt})));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public Long notifyWhenNotCompleted(String containerId, Long taskId, String expiresAt, EmailNotification emailNotification) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryString = "?expiresAt=" + expiresAt+"&whenNotCompleted=true";

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_NOTIFICATIONS_URI, valuesMap) + queryString, emailNotification, Object.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("UserTaskAdminService", "notifyWhenNotCompleted", serialize(emailNotification), marshaller.getFormat().getType(), new Object[]{containerId, taskId, expiresAt})));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public void cancelNotification(String containerId, Long taskId, Long notificationId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(NOTIFICATION_ID, notificationId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_NOTIFICATION_DELETE_URI, valuesMap), null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "cancelNotification", new Object[]{containerId, taskId, notificationId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void cancelReassignment(String containerId, Long taskId, Long reassignmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(REASSIGNMENT_ID, reassignmentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_REASSIGNMENT_DELETE_URI, valuesMap), null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "cancelReassignment", new Object[]{containerId, taskId, reassignmentId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<TaskNotification> getTaskNotifications(String containerId, Long taskId, boolean activeOnly) {
        TaskNotificationList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = "?activeOnly=" + activeOnly;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_NOTIFICATIONS_URI, valuesMap) + queryString, TaskNotificationList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getTaskNotifications", new Object[]{containerId, taskId, activeOnly}) ) );
            ServiceResponse<TaskNotificationList> response = (ServiceResponse<TaskNotificationList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<TaskReassignment> getTaskReassignments(String containerId, Long taskId, boolean activeOnly) {
        TaskReassignmentList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = "?activeOnly=" + activeOnly;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + TASK_INSTANCE_REASSIGNMENTS_URI, valuesMap) + queryString, TaskReassignmentList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getTaskReassignments", new Object[]{containerId, taskId, activeOnly}) ) );
            ServiceResponse<TaskReassignmentList> response = (ServiceResponse<TaskReassignmentList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<ExecutionErrorInstance> getTaskErrors(String containerId, boolean includeAcknowledged, Integer page, Integer pageSize) {
        ExecutionErrorInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = "?includeAck=" + includeAcknowledged;
            queryString = getPagingQueryString(queryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + ERRORS_GET_URI, valuesMap) + queryString, ExecutionErrorInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getExecutionErrorsByTaskName", new Object[]{containerId, "", "", includeAcknowledged, page, pageSize, "", true}) ) );
            ServiceResponse<ExecutionErrorInstanceList> response = (ServiceResponse<ExecutionErrorInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<ExecutionErrorInstance> getErrorsByTaskId(String containerId, Long taskId, boolean includeAcknowledged, Integer page, Integer pageSize) {
        ExecutionErrorInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = "?includeAck=" + includeAcknowledged;
            queryString = getPagingQueryString(queryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + ERRORS_BY_TASK_ID_GET_URI, valuesMap) + queryString, ExecutionErrorInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getExecutionErrorsByTaskId", new Object[]{containerId, taskId, includeAcknowledged, page, pageSize, "", true}) ) );
            ServiceResponse<ExecutionErrorInstanceList> response = (ServiceResponse<ExecutionErrorInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<ExecutionErrorInstance> getErrorsByTaskInfo(String containerId, Long processId, String taskName, boolean includeAcknowledged, Integer page, Integer pageSize) {
        ExecutionErrorInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = "?includeAck=" + includeAcknowledged+"&name=" + taskName + "&process=" + processId;
            queryString = getPagingQueryString(queryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + ERRORS_GET_URI, valuesMap) + queryString, ExecutionErrorInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getExecutionErrorsByTaskName", new Object[]{containerId, processId, taskName, includeAcknowledged, page, pageSize, "", true}) ) );
            ServiceResponse<ExecutionErrorInstanceList> response = (ServiceResponse<ExecutionErrorInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }
    
    @Override
    public void acknowledgeError(String containerId, String... errorId) {
        ArrayList<String> errorIds = new ArrayList<>(Arrays.asList(errorId));
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            Map<String, String> headers = new HashMap<String, String>();

            String endpoint = ACK_ERROR_PUT_URI;
            String queryString = "";
            if (errorIds.size() > 1) {
                endpoint = ACK_ERRORS_PUT_URI;
                queryString = getAdditionalParams(queryString, "errorId", errorIds);
            } else {
                valuesMap.put(ERROR_ID, errorIds.get(0));
            }

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + endpoint, valuesMap) + queryString, "", String.class, headers);
        } else {

            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "UserTaskAdminService", "acknowledgeError", new Object[]{safeList(errorIds)})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public ExecutionErrorInstance getError(String containerId, String errorId) {
        ExecutionErrorInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(ERROR_ID, errorId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_TASK_URI + "/" + ERROR_GET_URI , valuesMap), ExecutionErrorInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "UserTaskAdminService", "getError", new Object[]{errorId}) ) );
            ServiceResponse<ExecutionErrorInstance> response = (ServiceResponse<ExecutionErrorInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        return result;
    }
}

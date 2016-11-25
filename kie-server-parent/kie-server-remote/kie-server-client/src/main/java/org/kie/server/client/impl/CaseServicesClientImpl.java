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

package org.kie.server.client.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.cases.CaseAdHocFragment;
import org.kie.server.api.model.cases.CaseAdHocFragmentList;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseCommentList;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseMilestoneList;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.cases.CaseStageList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesConfiguration;

import static org.kie.server.api.rest.RestURI.*;

public class CaseServicesClientImpl extends AbstractKieServicesClientImpl implements CaseServicesClient {

    public CaseServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public CaseServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public String startCase(String containerId, String caseDefinitionId) {
        return startCase(containerId, caseDefinitionId, new CaseFile());
    }

    @Override
    public String startCase(String containerId, String caseDefinitionId, CaseFile caseFile) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_DEF_ID, caseDefinitionId);

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + START_CASE_POST_URI, valuesMap), caseFile, String.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "startCase", serialize(caseFile), marshaller.getFormat().getType(), new Object[]{containerId, caseDefinitionId})) );
            ServiceResponse<String> response = (ServiceResponse<String>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }

        if (result instanceof Wrapped) {
            return (String) ((Wrapped) result).unwrap();
        }
        return (String) result;
    }

    @Override
    public CaseInstance getCaseInstance(String containerId, String caseId) {
        return getCaseInstance(containerId, caseId, false, false, false, false);
    }

    @Override
    public CaseInstance getCaseInstance(String containerId, String caseId, boolean withData, boolean withRoles, boolean withMilestones, boolean withStages) {
        CaseInstance caseInstance = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = "?withData=" + withData + "&withRoles=" + withRoles + "&withMilestones=" + withMilestones + "&withStages=" + withStages;

            caseInstance = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_INSTANCE_GET_URI, valuesMap) + queryString, CaseInstance.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "getCaseInstance", marshaller.getFormat().getType(), new Object[]{containerId, caseId, withData, withRoles, withMilestones, withStages})) );
            ServiceResponse<String> response = (ServiceResponse<String>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            caseInstance = deserialize(response.getResult(), CaseInstance.class);
        }

        return caseInstance;
    }

    public void cancelOrDestroyCaseInstance(String containerId, String caseId, boolean destroy) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = "?destroy=" + destroy;

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_INSTANCE_DELETE_URI, valuesMap) + queryString, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "cancelCaseInstance", new Object[]{containerId, caseId, destroy})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void cancelCaseInstance(String containerId, String caseId) {
        cancelOrDestroyCaseInstance(containerId, caseId, false);
    }

    @Override
    public void destroyCaseInstance(String containerId, String caseId) {
        cancelOrDestroyCaseInstance(containerId, caseId, true);
    }

    @Override
    public void reopenCase(String caseId, String containerId, String caseDefinitionId) {
        reopenCase(caseId, containerId, caseDefinitionId, new HashMap<>());
    }

    @Override
    public void reopenCase(String caseId, String containerId, String caseDefinitionId, Map<String, Object> data) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_DEF_ID, caseDefinitionId);
            valuesMap.put(CASE_ID, caseId);


            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + REOPEN_CASE_PUT_URI, valuesMap), data, null, new HashMap<String, String>());
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "reopenCase", serialize(safeMap(data)), marshaller.getFormat().getType(), new Object[]{caseId, containerId, caseDefinitionId})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Map<String, Object> getCaseInstanceData(String containerId, String caseId) {
        Object result = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_FILE_GET_URI, valuesMap), Object.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "getCaseFileData", marshaller.getFormat().getType(), new Object[]{containerId, caseId})) );
            ServiceResponse<String> response = (ServiceResponse<String>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = deserialize(response.getResult(), Object.class);
        }
        if (result instanceof Wrapped) {
            return (Map) ((Wrapped) result).unwrap();
        }
        return (Map) result;
    }

    @Override
    public Object getCaseInstanceData(String containerId, String caseId, String name) {
        Object result = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_FILE_ITEM, name);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_FILE_BY_NAME_GET_URI, valuesMap), Object.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "getCaseFileDataByName", marshaller.getFormat().getType(), new Object[]{containerId, caseId, name})) );
            ServiceResponse<String> response = (ServiceResponse<String>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

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
    public void putCaseInstanceData(String containerId, String caseId, Map<String, Object> data) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_FILE_POST_URI, valuesMap), data, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "putCaseFileData", serialize(safeMap(data)), marshaller.getFormat().getType(), new Object[]{containerId, caseId})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void putCaseInstanceData(String containerId, String caseId, String name, Object data) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_FILE_ITEM, name);

            makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_FILE_BY_NAME_POST_URI, valuesMap), data, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "putCaseFileDataByName", serialize(data), marshaller.getFormat().getType(), new Object[]{containerId, caseId, name})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeCaseInstanceData(String containerId, String caseId, String... names) {
        List<String> variablesToRemove = Arrays.asList(names);
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getAdditionalParams("", CASE_FILE_ITEM, variablesToRemove);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_FILE_DELETE_URI, valuesMap) + queryString, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "removeCaseFileDataByName", new Object[]{containerId, caseId, variablesToRemove})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void addDynamicUserTask(String containerId, String caseId, String name, String description, String actors, String groups, Map<String, Object> data) {
        Map<String, Object> taskSpecMap = new HashMap<String, Object>();
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NAME_PROP, name);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DESC_PROP, description);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_ACTORS_PROP, actors);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_GROUPS_PROP, groups);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DATA_PROP, data);

        internalAddDynamicTask(containerId, caseId, null, taskSpecMap);
    }

    @Override
    public void addDynamicTask(String containerId, String caseId, String nodeType, String name, Map<String, Object> data) {
        Map<String, Object> taskSpecMap = new HashMap<String, Object>();
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NAME_PROP, name);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NODE_TYPE_PROP, nodeType);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DATA_PROP, data);

        internalAddDynamicTask(containerId, caseId, null, taskSpecMap);
    }

    @Override
    public void addDynamicUserTaskToStage(String containerId, String caseId, String stageId, String name, String description, String actors, String groups, Map<String, Object> data) {
        Map<String, Object> taskSpecMap = new HashMap<String, Object>();
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NAME_PROP, name);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DESC_PROP, description);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_ACTORS_PROP, actors);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_GROUPS_PROP, groups);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DATA_PROP, data);

        internalAddDynamicTask(containerId, caseId, stageId, taskSpecMap);

    }

    @Override
    public void addDynamicTaskToStage(String containerId, String caseId, String stageId, String nodeType, String name, Map<String, Object> data) {
        Map<String, Object> taskSpecMap = new HashMap<String, Object>();
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NAME_PROP, name);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_NODE_TYPE_PROP, nodeType);
        taskSpecMap.put(KieServerConstants.CASE_DYNAMIC_DATA_PROP, data);

        internalAddDynamicTask(containerId, caseId, stageId, taskSpecMap);
    }

    @Override
    public void addDynamicSubProcess(String containerId, String caseId, String processId, Map<String, Object> data) {
        internalAddDynamicSubProcess(containerId, caseId, null, processId, data);
    }

    @Override
    public void addDynamicSubProcessToStage(String containerId, String caseId, String stageId, String processId, Map<String, Object> data) {
        internalAddDynamicSubProcess(containerId, caseId, stageId, processId, data);
    }

    @Override
    public void triggerAdHocFragment(String containerId, String caseId, String adHocName, Map<String, Object> data) {
        internalTriggerAdHoc(containerId, caseId, null, adHocName, data);
    }

    @Override
    public void triggerAdHocFragmentInStage(String containerId, String caseId, String stageId, String adHocName, Map<String, Object> data) {
        internalTriggerAdHoc(containerId, caseId, stageId, adHocName, data);
    }

    @Override
    public List<CaseMilestone> getMilestones(String containerId, String caseId, boolean achievedOnly, Integer page, Integer pageSize) {
        CaseMilestoneList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getPagingQueryString("?achievedOnly=" + achievedOnly, page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_MILESTONES_GET_URI, valuesMap) + queryString, CaseMilestoneList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getMilestones", new Object[]{containerId, caseId, achievedOnly, page, pageSize})) );
            ServiceResponse<CaseMilestoneList> response = (ServiceResponse<CaseMilestoneList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseStage> getStages(String containerId, String caseId, boolean activeOnly, Integer page, Integer pageSize) {
        CaseStageList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getPagingQueryString("?activeOnly=" + activeOnly, page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_STAGES_GET_URI, valuesMap) + queryString, CaseStageList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getStages", new Object[]{containerId, caseId, activeOnly, page, pageSize})) );
            ServiceResponse<CaseStageList> response = (ServiceResponse<CaseStageList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseAdHocFragment> getAdHocFragments(String containerId, String caseId) {
        CaseAdHocFragmentList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_AD_HOC_FRAGMENTS_GET_URI, valuesMap), CaseAdHocFragmentList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getAdHocFragments", new Object[]{containerId, caseId})) );
            ServiceResponse<CaseAdHocFragmentList> response = (ServiceResponse<CaseAdHocFragmentList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseRoleAssignment> getRoleAssignments(String containerId, String caseId) {
        CaseRoleAssignmentList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_ROLES_GET_URI, valuesMap), CaseRoleAssignmentList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "getRoleAssignment", new Object[]{containerId, caseId})) );
            ServiceResponse<CaseRoleAssignmentList> response = (ServiceResponse<CaseRoleAssignmentList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<NodeInstance> getActiveNodes(String containerId, String caseId, Integer page, Integer pageSize) {
        NodeInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getPagingQueryString("", page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_NODE_INSTANCES_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getActiveNodes", new Object[]{containerId, caseId, page, pageSize})) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> getActiveProcessInstances(String containerId, String caseId, Integer page, Integer pageSize) {
        return getProcessInstances(containerId, caseId, null, page, pageSize);
    }

    @Override
    public List<ProcessInstance> getActiveProcessInstances(String containerId, String caseId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return getProcessInstances(containerId, caseId, null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<ProcessInstance> getProcessInstances(String containerId, String caseId, List<Integer> status, Integer page, Integer pageSize) {
        return getProcessInstances(containerId, caseId, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> getProcessInstances(String containerId, String caseId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getProcessInstancesForCase", new Object[]{containerId, caseId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public void assignUserToRole(String containerId, String caseId, String roleName, String user) {
        internalAddRoleAssignment(containerId, caseId, roleName, user, null);
    }

    @Override
    public void assignGroupToRole(String containerId, String caseId, String roleName, String group) {
        internalAddRoleAssignment(containerId, caseId, roleName, null, group);
    }

    @Override
    public void removeUserFromRole(String containerId, String caseId, String roleName, String user) {
        internalRemoveRoleAssignment(containerId, caseId, roleName, user, null);
    }

    @Override
    public void removeGroupFromRole(String containerId, String caseId, String roleName, String group) {
        internalRemoveRoleAssignment(containerId, caseId, roleName, null, group);
    }

    @Override
    public List<CaseComment> getComments(String containerId, String caseId, Integer page, Integer pageSize) {
        CaseCommentList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = getPagingQueryString("", page, pageSize);
            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_COMMENTS_GET_URI, valuesMap) + queryString, CaseCommentList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "getComments", new Object[]{containerId, caseId, page, pageSize})) );
            ServiceResponse<CaseCommentList> response = (ServiceResponse<CaseCommentList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public void addComment(String containerId, String caseId, String author, String text) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);

            String queryString = "?author="+ author;

            makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_COMMENTS_POST_URI, valuesMap) + queryString, text, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "addCommentToCase", serialize(text), marshaller.getFormat().getType(), new Object[]{containerId, caseId, author})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void updateComment(String containerId, String caseId, String commentId, String author, String text) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_COMMENT_ID, commentId);

            String queryString = "?author="+ author;

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_COMMENTS_PUT_URI, valuesMap) + queryString, serialize(text), null, new HashMap<String, String>());
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "updateCommentInCase", serialize(text), marshaller.getFormat().getType(), new Object[]{containerId, caseId, commentId, author})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void removeComment(String containerId, String caseId, String commentId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_COMMENT_ID, commentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_COMMENTS_DELETE_URI, valuesMap), null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "removeCommentFromCase", new Object[]{containerId, caseId, commentId})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<CaseInstance> getCaseInstances(Integer page, Integer pageSize) {
        return getCaseInstances(null, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstances(List<Integer> status, Integer page, Integer pageSize) {
        return getCaseInstances(status, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstances(Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return getCaseInstances(null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<CaseInstance> getCaseInstances(List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI + "/" + CASE_ALL_INSTANCES_GET_URI, valuesMap) + queryString, CaseInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseInstances", new Object[]{safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseInstanceList> response = (ServiceResponse<CaseInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseInstance> getCaseInstancesOwnedBy(String owner, List<Integer> status, Integer page, Integer pageSize) {
        return getCaseInstancesOwnedBy(owner, status, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstancesOwnedBy(String owner, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?owner="+owner, page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI + "/" + CASE_ALL_INSTANCES_GET_URI, valuesMap) + queryString, CaseInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseInstancesOwnedBy", new Object[]{owner, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseInstanceList> response = (ServiceResponse<CaseInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseInstance> getCaseInstancesByContainer(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        return getCaseInstancesByContainer(containerId, status, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstancesByContainer(String containerId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_INSTANCES_GET_URI, valuesMap) + queryString, CaseInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseInstancesByContainer", new Object[]{containerId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseInstanceList> response = (ServiceResponse<CaseInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseInstance> getCaseInstancesByDefinition(String containerId, String caseDefinitionId, List<Integer> status, Integer page, Integer pageSize) {
        return getCaseInstancesByDefinition(containerId, caseDefinitionId, status, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstancesByDefinition(String containerId, String caseDefinitionId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_DEF_ID, caseDefinitionId);

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_INSTANCES_BY_DEF_GET_URI, valuesMap) + queryString, CaseInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseInstancesByDefinition", new Object[]{containerId, caseDefinitionId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseInstanceList> response = (ServiceResponse<CaseInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseDefinition> getCaseDefinitionsByContainer(String containerId, Integer page, Integer pageSize) {
        return getCaseDefinitionsByContainer(containerId, page, pageSize, "", true);
    }

    @Override
    public List<CaseDefinition> getCaseDefinitionsByContainer(String containerId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseDefinitionList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI, valuesMap) + queryString, CaseDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseDefinitionsByContainer", new Object[]{containerId, page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseDefinitionList> response = (ServiceResponse<CaseDefinitionList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseDefinition> getCaseDefinitions(String filter, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseDefinitionList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String filterQueryString = "";
            if (filter != null) {
                filterQueryString = "?filter=" + emptyIfNull(filter);
            }
            String queryString = getPagingQueryString(filterQueryString, page, pageSize);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI, valuesMap) + queryString, CaseDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseDefinitions", new Object[]{emptyIfNull(filter), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseDefinitionList> response = (ServiceResponse<CaseDefinitionList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public List<CaseDefinition> getCaseDefinitions(Integer page, Integer pageSize) {
        return getCaseDefinitions(null, page, pageSize, "", true);
    }

    @Override
    public List<CaseDefinition> getCaseDefinitions(String filter, Integer page, Integer pageSize) {
        return getCaseDefinitions(filter, page, pageSize, "", true);
    }

    @Override
    public List<CaseDefinition> getCaseDefinitions(Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return getCaseDefinitions(null, page, pageSize, sort, sortOrder);
    }

    @Override
    public CaseDefinition getCaseDefinition(String containerId, String caseDefinitionId) {
        CaseDefinition result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_DEF_ID, caseDefinitionId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_GET_URI, valuesMap), CaseDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseDefinition", new Object[]{containerId, caseDefinitionId})) );
            ServiceResponse<CaseDefinition> response = (ServiceResponse<CaseDefinition>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        return result;
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsBusinessAdministrator(caseId, userId, null, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, List<String> status, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsBusinessAdministrator(caseId, userId, status, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsPotentialOwner(caseId, userId, null, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, List<String> status, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsPotentialOwner(caseId, userId, status, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsStakeholder(caseId, userId, null, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, List<String> status, Integer page, Integer pageSize) {
        return findCaseTasksAssignedAsStakeholder(caseId, userId, status, page, pageSize, "", false);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return findCaseTasksAssignedAsBusinessAdministrator(caseId, userId, null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CASE_ID, caseId);

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI + "/" + CASE_TASKS_AS_ADMIN_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseTasksAsBusinessAdmin", new Object[]{caseId, userId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

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
    public List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return findCaseTasksAssignedAsPotentialOwner(caseId, userId, null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CASE_ID, caseId);

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI + "/" + CASE_TASKS_AS_POT_OWNER_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseTasks", new Object[]{caseId, userId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

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
    public List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return findCaseTasksAssignedAsStakeholder(caseId, userId, null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        TaskSummaryList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CASE_ID, caseId);

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize)+"&sort="+sort+"&sortOrder="+sortOrder;

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_QUERY_URI + "/" + CASE_TASKS_AS_STAKEHOLDER_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseTasksAsStakeholder", new Object[]{caseId, userId, safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<TaskSummaryList> response = (ServiceResponse<TaskSummaryList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

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

    /*
     * internal methods
     */

    protected void internalAddDynamicTask(String containerId, String caseId, String stageId, Map<String, Object> taskSpecMap) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            if (stageId != null) {
                valuesMap.put(CASE_STAGE_ID, stageId);

                makeHttpPostRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_TASK_IN_STAGE_POST_URI, valuesMap), taskSpecMap, null);
            } else {
                makeHttpPostRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_TASK_POST_URI, valuesMap), taskSpecMap, null);
            }
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "addDynamicTask", serialize(taskSpecMap), marshaller.getFormat().getType(), new Object[]{containerId, caseId, emptyIfNull(stageId)})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    protected void internalAddDynamicSubProcess(String containerId, String caseId, String stageId, String processId, Map<String, Object> taskSpecMap) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(PROCESS_ID, processId);
            if (stageId != null) {
                valuesMap.put(CASE_STAGE_ID, stageId);

                makeHttpPostRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_PROCESS_IN_STAGE_POST_URI, valuesMap), taskSpecMap, null);
            } else {
                makeHttpPostRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_PROCESS_POST_URI, valuesMap), taskSpecMap, null);
            }
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "addDynamicSubprocess", serialize(safeMap(taskSpecMap)), marshaller.getFormat().getType(), new Object[]{containerId, caseId, emptyIfNull(stageId), processId})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    protected void internalTriggerAdHoc(String containerId, String caseId, String stageId, String adHocName, Map<String, Object> data) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_NODE_NAME, adHocName);
            if (stageId != null) {
                valuesMap.put(CASE_STAGE_ID, stageId);
                makeHttpPutRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_TASK_IN_STAGE_PUT_URI, valuesMap), data, null, new HashMap<String, String>());
            } else {
                makeHttpPutRequestAndCreateCustomResponse(
                        build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_DYNAMIC_TASK_PUT_URI, valuesMap), data, null, new HashMap<String, String>());
            }
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "triggerAdHocNode", serialize(safeMap(data)), marshaller.getFormat().getType(), new Object[]{containerId, caseId, emptyIfNull(stageId), emptyIfNull(adHocName)})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    protected void internalRemoveRoleAssignment(String containerId, String caseId, String roleName, String user, String group) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_ROLE_NAME, roleName);

            String queryString = "?user=" + emptyIfNull(user) + "&group=" + emptyIfNull(group);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_ROLES_DELETE_URI, valuesMap) + queryString, null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "removeFromRole", new Object[]{containerId, caseId, roleName, emptyIfNull(user), emptyIfNull(group)})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }


    protected void internalAddRoleAssignment(String containerId, String caseId, String roleName, String user, String group) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            valuesMap.put(CASE_ROLE_NAME, roleName);

            String queryString = "?user=" + emptyIfNull(user) + "&group=" + emptyIfNull(group);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), CASE_URI + "/" + CASE_ROLES_PUT_URI, valuesMap) + queryString, null, null, new HashMap<String, String>());
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseService", "assignToRole", new Object[]{containerId, caseId, roleName, emptyIfNull(user), emptyIfNull(group)})) );
            ServiceResponse<?> response = (ServiceResponse<?>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    protected String emptyIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}

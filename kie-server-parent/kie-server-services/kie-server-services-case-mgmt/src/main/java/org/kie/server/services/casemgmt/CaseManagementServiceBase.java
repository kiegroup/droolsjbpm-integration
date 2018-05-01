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

package org.kie.server.services.casemgmt;

import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_ACTORS_PROP;
import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_DATA_PROP;
import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_DESC_PROP;
import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_GROUPS_PROP;
import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_NAME_PROP;
import static org.kie.server.api.KieServerConstants.CASE_DYNAMIC_NODE_TYPE_PROP;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.jbpm.casemgmt.api.CaseDefinitionNotFoundException;
import org.jbpm.casemgmt.api.CaseNotFoundException;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.casemgmt.api.dynamic.TaskSpecification;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseRoleInstance;
import org.jbpm.casemgmt.api.model.instance.CommentInstance;
import org.jbpm.casemgmt.api.model.instance.CommentSortBy;
import org.jbpm.services.api.DeploymentNotFoundException;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.TaskModelFactory;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseCommentList;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.locator.ByCaseIdContainerLocator;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseManagementServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(CaseManagementServiceBase.class);

    private IdentityProvider identityProvider;

    private CaseService caseService;
    private CaseRuntimeDataService caseRuntimeDataService;
    private MarshallerHelper marshallerHelper;

    private KieServerRegistry context;

    private TaskModelFactory taskModelFactory;

    private boolean bypassAuthUser = false;

    public CaseManagementServiceBase(CaseService caseService, CaseRuntimeDataService caseRuntimeDataService, KieServerRegistry context) {
        this.caseService = caseService;
        this.caseRuntimeDataService = caseRuntimeDataService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.identityProvider = context.getIdentityProvider();
        this.context = context;

        this.taskModelFactory = TaskModelProvider.getFactory();

        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    protected String getUser(String queryParamUser) {
        if (bypassAuthUser) {
            return queryParamUser;
        }

        return identityProvider.getName();
    }

    public String startCase(String containerId, String caseDefinitionId, String payload, String marshallingType) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());

        CaseDefinition caseDef = caseRuntimeDataService.getCase(containerId, caseDefinitionId);
        if( caseDef == null ) {
            throw new CaseDefinitionNotFoundException("Unable to find case '" + caseDefinitionId + "' in container " + containerId);
        }

        logger.debug("About to unmarshal case file from payload: '{}'", payload);
        CaseFile caseFile = marshallerHelper.unmarshal(containerId, payload, marshallingType, CaseFile.class);
        String caseId;

        if (caseFile == null) {
            logger.debug("Case file not given, starting case without case file");
            caseId = caseService.startCase(containerId, caseDefinitionId);
        } else {
            logger.debug("Case file provided {}", caseFile);

            Map<String, List<String>> accessRestrictions = null;
            if (caseFile.getAccessRestrictions() != null) {
                accessRestrictions = new HashMap<>();
                
                for (Entry<String, String[]> entry : caseFile.getAccessRestrictions().entrySet()) {
                    accessRestrictions.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            }

            Map<String, OrganizationalEntity[]> roleAssignments = getRolesAssignments(caseFile);
            CaseFileInstance caseFileInstance = caseService.newCaseFileInstanceWithRestrictions(containerId, caseDefinitionId, caseFile.getData(), roleAssignments, accessRestrictions);
            caseId = caseService.startCase(containerId, caseDefinitionId, caseFileInstance);
        }
        logger.debug("New case instance started with case id {} for case definition {}", caseId, caseDefinitionId);
        // return response
        return marshallerHelper.marshal(containerId, marshallingType, caseId);
    }

    protected Map<String, OrganizationalEntity[]> getRolesAssignments(CaseFile caseFile) {
        Map<String, OrganizationalEntity[]> roleAssignments = new HashMap<>();
        if (caseFile.getUserAssignments() != null) {
            caseFile.getUserAssignments()
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        OrganizationalEntity[] users = Arrays.asList(entry.getValue()).stream().map(user -> taskModelFactory.newUser(user)).toArray(OrganizationalEntity[]::new);
                        roleAssignments.put(entry.getKey(), users);
                    });
        }

        if (caseFile.getGroupAssignments() != null) {
            caseFile.getGroupAssignments()
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        OrganizationalEntity[] groups = Arrays.asList(entry.getValue()).stream().map(group -> taskModelFactory.newGroup(group)).toArray(OrganizationalEntity[]::new);
                        roleAssignments.compute(entry.getKey(), (k, v) ->
                            v == null ? groups : ArrayUtils.addAll(groups, v)
                        );
                    });
        }
        return roleAssignments;
    }

    public String getCaseInstance(String containerId, String caseId, boolean withData, boolean withRoles, boolean withMilestones, boolean withStages, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        CaseInstance actualCaseInstance = caseService.getCaseInstance(caseId, withData, withRoles, withMilestones, withStages);

        org.kie.server.api.model.cases.CaseInstance caseInstance = ConvertUtils.transformCaseInstance(actualCaseInstance);
        if (actualCaseInstance.getStatus().equals(ProcessInstance.STATE_ACTIVE)) {
            if (withData) {
                caseInstance.setCaseFile(CaseFile.builder().data(actualCaseInstance.getCaseFile().getData()).build());
            }
    
            if (withMilestones) {
                caseInstance.setMilestones(ConvertUtils.transformMilestones(actualCaseInstance.getCaseMilestones()));
            }
    
            if (withStages) {
                caseInstance.setStages(ConvertUtils.transformStages(actualCaseInstance.getCaseStages()));
            }
    
            if (withRoles) {
                caseInstance.setRoleAssignments(ConvertUtils.transformRoleAssignment(actualCaseInstance.getCaseRoles()));
            }
        }
        logger.debug("About to marshal case instance with id '{}' {}", caseId, caseInstance);
        return marshallerHelper.marshal(containerId, marshallingType, caseInstance, new ByCaseIdContainerLocator(caseId));

    }
    
    public void closeCaseInstance(String containerId, String caseId, String payload, String marshallingType) {
        verifyContainerId(containerId, caseId);   
        logger.debug("About to unmarshal task name from payload: '{}'", payload);
        String comment = marshallerHelper.unmarshal(containerId, payload, marshallingType, String.class);
        
        logger.debug("Closing case with id {} inside container {} with comment {}", caseId, containerId, comment);
        caseService.closeCase(caseId, comment);        
    }

    public void cancelCaseInstance(String containerId, String caseId, boolean destroy) {
    	verifyContainerId(containerId, caseId);
        if (destroy) {
            logger.debug("Destroying case with id {} inside container {}", caseId, containerId);
            caseService.destroyCase(caseId);
        } else {
            logger.debug("Canceling case with id {} inside container {}", caseId, containerId);
            caseService.cancelCase(caseId);
        }
    }

    public void reopenCase(String caseId, String containerId, String caseDefinitionId, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        containerId = context.getContainerId(containerId, new ByCaseIdContainerLocator(caseId));

        CaseDefinition caseDef = caseRuntimeDataService.getCase(containerId, caseDefinitionId);
        if( caseDef == null ) {
            throw new IllegalStateException("Unable to find case '" + caseDefinitionId + "' in container " + containerId);
        }

        logger.debug("About to unmarshal data from payload: '{}'", payload);
        Map<String, Object> data = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        caseService.reopenCase(caseId, containerId, caseDefinitionId, data);
        logger.debug("Case {} successfully reopened", caseId);

    }

    public String getCaseFileData(String containerId, String caseId, List<String> names, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        CaseFileInstance caseFileInstance = caseService.getCaseFileInstance(caseId);

        Map<String, Object> caseFileData = caseFileInstance.getData();
        
        if (names != null && !names.isEmpty()) {
            logger.debug("Filtering case file data to return only items with following names {}", names);
            Map<String, Object> filtered = new HashMap<>();
            
            for (String name : names) {
                if (caseFileData.containsKey(name)) {
                    filtered.put(name, caseFileData.get(name));
                }
            }
            
            caseFileData = filtered;
        }
        logger.debug("About to marshal case file data for case with id '{}' {}", caseId, caseFileData);
        return marshallerHelper.marshal(containerId, marshallingType, caseFileData, new ByCaseIdContainerLocator(caseId));

    }

    public String getCaseFileDataByName(String containerId, String caseId, String name, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        CaseFileInstance caseFileInstance = caseService.getCaseFileInstance(caseId);

        Object caseFileData = caseFileInstance.getData(name);
        logger.debug("About to marshal case file data (name = {}) for case with id '{}' {}", name, caseId, caseFileData);
        return marshallerHelper.marshal(containerId, marshallingType, caseFileData, new ByCaseIdContainerLocator(caseId));

    }

    public void putCaseFileData(String containerId, String caseId, List<String> restrictions, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        logger.debug("About to unmarshal case file data from payload: '{}'", payload);
        Map<String, Object> caseFileData = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, new ByCaseIdContainerLocator(caseId));
        logger.debug("Unmarshalled case file data {} for case with id '{}' with restrictions", caseFileData, caseId, restrictions);
        caseService.addDataToCaseFile(caseId, caseFileData, restrictions.toArray(new String[restrictions.size()]));
    }

    public void putCaseFileDataByName(String containerId, String caseId, String name, List<String> restrictions, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        logger.debug("About to unmarshal case file data from payload: '{}'", payload);
        Object caseFileData = marshallerHelper.unmarshal(containerId, payload, marshallingType, Object.class, new ByCaseIdContainerLocator(caseId));
        logger.debug("Unmarshalled case file data {} for case with id '{}' will be stored under {} with restrictions", caseFileData, caseId, name, restrictions);
        caseService.addDataToCaseFile(caseId, name, caseFileData, restrictions.toArray(new String[restrictions.size()]));
    }

    public void removeCaseFileDataByName(String containerId, String caseId, List<String> names) {
    	verifyContainerId(containerId, caseId);
        logger.debug("Removing {} variables from case with id '{}'", names, caseId);
        caseService.removeDataFromCaseFile(caseId, names);

    }

    public void addDynamicTask(String containerId, String caseId, String stageId, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        logger.debug("About to unmarshal task specification content from payload: '{}'", payload);
        Map<String, Object> taskSpecificationMap = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, new ByCaseIdContainerLocator(caseId));
        TaskSpecification taskSpecification = null;

        if (taskSpecificationMap == null || taskSpecificationMap.isEmpty()) {
            throw new IllegalArgumentException("Task specification must be given");
        }

        String nodeType = (String) taskSpecificationMap.get(CASE_DYNAMIC_NODE_TYPE_PROP);
        if (nodeType != null) {
            logger.debug("Creating dynamic task of typ {} within case {}", nodeType, caseId);

            taskSpecification = caseService.newTaskSpec(nodeType, (String) taskSpecificationMap.get(CASE_DYNAMIC_NAME_PROP), (Map<String, Object>) taskSpecificationMap.get(CASE_DYNAMIC_DATA_PROP));
        } else {
            logger.debug("Creating dynamic user task for case {}", caseId);
            taskSpecification = caseService.newHumanTaskSpec((String) taskSpecificationMap.get(CASE_DYNAMIC_NAME_PROP),
                    (String) taskSpecificationMap.get(CASE_DYNAMIC_DESC_PROP),
                    (String) taskSpecificationMap.get(CASE_DYNAMIC_ACTORS_PROP),
                    (String) taskSpecificationMap.get(CASE_DYNAMIC_GROUPS_PROP),
                    (Map<String, Object>) taskSpecificationMap.get(CASE_DYNAMIC_DATA_PROP));
        }

        logger.debug("Complete task specification is '{}'", taskSpecification);
        if (stageId != null && !stageId.isEmpty()) {
            logger.debug("Adding dynamic task to stage {} within case {}", stageId, caseId);
            caseService.addDynamicTaskToStage(caseId, stageId, taskSpecification);
        } else {
            logger.debug("Adding dynamic task to case {}", caseId);
            caseService.addDynamicTask(caseId, taskSpecification);
        }
    }

    public void addDynamicSubprocess(String containerId, String caseId, String stageId, String processId, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        logger.debug("About to unmarshal process data from payload: '{}'", payload);
        Map<String, Object> subProcessParameters = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, new ByCaseIdContainerLocator(caseId));

        logger.debug("SubProcess data '{}'", subProcessParameters);
        if (stageId != null && !stageId.isEmpty()) {
            logger.debug("Adding dynamic subprocess to stage {} within case {}", stageId, caseId);
            caseService.addDynamicSubprocessToStage(caseId, stageId, processId, subProcessParameters);
        } else {
            logger.debug("Adding dynamic subprocess to case {}", caseId);
            caseService.addDynamicSubprocess(caseId, processId, subProcessParameters);
        }
    }

    public void triggerAdHocNode(String containerId, String caseId, String stageId, String adHocName, String payload, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        logger.debug("About to unmarshal task data from payload: '{}'", payload);
        Map<String, Object> adHocTaskData = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class, new ByCaseIdContainerLocator(caseId));

        logger.debug("AdHoc task {} will be triggered with data = {}", adHocName, adHocTaskData);
        if (stageId != null && !stageId.isEmpty()) {
            caseService.triggerAdHocFragment(caseId, stageId, adHocName, adHocTaskData);
        } else {
            caseService.triggerAdHocFragment(caseId, adHocName, adHocTaskData);
        }
    }

    public CaseRoleAssignmentList getRoleAssignment(String containerId, String caseId) {
    	verifyContainerId(containerId, caseId);
        Collection<CaseRoleInstance> caseRoleInstances = caseService.getCaseRoleAssignments(caseId);
        logger.debug("Roles assignments for case {} are {}", caseId, caseRoleInstances);
        List<CaseRoleAssignment> caseRoles = ConvertUtils.transformRoleAssignment(caseRoleInstances);
        CaseRoleAssignmentList caseRolesList = new CaseRoleAssignmentList(caseRoles);

        return caseRolesList;
    }

    public void assignToRole(String containerId, String caseId, String roleName, String user, String group) {
    	verifyContainerId(containerId, caseId);
        OrganizationalEntity entity = null;

        if (user != null && !user.isEmpty()) {
            entity = taskModelFactory.newUser(user);
            logger.debug("Assigning user {} to role {} in case {}", user, roleName, caseId);
            caseService.assignToCaseRole(caseId, roleName, entity);
        }
        if (group != null && !group.isEmpty()) {
            entity = taskModelFactory.newGroup(group);
            logger.debug("Assigning group {} to role {} in case {}", group, roleName, caseId);
            caseService.assignToCaseRole(caseId, roleName, entity);
        }
    }

    public void removeFromRole(String containerId, String caseId, String roleName, String user, String group) {
    	verifyContainerId(containerId, caseId);
        OrganizationalEntity entity = null;

        if (user != null && !user.isEmpty()) {
            entity = taskModelFactory.newUser(user);
            logger.debug("Removing user {} from role {} in case {}", user, roleName, caseId);
            caseService.removeFromCaseRole(caseId, roleName, entity);
        }
        if (group != null && !group.isEmpty()) {
            entity = taskModelFactory.newGroup(group);
            logger.debug("Removing group {} from role {} in case {}", group, roleName, caseId);
            caseService.removeFromCaseRole(caseId, roleName, entity);
        }
    }

    public String addCommentToCase(String containerId, String caseId, String author, List<String> restrictions, String comment, String marshallingType) {
    	verifyContainerId(containerId, caseId);
    	author = getUser(author);
        String actualComment = marshallerHelper.unmarshal(containerId, comment, marshallingType, String.class, new ByCaseIdContainerLocator(caseId));

        logger.debug("Adding comment to case {} by {} with text '{}' with restrictions {}", caseId, author, actualComment, restrictions);
        String commentId = caseService.addCaseComment(caseId, author, actualComment, restrictions.toArray(new String[restrictions.size()]));
        return marshallerHelper.marshal(containerId, marshallingType, commentId);
    }

    public void updateCommentInCase(String containerId, String caseId, String commentId, String author, List<String> restrictions, String comment, String marshallingType) {
    	verifyContainerId(containerId, caseId);
        author = getUser(author);
        String actualComment = marshallerHelper.unmarshal(containerId, comment, marshallingType, String.class, new ByCaseIdContainerLocator(caseId));

        logger.debug("Updating comment {} in case {} by {} with text '{}' with restrictions {}", commentId, caseId, author, actualComment, restrictions);
        caseService.updateCaseComment(caseId, commentId, author, actualComment, restrictions.toArray(new String[restrictions.size()]));
    }

    public void removeCommentFromCase(String containerId, String caseId, String commentId) {
    	verifyContainerId(containerId, caseId);
        logger.debug("Removing comment with id {} from case {}", commentId, caseId);
        caseService.removeCaseComment(caseId, commentId);
    }

    public CaseCommentList getComments(String containerId, String caseId, Integer page, Integer pageSize) {
        return getComments(containerId, caseId, null, page, pageSize);
    }

    public CaseCommentList getComments(String containerId, String caseId, String sort, Integer page, Integer pageSize) {
    	verifyContainerId(containerId, caseId);
        CommentSortBy sortBy = parseCommentSortBy(sort);
        Collection<CommentInstance> caseComments = caseService.getCaseComments(caseId, sortBy, ConvertUtils.buildQueryContext(page, pageSize));
        logger.debug("Comments for case {} are {}", caseId, caseComments);
        List<CaseComment> comments = ConvertUtils.transformCaseComments(caseComments);
        CaseCommentList commentsList = new CaseCommentList(comments);

        return commentsList;
    }

    protected CommentSortBy parseCommentSortBy(String sort) {
        if (sort == null || sort.isEmpty()) {
            return CommentSortBy.Date;
        }

        if (sort.equalsIgnoreCase("date")) {
            return CommentSortBy.Date;
        } else if (sort.equalsIgnoreCase("author")) {
            return CommentSortBy.Author;
        }

        logger.warn("Unexpected sort option for case comments '{}', returning default one - by date");
        return CommentSortBy.Date;
    }
    
    private void verifyContainerId(String containerId, String caseId) {
        String caseContainerId;
        try {
        	caseContainerId = (new ByCaseIdContainerLocator(caseId)).locateContainer(containerId, null);

        } catch (IllegalArgumentException e) {
            throw new CaseNotFoundException(e.getMessage());
        }

        KieContainerInstanceImpl taskContainer = context.getContainer(caseContainerId);
        List<KieContainerInstanceImpl> containersByAlias = context.getContainersForAlias(containerId);

        // The container id is either a non-existent one or is not a valid alias for the container id the task is associated with. Both scenarios should raise an exception.
        if (context.getContainer(containerId) == null && !containersByAlias.contains(taskContainer)) {
        	throw new DeploymentNotFoundException("CaseId: " + caseId + " is not associated with the provided container id: " + containerId + " or its alias.");
        }
    }

}

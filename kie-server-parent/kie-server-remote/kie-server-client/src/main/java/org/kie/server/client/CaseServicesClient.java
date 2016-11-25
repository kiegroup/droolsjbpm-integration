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

package org.kie.server.client;

import java.util.List;
import java.util.Map;

import org.kie.server.api.model.cases.CaseAdHocFragment;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;

public interface CaseServicesClient {

    public static final String SORT_BY_CASE_DEFINITION_ID = "CaseId";
    public static final String SORT_BY_CASE_DEFINITION_NAME = "CaseName";
    public static final String SORT_BY_CASE_DEFINITION_DEPLOYMENT_ID = "Project";

    public static final String SORT_BY_CASE_INSTANCE_ID = "CorrelationKey";
    public static final String SORT_BY_CASE_INSTANCE_NAME = "ProcessName";

    public static final String SORT_BY_PROCESS_INSTANCE_ID = "ProcessInstanceId";

    String startCase(String containerId, String caseDefinitionId);

    String startCase(String containerId, String caseDefinitionId, CaseFile caseFile);

    CaseInstance getCaseInstance(String containerId, String caseId);

    CaseInstance getCaseInstance(String containerId, String caseId, boolean withData, boolean withRoles, boolean withMilestones, boolean withStages);

    void cancelCaseInstance(String containerId, String caseId);

    void destroyCaseInstance(String containerId, String caseId);

    void reopenCase(String caseId, String containerId, String caseDefinitionId);

    void reopenCase(String caseId, String containerId, String caseDefinitionId, Map<String, Object> data);

    Map<String, Object> getCaseInstanceData(String containerId, String caseId);

    Object getCaseInstanceData(String containerId, String caseId, String name);

    void putCaseInstanceData(String containerId, String caseId, Map<String, Object> data);

    void putCaseInstanceData(String containerId, String caseId, String name, Object data);

    void removeCaseInstanceData(String containerId, String caseId, String... names);

    void addDynamicUserTask(String containerId, String caseId, String name, String description, String actors, String groups, Map<String, Object> data);

    void addDynamicTask(String containerId, String caseId, String nodeType, String name, Map<String, Object> data);

    void addDynamicUserTaskToStage(String containerId, String caseId, String stageId, String name, String description, String actors, String groups, Map<String, Object> data);

    void addDynamicTaskToStage(String containerId, String caseId, String stageId, String nodeType, String name, Map<String, Object> data);

    void addDynamicSubProcess(String containerId, String caseId, String processId, Map<String, Object> data);

    void addDynamicSubProcessToStage(String containerId, String caseId, String stageId,  String processId, Map<String, Object> data);

    void triggerAdHocFragment(String containerId, String caseId, String adHocName, Map<String, Object> data);

    void triggerAdHocFragmentInStage(String containerId, String caseId, String stageId, String adHocName, Map<String, Object> data);

    List<CaseMilestone> getMilestones(String containerId, String caseId, boolean achievedOnly, Integer page, Integer pageSize);

    List<CaseStage> getStages(String containerId, String caseId, boolean activeOnly, Integer page, Integer pageSize);

    List<CaseAdHocFragment> getAdHocFragments(String containerId, String caseId);

    List<CaseRoleAssignment> getRoleAssignments(String containerId, String caseId);

    List<NodeInstance> getActiveNodes(String containerId, String caseId, Integer page, Integer pageSize);

    List<ProcessInstance> getActiveProcessInstances(String containerId, String caseId, Integer page, Integer pageSize);

    List<ProcessInstance> getActiveProcessInstances(String containerId, String caseId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> getProcessInstances(String containerId, String caseId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> getProcessInstances(String containerId, String caseId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    void assignUserToRole(String containerId, String caseId, String roleName, String user);

    void assignGroupToRole(String containerId, String caseId, String roleName, String group);

    void removeUserFromRole(String containerId, String caseId, String roleName, String user);

    void removeGroupFromRole(String containerId, String caseId, String roleName, String group);

    List<CaseComment> getComments(String containerId, String caseId, Integer page, Integer pageSize);

    void addComment(String containerId, String caseId, String author, String text);

    void updateComment(String containerId, String caseId, String commentId, String author, String text);

    void removeComment(String containerId, String caseId, String commentId);

    List<CaseInstance> getCaseInstances(Integer page, Integer pageSize);

    List<CaseInstance> getCaseInstances(List<Integer> status, Integer page, Integer pageSize);

    List<CaseInstance> getCaseInstances(Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseInstance> getCaseInstances(List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseInstance> getCaseInstancesOwnedBy(String owner, List<Integer> status, Integer page, Integer pageSize);

    List<CaseInstance> getCaseInstancesOwnedBy(String owner, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseInstance> getCaseInstancesByContainer(String containerId, List<Integer> status, Integer page, Integer pageSize);

    List<CaseInstance> getCaseInstancesByContainer(String containerId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseInstance> getCaseInstancesByDefinition(String containerId, String caseDefinitionId, List<Integer> status, Integer page, Integer pageSize);

    List<CaseInstance> getCaseInstancesByDefinition(String containerId, String caseDefinitionId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseDefinition> getCaseDefinitionsByContainer(String containerId, Integer page, Integer pageSize);

    List<CaseDefinition> getCaseDefinitionsByContainer(String containerId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseDefinition> getCaseDefinitions(Integer page, Integer pageSize);

    List<CaseDefinition> getCaseDefinitions(String filter, Integer page, Integer pageSize);

    List<CaseDefinition> getCaseDefinitions(Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<CaseDefinition> getCaseDefinitions(String filter, Integer page, Integer pageSize, String sort, boolean sortOrder);

    CaseDefinition getCaseDefinition(String containerId, String caseDefinitionId);

    List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, List<String> status, Integer page, Integer pageSize);

    List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findCaseTasksAssignedAsBusinessAdministrator(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findCaseTasksAssignedAsPotentialOwner(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<TaskSummary> findCaseTasksAssignedAsStakeholder(String caseId, String userId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder);
}

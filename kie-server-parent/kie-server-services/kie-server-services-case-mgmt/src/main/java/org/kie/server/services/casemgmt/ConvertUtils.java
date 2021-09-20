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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.casemgmt.api.admin.CaseMigrationReport;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.CaseFileItem;
import org.jbpm.casemgmt.api.model.CaseRole;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseMilestoneInstance;
import org.jbpm.casemgmt.api.model.instance.CaseRoleInstance;
import org.jbpm.casemgmt.api.model.instance.CaseStageInstance;
import org.jbpm.casemgmt.api.model.instance.CommentInstance;
import org.jbpm.services.api.admin.MigrationEntry;
import org.jbpm.services.api.admin.MigrationReport;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.cases.CaseAdHocFragment;
import org.kie.server.api.model.cases.CaseComment;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseFileDataItem;
import org.kie.server.api.model.cases.CaseFileDataItemList;
import org.kie.server.api.model.cases.CaseInstanceCustomVars;
import org.kie.server.api.model.cases.CaseInstanceCustomVarsList;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseMilestoneDefinition;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.cases.CaseStageDefinition;
import org.kie.server.api.model.cases.CaseUserTaskWithVariables;
import org.kie.server.api.model.cases.CaseUserTaskWithVariablesList;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummaryList;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ConvertUtils {

    public static List<CaseRoleAssignment> transformRoleAssignment(Collection<CaseRoleInstance> roleInstances) {
        List<CaseRoleAssignment> roleAssignments = new ArrayList<>();
        if (roleInstances != null) {
            roleInstances.forEach(r ->
                            roleAssignments.add(CaseRoleAssignment.builder()
                                    .name(r.getRoleName())
                                    .users(r.getRoleAssignments().stream()
                                            .filter(oe -> oe instanceof User)
                                            .map(OrganizationalEntity::getId)
                                            .collect(toList()))
                                    .groups(r.getRoleAssignments().stream()
                                            .filter(oe -> oe instanceof Group)
                                            .map(OrganizationalEntity::getId)
                                            .collect(toList()))
                                    .build())
            );
        }
        return roleAssignments;
    }

    public static List<CaseMilestone> transformMilestones(Collection<CaseMilestoneInstance> milestoneInstances) {
        List<CaseMilestone> milestones = new ArrayList<>();
        if (milestoneInstances != null) {
            milestoneInstances.forEach(m ->
                    milestones.add(CaseMilestone.builder()
                            .id(m.getId())
                            .name(m.getName())
                            .status(m.getStatus().toString())
                            .achieved(m.isAchieved())
                            .achievedAt(m.getAchievedAt())
                            .build()));
        }
        return milestones;
    }

    public static List<CaseStage> transformStages(Collection<CaseStageInstance> stageInstances) {
        List<CaseStage> stages = new ArrayList<>();
        if (stageInstances != null) {
            stageInstances.forEach(s ->
                            stages.add(CaseStage.builder()
                                    .id(s.getId())
                                    .name(s.getName())
                                    .status(s.getStatus().toString())
                                    .activeNodes(transformNodeInstance(s.getActiveNodes()))
                                    .adHocFragments(s.getAdHocFragments().stream().map(adhoc ->
                                            CaseAdHocFragment.builder()
                                                    .name(adhoc.getName())
                                                    .type(adhoc.getType())
                                                    .build())
                                            .collect(toList()))
                                    .build())
            );
        }

        return stages;
    }

    public static org.kie.server.api.model.cases.CaseInstance transformCaseInstance(CaseInstance actualCaseInstance) {
        if (actualCaseInstance == null) {
            return null;
        }
        return org.kie.server.api.model.cases.CaseInstance.builder()
                .caseId(actualCaseInstance.getCaseId())
                .caseDefinitionId(actualCaseInstance.getCaseDefinitionId())
                .containerId(actualCaseInstance.getDeploymentId())
                .caseDescription(actualCaseInstance.getCaseDescription())
                .caseOwner(actualCaseInstance.getOwner())
                .caseStatus(actualCaseInstance.getStatus())
                .startedAt(actualCaseInstance.getStartedAt())
                .completedAt(actualCaseInstance.getCompletedAt())
                .completionMessage(actualCaseInstance.getCompletionMessage())
                .slaCompliance(actualCaseInstance.getSlaCompliance())
                .slaDueDate(actualCaseInstance.getSlaDueDate())
                .caseFile(toCaseFile(actualCaseInstance))
                .build();
    }

    private static CaseFile toCaseFile(CaseInstance caseFile) {
        return caseFile.getCaseFile() != null ? CaseFile.builder().data(caseFile.getCaseFile().getData()).build() : null;
    }

    public static List<CaseComment> transformCaseComments(Collection<CommentInstance> caseComments) {
        List<CaseComment> comments = new ArrayList<>();

        if (caseComments != null) {
            caseComments.forEach(c -> {
                String[] restrictions = new String[0];
                if (c.getRestrictedTo() != null) {
                    restrictions = c.getRestrictedTo().toArray(new String[c.getRestrictedTo().size()]);
                }
                comments.add(CaseComment.builder()
                        .id(c.getId())
                        .author(c.getAuthor())
                        .addedAt(c.getCreatedAt())
                        .text(c.getComment())
                        .restrictedTo(restrictions)
                        .build());
            }
            );
        }
        return comments;
    }

    public static List<NodeInstance> transformNodeInstance(Collection<NodeInstanceDesc> nodeInstances) {
        if (nodeInstances == null) {
            return Collections.emptyList();
        }
        return nodeInstances.stream().map(an ->
                NodeInstance.builder()
                        .id(an.getId())
                        .name(an.getName())
                        .date(an.getDataTimeStamp())
                        .completed(an.isCompleted())
                        .connection(an.getConnection())
                        .containerId(an.getDeploymentId())
                        .nodeType(an.getNodeType())
                        .nodeId(an.getNodeId())
                        .processInstanceId(an.getProcessInstanceId())
                        .workItemId(an.getWorkItemId())
                        .referenceId(an.getReferenceId())
                        .slaCompliance(an.getSlaCompliance())
                        .slaDueDate(an.getSlaDueDate())
                        .build())
                .collect(toList());
    }


    public static QueryContext buildQueryContext(Integer page, Integer pageSize) {
        checkPagination(page, pageSize);
        return new QueryContext(page * pageSize, pageSize);
    }

    public static QueryContext buildQueryContext(Integer page, Integer pageSize, String orderBy, boolean asc) {
        checkPagination(page, pageSize);
        if (orderBy != null && !orderBy.isEmpty()) {
            return new QueryContext(page * pageSize, pageSize, orderBy, asc);
        }

        return new QueryContext(page * pageSize, pageSize);
    }

    public static List<ProcessInstance> transformProcessInstance(Collection<ProcessInstanceDesc> processInstanceDescs) {
        if (processInstanceDescs == null) {
            return Collections.emptyList();
        }

        return processInstanceDescs.stream().map(pi ->
                org.kie.server.api.model.instance.ProcessInstance.builder()
                        .id(pi.getId())
                        .processId(pi.getProcessId())
                        .processName(pi.getProcessName())
                        .processVersion(pi.getProcessVersion())
                        .containerId(pi.getDeploymentId())
                        .processInstanceDescription(pi.getProcessInstanceDescription())
                        .correlationKey(pi.getCorrelationKey())
                        .parentInstanceId(pi.getParentId())
                        .date(pi.getDataTimeStamp())
                        .initiator(pi.getInitiator())
                        .state(pi.getState())
                        .slaCompliance(pi.getSlaCompliance())
                        .slaDueDate(pi.getSlaDueDate())
                        .build()
        )
        .collect(toList());
    }

    public static List<org.kie.server.api.model.cases.CaseInstance> transformCaseInstances(Collection<CaseInstance> caseInstanceDescs) {
        if (caseInstanceDescs == null) {
            return Collections.emptyList();
        }

        return caseInstanceDescs.stream().map(c -> transformCaseInstance(c)).collect(toList());
    }

    public static org.kie.server.api.model.cases.CaseDefinition transformCase(CaseDefinition caseDefinition) {
        if (caseDefinition == null) {
            return null;
        }

        return org.kie.server.api.model.cases.CaseDefinition.builder()
                .id(caseDefinition.getId())
                .caseIdPrefix(caseDefinition.getIdentifierPrefix())
                .name(caseDefinition.getName())
                .version(caseDefinition.getVersion())
                .containerId(caseDefinition.getDeploymentId())
                .roles(caseDefinition.getCaseRoles().stream().collect(toMap(CaseRole::getName, CaseRole::getCardinality)))
                .adHocFragments(caseDefinition.getAdHocFragments().stream().map(adf -> CaseAdHocFragment.builder()
                        .name(adf.getName())
                        .type(adf.getType())
                        .build())
                        .collect(toList()))
                .stages(caseDefinition.getCaseStages().stream().map(s -> CaseStageDefinition.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .adHocFragments(s.getAdHocFragments().stream().map(adf -> CaseAdHocFragment.builder()
                                .name(adf.getName())
                                .type(adf.getType())
                                .build())
                                .collect(toList()))
                        .build()).collect(toList()))
                .milestones(caseDefinition.getCaseMilestones().stream().map(m -> CaseMilestoneDefinition.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .mandatory(m.isMandatory())
                        .build())
                        .collect(toList()))
                .build();

    }

    public static List<org.kie.server.api.model.cases.CaseDefinition> transformCases(Collection<CaseDefinition> caseDescs) {
        if (caseDescs == null) {
            return Collections.emptyList();
        }
        return caseDescs.stream().map(c -> transformCase(c)).collect(toList());
    }

    public static TaskSummaryList convertToTaskSummaryList(Collection<TaskSummary> tasks) {
        if (tasks == null) {
            return new TaskSummaryList(new org.kie.server.api.model.instance.TaskSummary[0]);
        }
        org.kie.server.api.model.instance.TaskSummary[] instances = new org.kie.server.api.model.instance.TaskSummary[tasks.size()];
        int counter = 0;
        for (TaskSummary taskSummary : tasks) {

            instances[counter] = convertToTaskSummary(taskSummary);
            counter++;
        }

        return new TaskSummaryList(instances);
    }

    public static org.kie.server.api.model.instance.TaskSummary convertToTaskSummary(TaskSummary taskSummary) {
        org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary.builder()
                .id(taskSummary.getId())
                .name(taskSummary.getName())
                .description(taskSummary.getDescription())
                .subject(taskSummary.getSubject())
                .taskParentId(taskSummary.getParentId())
                .activationTime(taskSummary.getActivationTime())
                .actualOwner(taskSummary.getActualOwnerId())
                .containerId(taskSummary.getDeploymentId())
                .createdBy(taskSummary.getCreatedById())
                .createdOn(taskSummary.getCreatedOn())
                .expirationTime(taskSummary.getExpirationTime())
                .priority(taskSummary.getPriority())
                .processId(taskSummary.getProcessId())
                .processInstanceId(taskSummary.getProcessInstanceId())
                .status(taskSummary.getStatusId())
                .skipable(taskSummary.isSkipable())
                .build();
        return task;
    }

    public static ProcessDefinitionList transformProcesses(Collection<ProcessDefinition> definitions) {
        if (definitions == null) {
            return new ProcessDefinitionList(new org.kie.server.api.model.definition.ProcessDefinition[0]);
        }

        List<org.kie.server.api.model.definition.ProcessDefinition> processes = new ArrayList<org.kie.server.api.model.definition.ProcessDefinition>(definitions.size());
        for (ProcessDefinition pd : definitions) {
            org.kie.server.api.model.definition.ProcessDefinition definition = transformProcess(pd);

            processes.add(definition);
        }

        return new ProcessDefinitionList(processes);
    }

    public static org.kie.server.api.model.definition.ProcessDefinition transformProcess(ProcessDefinition processDesc) {
        if (processDesc == null) {
            return null;
        }

        org.kie.server.api.model.definition.ProcessDefinition processDefinition = org.kie.server.api.model.definition.ProcessDefinition.builder()
                .id(processDesc.getId())
                .name(processDesc.getName())
                .packageName(processDesc.getPackageName())
                .version(processDesc.getVersion())
                .containerId(processDesc.getDeploymentId())
                .dynamic(processDesc.isDynamic())
                .build();

        return processDefinition;
    }

    public static CaseFileDataItemList transformCaseFileDataItems(Collection<CaseFileItem> caseFileItems) {
        if (caseFileItems == null) {
            return new CaseFileDataItemList(new CaseFileDataItem[0]);
        }

        List<CaseFileDataItem> caseFileDataItems = new ArrayList<CaseFileDataItem>(caseFileItems.size());
        for (CaseFileItem item : caseFileItems) {
            CaseFileDataItem caseFileDataItem = transformCaseFileDataItem(item);

            caseFileDataItems.add(caseFileDataItem);
        }

        return new CaseFileDataItemList(caseFileDataItems);
    }

    public static CaseFileDataItem transformCaseFileDataItem(CaseFileItem caseFileItem) {
        if (caseFileItem == null) {
            return null;
        }

        CaseFileDataItem caseFileDataItem = CaseFileDataItem.builder()
                .caseId(caseFileItem.getCaseId())
                .name(caseFileItem.getName())
                .value(caseFileItem.getValue())
                .type(caseFileItem.getType())
                .lastModified(caseFileItem.getLastModified())
                .lastModifiedBy(caseFileItem.getLastModifiedBy())
                .build();

        return caseFileDataItem;
    }

    private static void checkPagination(final Integer page, final Integer pageSize) {
        if (page < 0) {
            throw new IllegalArgumentException(MessageFormat.format(Messages.ILLEGAL_PAGE, page));
        }

        if (pageSize < 0) {
            throw new IllegalArgumentException(MessageFormat.format(Messages.ILLEGAL_PAGE_SIZE, pageSize));
        }
    }


    public static  MigrationReportInstanceList convertMigrationReports(List<MigrationReport> reports) {

        if (reports == null) {
            return new MigrationReportInstanceList();
        }
        MigrationReportInstance[] reportInstances = new MigrationReportInstance[reports.size()];
        int index = 0;
        for (MigrationReport report : reports) {

            MigrationReportInstance instance = convertMigrationReport(report);
            reportInstances[index] = instance;

            index++;
        }
        return new MigrationReportInstanceList(reportInstances);
    }

    public static  MigrationReportInstance convertMigrationReport(MigrationReport report) {
        if (report == null) {
            return null;
        }
        MigrationReportInstance reportInstance = MigrationReportInstance.builder()
                .successful(report.isSuccessful())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .logs(convertLogs(report.getEntries()))
                .processInstanceId(report.getProcessInstanceId())
                .build();

        return reportInstance;
    }


    public static  List<String> convertLogs(List<MigrationEntry> entries) {

        List<String> logs = new ArrayList<String>();
        if (entries != null) {
            for (MigrationEntry entry : entries) {
                logs.add(entry.getType() + " " + entry.getTimestamp() + " " + entry.getMessage());
            }
        }
        return logs;
    }

    public static  CaseMigrationReportInstance convertCaseMigrationReport(String caseId, CaseMigrationReport report) {
        if (report == null) {
            return null;
        }
        CaseMigrationReportInstance reportInstance = CaseMigrationReportInstance.builder()
                .successful(report.isSuccessful())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .reports(convertReports(report.getReports()))
                .caseId(caseId)
                .build();

        return reportInstance;
    }

    public static MigrationReportInstance[] convertReports(List<MigrationReport> entries) {
        MigrationReportInstance[] reports = new MigrationReportInstance[entries.size()];
        int counter = 0;
        if (entries != null) {
            for (MigrationReport entry : entries) {
                reports[counter++] = convertMigrationReport(entry);
            }
        }
        return reports;
    }

    public static CaseUserTaskWithVariablesList convertToCaseUserTaskWithVariablesList(List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables) {
        List<CaseUserTaskWithVariables> data = new ArrayList<>();
        for (UserTaskInstanceWithPotOwnerDesc desc : queryUserTasksByVariables) {
            CaseUserTaskWithVariables var = new CaseUserTaskWithVariables();
            var.setId(desc.getTaskId());
            var.setName(desc.getName());
            var.setDescription(desc.getDescription());
            var.setProcessInstanceId(desc.getProcessInstanceId());
            var.setCorrelationKey(desc.getCorrelationKey());
            var.setActualOwner(desc.getActualOwner());
            var.setCaseDefinitionId(desc.getProcessId());
            var.setPotentialOwners(desc.getPotentialOwners());
            var.setCaseId(desc.getCorrelationKey());
            var.setProcessVariables(desc.getProcessVariables());
            var.setInputVariables(desc.getInputdata());
            var.setCaseVariables(desc.getExtraData());
            var.setStatus(desc.getStatus());
            data.add(var);
        }

        CaseUserTaskWithVariablesList result = new CaseUserTaskWithVariablesList();
        result.setCaseUserTaskWithVariables(data.stream().toArray(CaseUserTaskWithVariables[]::new));
        return result;
    }

    public static CaseInstanceCustomVarsList convertToCaseInstanceCustomVarsList(List<ProcessInstanceWithVarsDesc> queryCaseByVariables) {
        List<CaseInstanceCustomVars> processInstances = new ArrayList<>();
        for (ProcessInstanceWithVarsDesc proc : queryCaseByVariables) {
            CaseInstanceCustomVars tmp = new CaseInstanceCustomVars();
            tmp.setProcessInstanceId(proc.getId());
            tmp.setProcessName(proc.getProcessName());
            tmp.setProcessVersion(proc.getProcessVersion());
            tmp.setCaseId(proc.getCorrelationKey());
            tmp.setCaseDefinitionId(proc.getProcessId());
            tmp.setCorrelationKey(proc.getCorrelationKey());
            tmp.setProcessVariables(proc.getVariables());
            tmp.setCaseVariables(proc.getExtraData());
            tmp.setInitiator(proc.getInitiator());
            tmp.setState(proc.getState());
            tmp.setContainerId(proc.getDeploymentId());
            processInstances.add(tmp);
        }

        CaseInstanceCustomVarsList result = new CaseInstanceCustomVarsList();
        result.setCaseInstances(processInstances.stream().toArray(CaseInstanceCustomVars[]::new));
        return result;
    }

    public static List<org.jbpm.services.api.query.model.QueryParam> convertToServiceApiQueryParam(List<QueryParam> param) {
        return param.stream().map(e -> new org.jbpm.services.api.query.model.QueryParam(e.getColumn(), e.getOperator(), e.getValue())).collect(toList());
    }
}

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

package org.kie.server.api.model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.ProcessNode;
import org.kie.server.api.model.admin.ProcessNodeList;
import org.kie.server.api.model.admin.TaskNotification;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignment;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.api.model.admin.TimerInstance;
import org.kie.server.api.model.admin.TimerInstanceList;
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
import org.kie.server.api.model.cases.CaseMilestoneDefinition;
import org.kie.server.api.model.cases.CaseMilestoneList;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.cases.CaseStageDefinition;
import org.kie.server.api.model.cases.CaseStageList;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNNodeStub;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.*;
import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.model.type.JaxbByte;
import org.kie.server.api.model.type.JaxbCharacter;
import org.kie.server.api.model.type.JaxbDouble;
import org.kie.server.api.model.type.JaxbFloat;
import org.kie.server.api.model.type.JaxbInteger;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.api.model.type.JaxbShort;
import org.kie.server.api.model.type.JaxbString;

@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceResponse<T> {
    public static enum ResponseType {
        SUCCESS, FAILURE, NO_RESPONSE;
    }

    @XmlAttribute
    private ServiceResponse.ResponseType type;
    @XmlAttribute
    private String                       msg;
    @XmlElements({
            // types model
            @XmlElement(name = "boolean-type", type = JaxbBoolean.class),
            @XmlElement(name = "byte-type", type = JaxbByte.class),
            @XmlElement(name = "char-type", type = JaxbCharacter.class),
            @XmlElement(name = "double-type", type = JaxbDouble.class),
            @XmlElement(name = "float-type", type = JaxbFloat.class),
            @XmlElement(name = "int-type", type = JaxbInteger.class),
            @XmlElement(name = "long-type", type = JaxbLong.class),
            @XmlElement(name = "short-type", type = JaxbShort.class),
            @XmlElement(name = "string-type", type = JaxbString.class),
            @XmlElement(name = "map-type", type = JaxbMap.class),
            @XmlElement(name = "list-type", type = JaxbList.class),
            @XmlElement(name = "array-list", type = ArrayList.class),

            //kie server model
            @XmlElement(name = "kie-server-info", type = KieServerInfo.class),
            @XmlElement(name = "kie-container", type = KieContainerResource.class),
            @XmlElement(name = "results", type = String.class),
            @XmlElement(name = "kie-containers", type = KieContainerResourceList.class),
            @XmlElement(name = "kie-scanner", type = KieScannerResource.class),
            @XmlElement(name = "release-id", type = ReleaseId.class),
            @XmlElement(name = "kie-server-state-info", type = KieServerStateInfo.class),
            // definition model
            @XmlElement(name = "process-associated-entities", type = AssociatedEntitiesDefinition.class),
            @XmlElement(name = "process-definition", type = ProcessDefinition.class),
            @XmlElement(name = "process-service-tasks", type = ServiceTasksDefinition.class),
            @XmlElement(name = "process-task-inputs", type = TaskInputsDefinition.class),
            @XmlElement(name = "process-task-outputs", type = TaskOutputsDefinition.class),
            @XmlElement(name = "user-task-definition", type = UserTaskDefinition.class),
            @XmlElement(name = "user-task-definitions", type = UserTaskDefinitionList.class),
            @XmlElement(name = "process-variables", type = VariablesDefinition.class),
            @XmlElement(name = "process-subprocesses", type = SubProcessesDefinition.class),

            @XmlElement(name = "process-definitions", type = ProcessDefinitionList.class),
            @XmlElement(name = "process-instance", type = ProcessInstance.class),
            @XmlElement(name = "process-instance-list", type = ProcessInstanceList.class),
            @XmlElement(name = "node-instance", type = NodeInstance.class),
            @XmlElement(name = "node-instance-list", type = NodeInstanceList.class),
            @XmlElement(name = "variable-instance", type = VariableInstance.class),
            @XmlElement(name = "variable-instance-list", type = VariableInstanceList.class),
            @XmlElement(name = "task-instance", type = TaskInstance.class),
            @XmlElement(name = "task-instance-list", type = TaskInstanceList.class),
            @XmlElement(name = "task-summary", type = TaskSummary.class),
            @XmlElement(name = "task-summary-list", type = TaskSummaryList.class),
            @XmlElement(name = "task-event-instance", type = TaskEventInstance.class),
            @XmlElement(name = "task-event-instance-list", type = TaskEventInstanceList.class),
            @XmlElement(name = "work-item-instance", type = WorkItemInstance.class),
            @XmlElement(name = "work-item-instance-list", type = WorkItemInstanceList.class),
            @XmlElement(name = "request-info-instance", type = RequestInfoInstance.class),
            @XmlElement(name = "request-info-instance-list", type = RequestInfoInstanceList.class),
            @XmlElement(name = "error-info-instance", type = ErrorInfoInstance.class),
            @XmlElement(name = "error-info-instance-list", type = ErrorInfoInstanceList.class),
            @XmlElement(name = "job-request-instance", type = JobRequestInstance.class),
            @XmlElement(name = "query-definition", type = QueryDefinition.class),
            @XmlElement(name = "query-definitions", type = QueryDefinitionList.class),
            @XmlElement(name = "document-instance", type = DocumentInstance.class),
            @XmlElement(name = "document-instance-list", type = DocumentInstanceList.class),

            // optaplanner entities
            @XmlElement(name = "solver-instance", type = SolverInstance.class),
            @XmlElement(name = "solver-instance-list", type = SolverInstanceList.class),

            @XmlElement(name = "execution-results", type = ExecutionResultImpl.class),

            // admin section
            @XmlElement(name = "migration-report-instance", type = MigrationReportInstance.class),
            @XmlElement(name = "migration-report-instance-list", type = MigrationReportInstanceList.class),
            @XmlElement(name = "email-notification", type = EmailNotification.class),
            @XmlElement(name = "process-node", type = ProcessNode.class),
            @XmlElement(name = "process-node-list", type = ProcessNodeList.class),
            @XmlElement(name = "timer-instance", type = TimerInstance.class),
            @XmlElement(name = "timer-instance-list", type = TimerInstanceList.class),
            @XmlElement(name = "org-entities", type = OrgEntities.class),
            @XmlElement(name = "task-notification", type = TaskNotification.class),
            @XmlElement(name = "task-notification-list", type = TaskNotificationList.class),
            @XmlElement(name = "task-reassignment", type = TaskReassignment.class),
            @XmlElement(name = "task-reassignment-list", type = TaskReassignmentList.class),

            // case management
            @XmlElement(name = "case-milestone", type = CaseMilestone.class),
            @XmlElement(name = "case-milestone-list", type = CaseMilestoneList.class),
            @XmlElement(name = "case-instance", type = CaseInstance.class),
            @XmlElement(name = "case-instance-list", type = CaseInstanceList.class),
            @XmlElement(name = "case-file", type = CaseFile.class),
            @XmlElement(name = "case-adhoc-fragment", type = CaseAdHocFragment.class),
            @XmlElement(name = "case-adhoc-fragment-list", type = CaseAdHocFragmentList.class),
            @XmlElement(name = "case-comment", type = CaseComment.class),
            @XmlElement(name = "case-comment-list", type = CaseCommentList.class),
            @XmlElement(name = "case-role-assignment", type = CaseRoleAssignment.class),
            @XmlElement(name = "case-role-assignment-list", type = CaseRoleAssignmentList.class),
            @XmlElement(name = "case-stage", type = CaseStage.class),
            @XmlElement(name = "case-stage-list", type = CaseStageList.class),
            @XmlElement(name = "case-definition", type = CaseDefinition.class),
            @XmlElement(name = "case-definition-list", type = CaseDefinitionList.class),
            @XmlElement(name = "case-stage-def", type = CaseStageDefinition.class),
            @XmlElement(name = "case-milestone-def", type = CaseMilestoneDefinition.class),

            // Kie DMN
            @XmlElement(name = "dmn-evaluation-context", type = DMNContextKS.class),
            @XmlElement(name = "dmn-evaluation-result" , type = DMNResultKS.class),
            @XmlElement(name = "dmn-node-stub" , type = DMNNodeStub.class)
            
            })
    private T                            result;

    public ServiceResponse() {
    }

    public ServiceResponse(ServiceResponse.ResponseType type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public ServiceResponse(ServiceResponse.ResponseType type, String msg, T result) {
        this.type = type;
        this.msg = msg;
        this.result = result;
    }

    public ServiceResponse.ResponseType getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public void setType(ServiceResponse.ResponseType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}

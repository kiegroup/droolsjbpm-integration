/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.marshalling.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.runtime.AdvanceSessionTimeCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.command.runtime.GetFactCountCommand;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.GetIdCommand;
import org.drools.core.command.runtime.GetSessionTimeCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.rule.AgendaGroupSetFocusCommand;
import org.drools.core.command.runtime.rule.ClearActivationGroupCommand;
import org.drools.core.command.runtime.rule.ClearAgendaCommand;
import org.drools.core.command.runtime.rule.ClearAgendaGroupCommand;
import org.drools.core.command.runtime.rule.ClearRuleFlowGroupCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetFactHandlesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.drools.core.runtime.rule.impl.FlatQueryResults;
import org.kie.api.pmml.DoubleFieldOutput;
import org.kie.api.pmml.IntegerFieldOutput;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.StringFieldOutput;
import org.kie.pmml.pmml_4_2.model.ScoreCard;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetReleaseIdCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.GetServerStateCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangeCommand;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangesCommand;
import org.kie.server.api.commands.optaplanner.CreateSolverCommand;
import org.kie.server.api.commands.optaplanner.DisposeSolverCommand;
import org.kie.server.api.commands.optaplanner.GetSolverCommand;
import org.kie.server.api.commands.optaplanner.GetSolverWithBestSolutionCommand;
import org.kie.server.api.commands.optaplanner.GetSolversCommand;
import org.kie.server.api.commands.optaplanner.IsEveryProblemFactChangeProcessedCommand;
import org.kie.server.api.commands.optaplanner.SolvePlanningProblemCommand;
import org.kie.server.api.commands.optaplanner.TerminateSolverEarlyCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ExecutionErrorInstanceList;
import org.kie.server.api.model.admin.MigrationProcessSpecification;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.admin.MigrationSpecification;
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
import org.kie.server.api.model.cases.CaseFileDataItem;
import org.kie.server.api.model.cases.CaseFileDataItemList;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceCustomVarsList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.api.model.cases.CaseMilestone;
import org.kie.server.api.model.cases.CaseMilestoneDefinition;
import org.kie.server.api.model.cases.CaseMilestoneList;
import org.kie.server.api.model.cases.CaseRoleAssignment;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.api.model.cases.CaseStage;
import org.kie.server.api.model.cases.CaseStageDefinition;
import org.kie.server.api.model.cases.CaseStageList;
import org.kie.server.api.model.cases.CaseUserTaskWithVariablesList;
import org.kie.server.api.model.definition.NodeDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.definition.ProcessStartSpec;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.model.definition.SearchQueryFilterSpec;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.definition.TimerDefinition;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNDecisionInfo;
import org.kie.server.api.model.dmn.DMNDecisionResultKS;
import org.kie.server.api.model.dmn.DMNInputDataInfo;
import org.kie.server.api.model.dmn.DMNItemDefinitionInfo;
import org.kie.server.api.model.dmn.DMNMessageKS;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNNodeStub;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.api.model.instance.ErrorInfoInstance;
import org.kie.server.api.model.instance.ErrorInfoInstanceList;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceCustomVars;
import org.kie.server.api.model.instance.ProcessInstanceCustomVarsList;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.ProcessInstanceUserTaskWithVariablesList;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.TaskWithProcessDescription;
import org.kie.server.api.model.instance.TaskWithProcessDescriptionList;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.api.model.taskassigning.OrganizationalEntity;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.type.JaxbByteArray;
import org.kie.server.api.model.type.JaxbDate;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.xml.bind.ValidationEvent.ERROR;
import static javax.xml.bind.ValidationEvent.FATAL_ERROR;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STRICT_JAXB_FORMAT;;

public class JaxbMarshaller implements Marshaller {

    private static final Logger logger = LoggerFactory.getLogger(JaxbMarshaller.class);

    public static final Class<?>[] KIE_SERVER_JAXB_CLASSES;

    static {
        KIE_SERVER_JAXB_CLASSES = new Class<?>[]{
                                                 CallContainerCommand.class,
                                                 CommandScript.class,
                                                 CreateContainerCommand.class,
                                                 DisposeContainerCommand.class,
                                                 ListContainersCommand.class,
                                                 GetContainerInfoCommand.class,
                                                 GetScannerInfoCommand.class,
                                                 GetServerInfoCommand.class,
                                                 UpdateScannerCommand.class,
                                                 GetReleaseIdCommand.class,
                                                 UpdateReleaseIdCommand.class,
                                                 DescriptorCommand.class,
                                                 GetServerStateCommand.class,

                                                 KieContainerResource.class,
                                                 KieContainerResourceList.class,
                                                 KieContainerStatus.class,
                                                 KieServerInfo.class,
                                                 ReleaseId.class,
                                                 ServiceResponse.class,
                                                 ServiceResponsesList.class,
                                                 KieServerStateInfo.class,

                                                 ReleaseIdFilter.class,
                                                 KieContainerStatusFilter.class,
                                                 KieContainerResourceFilter.class,

                                                 BatchExecutionCommandImpl.class,
                                                 ExecutionResultImpl.class,
                                                 DefaultFactHandle.class,
                                                 DeleteCommand.class,
                                                 GetVariableCommand.class,
                                                 GetFactCountCommand.class,
                                                 SetGlobalCommand.class,
                                                 UpdateCommand.class,
                                                 ClearAgendaCommand.class,
                                                 FireAllRulesCommand.class,
                                                 DisposeCommand.class,
                                                 GetIdCommand.class,
                                                 GetGlobalCommand.class,
                                                 InsertObjectCommand.class,
                                                 ClearAgendaGroupCommand.class,
                                                 FlatQueryResults.class,
                                                 AgendaGroupSetFocusCommand.class,
                                                 ClearRuleFlowGroupCommand.class,
                                                 ClearActivationGroupCommand.class,
                                                 GetFactHandlesCommand.class,
                                                 GetSessionTimeCommand.class,
                                                 AdvanceSessionTimeCommand.class,

                                                 KieServerConfig.class,
                                                 KieServerConfigItem.class,

                                                 JaxbList.class,
                                                 JaxbMap.class,
                                                 JaxbDate.class,
                                                 JaxbByteArray.class,

                                                 JaxbByteArray.class,

                                                 ProcessDefinition.class,
                                                 ProcessDefinitionList.class,

                                                 NodeDefinition.class,
                                                 TimerDefinition.class,

                                                 ProcessInstance.class,
                                                 ProcessInstanceList.class,
                                                 ProcessInstanceCustomVars.class,
                                                 ProcessInstanceCustomVarsList.class,

                                                 NodeInstance.class,
                                                 NodeInstanceList.class,

                                                 VariableInstance.class,
                                                 VariableInstanceList.class,

                                                 TaskInstance.class,
                                                 TaskInstanceList.class,
                                                 TaskSummary.class,
                                                 TaskSummaryList.class,
                                                 TaskWithProcessDescription.class,
                                                 TaskWithProcessDescriptionList.class,

                                                 TaskEventInstance.class,
                                                 TaskEventInstanceList.class,

                                                 TaskComment.class,
                                                 TaskCommentList.class,
                                                 TaskAttachment.class,
                                                 TaskAttachmentList.class,

                                                 WorkItemImpl.class,
                                                 WorkItemInstance.class,
                                                 WorkItemInstanceList.class,

                                                 RequestInfoInstance.class,
                                                 RequestInfoInstanceList.class,
                                                 ErrorInfoInstance.class,
                                                 ErrorInfoInstanceList.class,
                                                 JobRequestInstance.class,

                                                 DocumentInstance.class,
                                                 DocumentInstanceList.class,

                                                 QueryDefinition.class,
                                                 QueryDefinitionList.class,
                                                 QueryFilterSpec.class,
                                                 QueryParam.class,

                                                 ProcessInstanceQueryFilterSpec.class,
                                                 TaskQueryFilterSpec.class,

                                                 ArrayList.class,

                                                 // OptaPlanner
                                                 SolverInstance.class,
                                                 SolverInstanceList.class,
                                                 Message.class,
                                                 ScoreWrapper.class,

                                                 // Optaplanner commands
                                                 CreateSolverCommand.class,
                                                 DisposeSolverCommand.class,
                                                 GetSolverWithBestSolutionCommand.class,
                                                 GetSolversCommand.class,
                                                 GetSolverCommand.class,
                                                 SolvePlanningProblemCommand.class,
                                                 TerminateSolverEarlyCommand.class,
                                                 AddProblemFactChangeCommand.class,
                                                 AddProblemFactChangesCommand.class,
                                                 IsEveryProblemFactChangeProcessedCommand.class,

                                                 // admin section
                                                 MigrationReportInstance.class,
                                                 MigrationReportInstanceList.class,
                                                 ProcessNode.class,
                                                 ProcessNodeList.class,
                                                 TimerInstance.class,
                                                 TimerInstanceList.class,
                                                 EmailNotification.class,
                                                 OrgEntities.class,
                                                 TaskNotification.class,
                                                 TaskNotificationList.class,
                                                 TaskReassignment.class,
                                                 TaskReassignmentList.class,
                                                 ExecutionErrorInstance.class,
                                                 ExecutionErrorInstanceList.class,
                                                 MigrationSpecification.class,
                                                 MigrationProcessSpecification.class,

                                                 // case management
                                                 CaseMilestone.class,
                                                 CaseMilestoneList.class,
                                                 CaseInstance.class,
                                                 CaseInstanceList.class,
                                                 CaseFile.class,
                                                 CaseStage.class,
                                                 CaseStageList.class,
                                                 CaseAdHocFragment.class,
                                                 CaseAdHocFragmentList.class,
                                                 CaseComment.class,
                                                 CaseCommentList.class,
                                                 CaseRoleAssignment.class,
                                                 CaseRoleAssignmentList.class,
                                                 CaseDefinition.class,
                                                 CaseDefinitionList.class,
                                                 CaseMilestoneDefinition.class,
                                                 CaseStageDefinition.class,
                                                 CaseFileDataItem.class,
                                                 CaseFileDataItemList.class,
                                                 CaseMigrationReportInstance.class,

                                                 // Kie DMN
                                                 DMNContextKS.class,
                                                 DMNResultKS.class,
                                                 DMNNodeStub.class,
                                                 DMNMessageKS.class,
                                                 DMNDecisionResultKS.class,
                                                 DMNModelInfoList.class,
                                                 DMNModelInfo.class,
                                                 DMNDecisionInfo.class,
                                                 DMNInputDataInfo.class,
                                                 DMNItemDefinitionInfo.class,

                                                 // Kie PMML
                                                 PMML4Result.class,
                                                 IntegerFieldOutput.class,
                                                 DoubleFieldOutput.class,
                                                 StringFieldOutput.class,
                                                 ScoreCard.class,

                                                 //TaskAssigning
                                                 PlanningExecutionResult.class,
                                                 OrganizationalEntity.class,
                                                 PlanningItem.class,
                                                 PlanningItemList.class,
                                                 PlanningTask.class,
                                                 TaskDataList.class,
                                                 TaskData.class,
                                                 SearchQueryFilterSpec.class,
                                                 ProcessInstanceUserTaskWithVariablesList.class,
                                                 CaseUserTaskWithVariablesList.class,
                                                 CaseInstanceCustomVarsList.class,
                                                 ProcessStartSpec.class
        };
    }

    protected JAXBContext jaxbContext;

    protected ClassLoader classLoader;

    public JaxbMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        this.classLoader = classLoader;

        buildMarshaller(classes, classLoader);
    }

    protected void buildMarshaller(Set<Class<?>> classes, final ClassLoader classLoader) {

        try {
            logger.debug("Additional classes for JAXB context are {}", classes);
            Set<Class<?>> allClasses = new HashSet<Class<?>>();

            allClasses.addAll(Arrays.asList(KIE_SERVER_JAXB_CLASSES));
            if (classes != null) {
                allClasses.addAll(classes);
            }
            logger.debug("All classes for JAXB context are {}", allClasses);
            this.jaxbContext = JAXBContext.newInstance(allClasses.toArray(new Class[allClasses.size()]));
        } catch (JAXBException e) {
            logger.error("Error while creating JAXB Marshaller due to {}", e.getMessage(), e);
            throw new MarshallingException("Error while creating JAXB context from default classes! " + e.getMessage(), e);
        }
    }

    protected void configureMarshaller(Set<Class<?>> classes, final ClassLoader classLoader) {
        // by default nothing to configure though it might be needed in case of extensions
    }

    @Override
    public String marshall(Object input) {
        if (input == null) {
            return null;
        }

        StringWriter writer = new StringWriter();
        try {
            getMarshaller().marshal(ModelWrapper.wrap(input), writer);
        } catch (JAXBException e) {
            throw new MarshallingException("Can't marshall input object: " + input, e);
        }
        return writer.toString();
    }

    @Override
    public <T> T unmarshall(String input, Class<T> type) {
        try {
            Unmarshaller unmarshaller = getUnmarshaller();
            ValidationEventCollector vec = new ValidationEventCollector();
            boolean strict = Boolean.getBoolean(KIE_SERVER_STRICT_JAXB_FORMAT);
            unmarshaller.setEventHandler(vec);
            Object result = unmarshaller.unmarshal(new StringReader(input));
            if (strict || logger.isWarnEnabled()) {
                String errorMessage = Arrays.stream(vec.getEvents())
                        .filter(ve -> ve.getSeverity() == ERROR || ve.getSeverity() == FATAL_ERROR)
                        .map(ValidationEvent::getMessage)
                        .collect(Collectors.joining("\n"));
                if (!errorMessage.isEmpty()) {
                    logger.warn(errorMessage);
                    if (strict) {
                        throw new MarshallingException(errorMessage);
                    }
                }
            }
            return (T) unwrap(result);
        } catch (JAXBException e) {
            throw new MarshallingException("Can't unmarshall input string: " + input, e);
        }
    }

    protected Object unwrap(Object data) {
        if (data instanceof Wrapped) {
            return ((Wrapped) data).unwrap();
        }
        return data;
    }

    @Override
    public void dispose() {
        //nothing to do 
    }

    @Override
    public MarshallingFormat getFormat() {
        return MarshallingFormat.JAXB;
    }

    protected javax.xml.bind.Marshaller getMarshaller() throws JAXBException {
        javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    protected Unmarshaller getUnmarshaller() throws JAXBException {
        return jaxbContext.createUnmarshaller();
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}

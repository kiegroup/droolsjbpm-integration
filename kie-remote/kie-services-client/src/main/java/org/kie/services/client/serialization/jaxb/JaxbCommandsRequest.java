package org.kie.services.client.serialization.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.drools.core.command.runtime.*;
import org.drools.core.command.runtime.process.*;
import org.drools.core.command.runtime.rule.*;
import org.jbpm.services.task.commands.*;
import org.kie.api.command.Command;

@XmlRootElement(name = "command-request")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbCommandsRequest {

    @XmlElement(name = "deployment-id")
    @XmlSchemaType(name = "string")
    private String deploymentId;

    @XmlElement(name = "process-instance-id")
    @XmlSchemaType(name = "long")
    private Long processInstanceId;

    @XmlElement(name = "ver")
    @XmlSchemaType(name = "int")
    private Integer version;

    @XmlElements({
            @XmlElement(name = "abort-process-instance", type = AbortProcessInstanceCommand.class),
            @XmlElement(name = "abort-work-item", type = AbortWorkItemCommand.class),
            @XmlElement(name = "complete-work-item", type = CompleteWorkItemCommand.class),
            // @XmlElement(name = "create-correlated-process-instance", type = CreateCorrelatedProcessInstanceCommand.class),
            @XmlElement(name = "create-process-instance", type = CreateProcessInstanceCommand.class),
            @XmlElement(name = "get-process-event-listeners", type = GetProcessEventListenersCommand.class),
            @XmlElement(name = "get-process-instance", type = GetProcessInstanceCommand.class),
            // @XmlElement(name = "get-process-instance-by-correlation-key", type =
            // GetProcessInstanceByCorrelationKeyCommand.class),
            @XmlElement(name = "get-process-instances", type = GetProcessInstancesCommand.class),
            @XmlElement(name = "get-workitem", type = GetWorkItemCommand.class),
            @XmlElement(name = "register-workitem-handler", type = RegisterWorkItemHandlerCommand.class),
            @XmlElement(name = "signal-event", type = SignalEventCommand.class),
            // @XmlElement(name = "start-correlated-process", type = StartCorrelatedProcessCommand.class),
            @XmlElement(name = "start-process", type = StartProcessCommand.class),
            @XmlElement(name = "start-process-instance", type = StartProcessInstanceCommand.class),

            @XmlElement(name = "agenda-group-set-focus", type = AgendaGroupSetFocusCommand.class),
            @XmlElement(name = "clear-activation-group", type = ClearActivationGroupCommand.class),
            @XmlElement(name = "clear-agenda", type = ClearAgendaCommand.class),
            @XmlElement(name = "clear-agenda-group", type = ClearAgendaGroupCommand.class),
            @XmlElement(name = "clear-ruleflow-group", type = ClearRuleFlowGroupCommand.class),
            @XmlElement(name = "delete", type = DeleteCommand.class),
            @XmlElement(name = "fire-all-rules", type = FireAllRulesCommand.class),
            @XmlElement(name = "fire-until-halt", type = FireUntilHaltCommand.class),
            @XmlElement(name = "from-external-fact-handle", type = FromExternalFactHandleCommand.class),
            @XmlElement(name = "get-agenda-event-listeners", type = GetAgendaEventListenersCommand.class),
            @XmlElement(name = "get-entry-point", type = GetEntryPointCommand.class),
            @XmlElement(name = "get-entry-points", type = GetEntryPointsCommand.class),
            @XmlElement(name = "get-fact-handle", type = GetFactHandleCommand.class),
            @XmlElement(name = "get-fact-handles", type = GetFactHandlesCommand.class),
            @XmlElement(name = "get-object", type = GetObjectCommand.class),
            @XmlElement(name = "get-objects", type = GetObjectsCommand.class),
            @XmlElement(name = "get-working-memory-event-listeners", type = GetWorkingMemoryEventListenersCommand.class),
            @XmlElement(name = "halt", type = HaltCommand.class),
            @XmlElement(name = "insert-elements", type = InsertElementsCommand.class),
            @XmlElement(name = "insert-object", type = InsertObjectCommand.class),
            @XmlElement(name = "insert-object-in-entry-point", type = InsertObjectInEntryPointCommand.class),
            @XmlElement(name = "modify", type = ModifyCommand.class),
            @XmlElement(name = "query", type = QueryCommand.class),
            @XmlElement(name = "update", type = UpdateCommand.class),

            @XmlElement(name = "destroy-session", type = DestroySessionCommand.class),
            @XmlElement(name = "dispose", type = DisposeCommand.class),
            @XmlElement(name = "get-calendars", type = GetCalendarsCommand.class),
            @XmlElement(name = "get-channels", type = GetChannelsCommand.class),
            @XmlElement(name = "get-environment", type = GetEnvironmentCommand.class),
            @XmlElement(name = "get-fact-count", type = GetFactCountCommand.class),
            @XmlElement(name = "get-global", type = GetGlobalCommand.class),
            @XmlElement(name = "get-globals", type = GetGlobalsCommand.class),
            @XmlElement(name = "get-id", type = GetIdCommand.class),
            @XmlElement(name = "get-knowledge-base", type = GetKnowledgeBaseCommand.class),
            @XmlElement(name = "kbuilder-set-property", type = KBuilderSetPropertyCommand.class),
            @XmlElement(name = "register-channel", type = RegisterChannelCommand.class),
            @XmlElement(name = "remove-event-listener", type = RemoveEventListenerCommand.class),
            @XmlElement(name = "set-global", type = SetGlobalCommand.class),
            @XmlElement(name = "unregister-channel", type = UnregisterChannelCommand.class),

            @XmlElement(name = "activate-task", type = ActivateTaskCommand.class),
            @XmlElement(name = "add-task", type = AddTaskCommand.class),
            @XmlElement(name = "claim-next-available-task", type = ClaimNextAvailableTaskCommand.class),
            @XmlElement(name = "claim-task", type = ClaimTaskCommand.class),
            @XmlElement(name = "complete-task", type = CompleteTaskCommand.class),
            @XmlElement(name = "delegate-task", type = DelegateTaskCommand.class),
            @XmlElement(name = "exit-task", type = ExitTaskCommand.class),
            @XmlElement(name = "fail-task", type = FailTaskCommand.class),
            @XmlElement(name = "forward-task", type = ForwardTaskCommand.class),
            @XmlElement(name = "get-attachment", type = GetAttachmentCommand.class),
            @XmlElement(name = "get-content", type = GetContentCommand.class),
            @XmlElement(name = "get-task-as-business-admin", type = GetTaskAssignedAsBusinessAdminCommand.class),
            @XmlElement(name = "get-task-as-potential-owner", type = GetTaskAssignedAsPotentialOwnerCommand.class),
            @XmlElement(name = "get-task-by-workitemid", type = GetTaskByWorkItemIdCommand.class),
            @XmlElement(name = "get-task", type = GetTaskCommand.class),
            @XmlElement(name = "get-tasks-by-processinstanceid", type = GetTasksByProcessInstanceIdCommand.class),
            @XmlElement(name = "get-tasks-by-status-by-processinstanceid", type = GetTasksByStatusByProcessInstanceIdCommand.class),
            @XmlElement(name = "get-tasks-owned", type = GetTasksOwnedCommand.class),
            @XmlElement(name = "nominate-task", type = NominateTaskCommand.class),
            @XmlElement(name = "release-task", type = ReleaseTaskCommand.class),
            @XmlElement(name = "resume-task", type = ResumeTaskCommand.class),
            @XmlElement(name = "skip-task", type = SkipTaskCommand.class),
            @XmlElement(name = "start-task", type = StartTaskCommand.class),
            @XmlElement(name = "stop-task", type = StopTaskCommand.class),
            @XmlElement(name = "suspend-task", type = SuspendTaskCommand.class) })
    protected List<Command<?>> commands;

    public JaxbCommandsRequest() {
        this.version = 1;
        // Default constructor
    }

    public JaxbCommandsRequest(String deploymentId, Command<?> command) {
        super();
        this.deploymentId = deploymentId;
        this.commands = new ArrayList<Command<?>>();
        this.commands.add(command);
    }

    public JaxbCommandsRequest(String deploymentId, List<Command<?>> commands) {
        super();
        this.deploymentId = deploymentId;
        this.commands = commands;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setCommands(List<Command<?>> commands) {
        this.commands = commands;
    }

    public List<Command<?>> getCommands() {
        return this.commands;
    }

}

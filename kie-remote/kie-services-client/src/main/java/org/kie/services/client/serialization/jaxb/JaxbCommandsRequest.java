package org.kie.services.client.serialization.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.runtime.*;
import org.drools.core.command.runtime.process.*;
import org.drools.core.command.runtime.rule.*;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindActiveProcessInstancesCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
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
    private Integer version = 1;

    // This list should match the list in AcceptedCommands
    @XmlElements({
            @XmlElement(name = "complete-work-item", type = CompleteWorkItemCommand.class),
            @XmlElement(name = "abort-work-item", type = AbortWorkItemCommand.class),
            @XmlElement(name = "get-workitem", type = GetWorkItemCommand.class),
            
            @XmlElement(name = "abort-process-instance", type = AbortProcessInstanceCommand.class),
            @XmlElement(name = "get-process-ids", type = GetProcessIdsCommand.class),
            @XmlElement(name = "get-process-instance-by-correlation-key", type = GetProcessInstanceByCorrelationKeyCommand.class),
            @XmlElement(name = "get-process-instance", type = GetProcessInstanceCommand.class),
            @XmlElement(name = "get-process-instances", type = GetProcessInstancesCommand.class),
            @XmlElement(name = "set-process-instance-vars", type = SetProcessInstanceVariablesCommand.class),
            @XmlElement(name = "signal-event", type = SignalEventCommand.class),
            @XmlElement(name = "start-correlated-process", type = StartCorrelatedProcessCommand.class),
            @XmlElement(name = "start-process", type = StartProcessCommand.class),
            
            @XmlElement(name = "get-variable", type = GetVariableCommand.class),
            @XmlElement(name = "get-fact-count", type = GetFactCountCommand.class),
            @XmlElement(name = "get-global", type = GetGlobalCommand.class),
            @XmlElement(name = "get-id", type = GetIdCommand.class),
            @XmlElement(name = "set-global", type = SetGlobalCommand.class),
            
            @XmlElement(name = "delete", type = DeleteCommand.class),
            @XmlElement(name = "fire-all-rules", type = FireAllRulesCommand.class),
            @XmlElement(name = "insert-object", type = InsertObjectCommand.class),
            @XmlElement(name = "update", type = UpdateCommand.class),
           
            // task
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
            @XmlElement(name = "suspend-task", type = SuspendTaskCommand.class),
            
            // audit
            @XmlElement(name = "clear-history-logs", type = ClearHistoryLogsCommand.class),
            @XmlElement(name = "find-active-process-instances", type = FindActiveProcessInstancesCommand.class),
            @XmlElement(name = "find-node-instances", type = FindNodeInstancesCommand.class),
            @XmlElement(name = "find-process-instance", type = FindProcessInstanceCommand.class),
            @XmlElement(name = "find-process-instances", type = FindProcessInstancesCommand.class),
            @XmlElement(name = "find-subprocess-instances", type = FindSubProcessInstancesCommand.class),
            @XmlElement(name = "find-variable-instances", type = FindVariableInstancesCommand.class)
    })
    protected List<Command<?>> commands;

    public JaxbCommandsRequest() {
        // Default constructor
    }

    public JaxbCommandsRequest(Command<?> command) {
        this.commands = new ArrayList<Command<?>>();
        this.commands.add(command);
        checkThatCommandsAreTaskCommands(commands);
    }
    
    public JaxbCommandsRequest(List<Command<?>> commands) {
        this.commands = new ArrayList<Command<?>>(); 
        this.commands.addAll(commands);
        checkThatCommandsAreTaskCommands(commands);
    }

    private void checkThatCommandsAreTaskCommands(List<Command<?>> commands) {
        for( Command<?> command : commands ) { 
           if( ! (command instanceof TaskCommand<?>) ) { 
               throw new UnsupportedOperationException( "Only commands for the task service are supported when leaving out the deployment id (" + command.getClass().getSimpleName()  + ")" );
           }
        }
    }
    
    public JaxbCommandsRequest(String deploymentId, Command<?> command) {
        this.deploymentId = deploymentId;
        this.commands = new ArrayList<Command<?>>();
        this.commands.add(command);
    }

    public JaxbCommandsRequest(String deploymentId, List<Command<?>> commands) {
        this.deploymentId = deploymentId;
        this.commands = new ArrayList<Command<?>>();
        this.commands.addAll(commands);
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
    
    public String toString() {
    	String result = "JaxbCommandsRequest " + deploymentId + "\n";
    	if (commands != null) {
	    	for (Command command: commands) {
	    		result += command + "\n";
	    	}
    	}
    	return result;
    }

}

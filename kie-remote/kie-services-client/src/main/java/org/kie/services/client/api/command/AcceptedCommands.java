package org.kie.services.client.api.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.runtime.GetFactCountCommand;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.GetIdCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.*;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindActiveProcessInstancesCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesByNameCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.services.task.commands.*;
import org.kie.api.command.Command;

public class AcceptedCommands {

    private AcceptedCommands() { 
        // static fields only
    }
    
    private static Set<Class<? extends Command>> acceptedCommands = new HashSet<Class<? extends Command>>();
    static {
        acceptedCommands.add(AbortWorkItemCommand.class);
        acceptedCommands.add(CompleteWorkItemCommand.class);
        acceptedCommands.add(GetWorkItemCommand.class);

        acceptedCommands.add(AbortProcessInstanceCommand.class);
        acceptedCommands.add(GetProcessIdsCommand.class);
        acceptedCommands.add(GetProcessInstanceByCorrelationKeyCommand.class);
        acceptedCommands.add(GetProcessInstanceCommand.class);
        acceptedCommands.add(GetProcessInstancesCommand.class);
        acceptedCommands.add(SetProcessInstanceVariablesCommand.class);
        acceptedCommands.add(SignalEventCommand.class);
        acceptedCommands.add(StartCorrelatedProcessCommand.class);
        acceptedCommands.add(StartProcessCommand.class);

        acceptedCommands.add(GetVariableCommand.class);
        acceptedCommands.add(GetFactCountCommand.class);
        acceptedCommands.add(GetGlobalCommand.class);
        acceptedCommands.add(GetIdCommand.class);
        acceptedCommands.add(SetGlobalCommand.class);

        acceptedCommands.add(DeleteCommand.class);
        acceptedCommands.add(FireAllRulesCommand.class);
        acceptedCommands.add(InsertObjectCommand.class);
        acceptedCommands.add(UpdateCommand.class);

        // Task commands
        acceptedCommands.add(ActivateTaskCommand.class);
        acceptedCommands.add(AddTaskCommand.class);
        acceptedCommands.add(ClaimNextAvailableTaskCommand.class);
        acceptedCommands.add(ClaimTaskCommand.class);
        acceptedCommands.add(CompleteTaskCommand.class);
        acceptedCommands.add(DelegateTaskCommand.class);
        acceptedCommands.add(ExitTaskCommand.class);
        acceptedCommands.add(FailTaskCommand.class);
        acceptedCommands.add(ForwardTaskCommand.class);
        acceptedCommands.add(GetAttachmentCommand.class);
        acceptedCommands.add(GetContentCommand.class);
        acceptedCommands.add(GetTaskAssignedAsBusinessAdminCommand.class);
        acceptedCommands.add(GetTaskAssignedAsPotentialOwnerCommand.class);
        acceptedCommands.add(GetTaskByWorkItemIdCommand.class);
        acceptedCommands.add(GetTaskCommand.class);
        acceptedCommands.add(GetTasksByProcessInstanceIdCommand.class);
        acceptedCommands.add(GetTasksByStatusByProcessInstanceIdCommand.class);
        acceptedCommands.add(GetTasksOwnedCommand.class);
        acceptedCommands.add(NominateTaskCommand.class);
        acceptedCommands.add(ReleaseTaskCommand.class);
        acceptedCommands.add(ResumeTaskCommand.class);
        acceptedCommands.add(SkipTaskCommand.class);
        acceptedCommands.add(StartTaskCommand.class);
        acceptedCommands.add(StopTaskCommand.class);
        acceptedCommands.add(SuspendTaskCommand.class);
        acceptedCommands.add(CompositeCommand.class);
        acceptedCommands.add(ProcessSubTaskCommand.class);
        acceptedCommands.add(ExecuteTaskRulesCommand.class);
        acceptedCommands.add(CancelDeadlineCommand.class);

        // audit commands
        acceptedCommands.add(ClearHistoryLogsCommand.class);
        acceptedCommands.add(FindActiveProcessInstancesCommand.class);
        acceptedCommands.add(FindNodeInstancesCommand.class);
        acceptedCommands.add(FindProcessInstanceCommand.class);
        acceptedCommands.add(FindProcessInstancesCommand.class);
        acceptedCommands.add(FindSubProcessInstancesCommand.class);
        acceptedCommands.add(FindSubProcessInstancesCommand.class);
        acceptedCommands.add(FindVariableInstancesCommand.class);
        acceptedCommands.add(FindVariableInstancesByNameCommand.class);
        
        acceptedCommands = Collections.unmodifiableSet(acceptedCommands);
    }

    public static Set<Class<? extends Command>> getSet() {
        return acceptedCommands;
    }
    
    public static Set<Class<? extends TaskCommand<?>>> TASK_COMMANDS_THAT_INFLUENCE_KIESESSION = new HashSet<Class<? extends TaskCommand<?>>>();
    static { 
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(CompleteTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(ExitTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(FailTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(SkipTaskCommand.class);
        
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION = Collections.unmodifiableSet(TASK_COMMANDS_THAT_INFLUENCE_KIESESSION);
    }
    
    static Set<Class<? extends Command>> SEND_OBJECT_PARAMETER_COMMANDS = new HashSet<Class<? extends Command>>();
    static { 
        SEND_OBJECT_PARAMETER_COMMANDS.add(CompleteWorkItemCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(SignalEventCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(StartCorrelatedProcessCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(StartProcessCommand.class);
        
        SEND_OBJECT_PARAMETER_COMMANDS.add(SetGlobalCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(InsertObjectCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(UpdateCommand.class);

        SEND_OBJECT_PARAMETER_COMMANDS.add(AddTaskCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(CompleteTaskCommand.class);
        SEND_OBJECT_PARAMETER_COMMANDS.add(FailTaskCommand.class);
        
        SEND_OBJECT_PARAMETER_COMMANDS = Collections.unmodifiableSet(SEND_OBJECT_PARAMETER_COMMANDS);
    }

    static Set<Class<? extends Command>> RECV_OBJECT_PARAMETER_COMMANDS = new HashSet<Class<? extends Command>>();
    static { 
        RECV_OBJECT_PARAMETER_COMMANDS.add(GetVariableCommand.class);
        RECV_OBJECT_PARAMETER_COMMANDS.add(GetGlobalCommand.class);
        
        RECV_OBJECT_PARAMETER_COMMANDS = Collections.unmodifiableSet(SEND_OBJECT_PARAMETER_COMMANDS);
    }
}

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
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.services.task.commands.*;

public class AcceptedCommands {

    private static Set<Class> acceptedCommands = new HashSet<Class>();
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

        // audit commands
        acceptedCommands.add(ClearHistoryLogsCommand.class);
        acceptedCommands.add(FindActiveProcessInstancesCommand.class);
        acceptedCommands.add(FindNodeInstancesCommand.class);
        acceptedCommands.add(FindProcessInstanceCommand.class);
        acceptedCommands.add(FindProcessInstancesCommand.class);
        acceptedCommands.add(FindSubProcessInstancesCommand.class);
        acceptedCommands.add(FindVariableInstancesCommand.class);
        
        acceptedCommands = Collections.unmodifiableSet(acceptedCommands);
    }

    public static Set<Class> getSet() {
        return acceptedCommands;
    }
}

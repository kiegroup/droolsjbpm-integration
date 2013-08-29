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


public class AcceptedCommands {

    private static Set<String> acceptedCommands = new HashSet<String>();
    static {

        acceptedCommands.add(AbortWorkItemCommand.class.getName());
        acceptedCommands.add(CompleteWorkItemCommand.class.getName());
        acceptedCommands.add(GetWorkItemCommand.class.getName());
        acceptedCommands.add(RegisterWorkItemHandlerCommand.class.getName());

        acceptedCommands.add(AbortProcessInstanceCommand.class.getName());
        acceptedCommands.add(GetProcessIdsCommand.class.getName());
        acceptedCommands.add(GetProcessInstanceByCorrelationKeyCommand.class.getName());
        acceptedCommands.add(GetProcessInstanceCommand.class.getName());
        acceptedCommands.add(GetProcessInstancesCommand.class.getName());
        acceptedCommands.add(SetProcessInstanceVariablesCommand.class.getName());
        acceptedCommands.add(SignalEventCommand.class.getName());
        acceptedCommands.add(StartCorrelatedProcessCommand.class.getName());
        acceptedCommands.add(StartProcessCommand.class.getName());

        acceptedCommands.add(GetVariableCommand.class.getName());
        acceptedCommands.add(GetFactCountCommand.class.getName());
        acceptedCommands.add(GetGlobalCommand.class.getName());
        acceptedCommands.add(GetIdCommand.class.getName());
        acceptedCommands.add(SetGlobalCommand.class.getName());
        
        acceptedCommands.add(DeleteCommand.class.getName());
        acceptedCommands.add(FireAllRulesCommand.class.getName());
        acceptedCommands.add(InsertObjectCommand.class.getName());
        acceptedCommands.add(UpdateCommand.class.getName());
        
        acceptedCommands = Collections.unmodifiableSet(acceptedCommands);
    }
    
    public static Set<String> getSet() { 
       return acceptedCommands; 
    }
}

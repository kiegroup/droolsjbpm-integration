package org.kie.remote.client.jaxb;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class AcceptedClientCommands {

    private AcceptedClientCommands() { 
        // static fields only
    }
    
    private static Set<String> acceptedCommands = new HashSet<String>();
    static {
        acceptedCommands.add("AbortWorkItemCommand");
        acceptedCommands.add("CompleteWorkItemCommand");
        acceptedCommands.add("GetWorkItemCommand");

        acceptedCommands.add("AbortProcessInstanceCommand");
        acceptedCommands.add("GetProcessIdsCommand");
        acceptedCommands.add("GetProcessInstanceByCorrelationKeyCommand");
        acceptedCommands.add("GetProcessInstanceCommand");
        acceptedCommands.add("GetProcessInstancesCommand");
        acceptedCommands.add("SetProcessInstanceVariablesCommand");
        acceptedCommands.add("SignalEventCommand");
        acceptedCommands.add("StartCorrelatedProcessCommand");
        acceptedCommands.add("StartProcessCommand");

        acceptedCommands.add("GetVariableCommand");
        acceptedCommands.add("GetFactCountCommand");
        acceptedCommands.add("GetGlobalCommand");
        acceptedCommands.add("GetIdCommand");
        acceptedCommands.add("SetGlobalCommand");

        acceptedCommands.add("DeleteCommand");
        acceptedCommands.add("FireAllRulesCommand");
        acceptedCommands.add("InsertObjectCommand");
        acceptedCommands.add("UpdateCommand");

        // Task commands
        acceptedCommands.add("ActivateTaskCommand");
        acceptedCommands.add("AddTaskCommand");
        acceptedCommands.add("ClaimNextAvailableTaskCommand");
        acceptedCommands.add("ClaimTaskCommand");
        acceptedCommands.add("CompleteTaskCommand");
        acceptedCommands.add("DelegateTaskCommand");
        acceptedCommands.add("ExitTaskCommand");
        acceptedCommands.add("FailTaskCommand");
        acceptedCommands.add("ForwardTaskCommand");
        acceptedCommands.add("GetAttachmentCommand");

        acceptedCommands.add("GetContentCommand");
        acceptedCommands.add("GetTaskContentCommand");

        acceptedCommands.add("GetTaskAssignedAsBusinessAdminCommand");
        acceptedCommands.add("GetTaskAssignedAsPotentialOwnerCommand");
        acceptedCommands.add("GetTaskByWorkItemIdCommand");
        acceptedCommands.add("GetTaskCommand");
        acceptedCommands.add("GetTasksByProcessInstanceIdCommand");
        acceptedCommands.add("GetTasksByStatusByProcessInstanceIdCommand");
        acceptedCommands.add("GetTasksByVariousFieldsCommand");
        acceptedCommands.add("GetTasksOwnedCommand");
        acceptedCommands.add("NominateTaskCommand");
        acceptedCommands.add("ReleaseTaskCommand");
        acceptedCommands.add("ResumeTaskCommand");
        acceptedCommands.add("SkipTaskCommand");
        acceptedCommands.add("StartTaskCommand");
        acceptedCommands.add("StopTaskCommand");
        acceptedCommands.add("SuspendTaskCommand");
        acceptedCommands.add("CompositeCommand");
        acceptedCommands.add("ProcessSubTaskCommand");
        acceptedCommands.add("ExecuteTaskRulesCommand");
        acceptedCommands.add("CancelDeadlineCommand");

        // audit commands
        acceptedCommands.add("ClearHistoryLogsCommand");
        acceptedCommands.add("FindActiveProcessInstancesCommand");
        acceptedCommands.add("FindNodeInstancesCommand");
        acceptedCommands.add("FindProcessInstanceCommand");
        acceptedCommands.add("FindProcessInstancesCommand");
        acceptedCommands.add("FindSubProcessInstancesCommand");
        acceptedCommands.add("FindSubProcessInstancesCommand");
        acceptedCommands.add("FindVariableInstancesCommand");
        acceptedCommands.add("FindVariableInstancesByNameCommand");
        
        acceptedCommands = Collections.unmodifiableSet(acceptedCommands);
    }

    public static Set<String> getAcceptedClassNameSet() { 
        return acceptedCommands;
    }
    
    public static boolean isAcceptedCommandClass(Class<?> commandClass) { 
        return acceptedCommands.contains(commandClass.getSimpleName());
    }
    
    private static Set<String> taskCommandClassNamesThatInfluenceKieSession = new HashSet<String>();
    static { 
        taskCommandClassNamesThatInfluenceKieSession.add("CompleteTaskCommand");
        taskCommandClassNamesThatInfluenceKieSession.add("ExitTaskCommand");
        taskCommandClassNamesThatInfluenceKieSession.add("FailTaskCommand");
        taskCommandClassNamesThatInfluenceKieSession.add("SkipTaskCommand");
        
        taskCommandClassNamesThatInfluenceKieSession = Collections.unmodifiableSet(taskCommandClassNamesThatInfluenceKieSession);
    }
    
    public static boolean isTaskCommandClassThatInfluencesKieSession(Class<?> commandClass) { 
        return taskCommandClassNamesThatInfluenceKieSession.contains(commandClass.getSimpleName());
    }
    
    private static Set<String> sendObjectParameterCommandClassNames = new HashSet<String>();
    static { 
        sendObjectParameterCommandClassNames.add("CompleteWorkItemCommand");
        sendObjectParameterCommandClassNames.add("SignalEventCommand");
        sendObjectParameterCommandClassNames.add("StartCorrelatedProcessCommand");
        sendObjectParameterCommandClassNames.add("StartProcessCommand");
        
        sendObjectParameterCommandClassNames.add("SetGlobalCommand");
        sendObjectParameterCommandClassNames.add("InsertObjectCommand");
        sendObjectParameterCommandClassNames.add("UpdateCommand");

        sendObjectParameterCommandClassNames.add("AddTaskCommand");
        sendObjectParameterCommandClassNames.add("CompleteTaskCommand");
        sendObjectParameterCommandClassNames.add("FailTaskCommand");
        sendObjectParameterCommandClassNames.add("CompositeCommand");
        
        sendObjectParameterCommandClassNames = Collections.unmodifiableSet(sendObjectParameterCommandClassNames);
    }

    public static boolean isSendObjectParameterCommandClass(Class<?> commandClass) { 
        return sendObjectParameterCommandClassNames.contains(commandClass.getSimpleName());
    }
    
    private static Set<String> receiveObjectParameterCommandClassNames = new HashSet<String>();
    static { 
        receiveObjectParameterCommandClassNames.add("GetVariableCommand");
        receiveObjectParameterCommandClassNames.add("GetGlobalCommand");
        
        receiveObjectParameterCommandClassNames = Collections.unmodifiableSet(sendObjectParameterCommandClassNames);
    }
    
    public static boolean isReceiveObjectParameterCommandClass(Class<?> commandClass) { 
        return receiveObjectParameterCommandClassNames.contains(commandClass.getSimpleName());
    }
}

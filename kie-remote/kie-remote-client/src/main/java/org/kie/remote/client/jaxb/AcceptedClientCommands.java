/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.client.jaxb;

import org.kie.remote.jaxb.gen.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.kie.remote.jaxb.gen.AbortWorkItemCommand;

@SuppressWarnings("rawtypes")
public class AcceptedClientCommands {

    private AcceptedClientCommands() { 
        // static fields only
    }
    
    private static Set<Class<?>> acceptedCommands = new HashSet<Class<?>>();
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

        acceptedCommands.add(GetContentByIdCommand.class);
        acceptedCommands.add(GetTaskContentCommand.class);

        acceptedCommands.add(DeleteCommentCommand.class);
        acceptedCommands.add(AddCommentCommand.class);
        acceptedCommands.add(GetAllCommentsCommand.class);
        acceptedCommands.add(GetCommentCommand.class);
        acceptedCommands.add(SetTaskPropertyCommand.class);
        
        acceptedCommands.add(AddContentFromUserCommand.class);
        acceptedCommands.add(GetContentByIdForUserCommand.class);
        acceptedCommands.add(GetContentMapForUserCommand.class);
        
        acceptedCommands.add(GetTaskAssignedAsBusinessAdminCommand.class);
        acceptedCommands.add(GetTaskAssignedAsPotentialOwnerCommand.class);
        acceptedCommands.add(GetTaskByWorkItemIdCommand.class);
        acceptedCommands.add(GetTaskCommand.class);
        acceptedCommands.add(GetTasksByProcessInstanceIdCommand.class);
        acceptedCommands.add(GetTasksByStatusByProcessInstanceIdCommand.class);
        acceptedCommands.add(GetTasksByVariousFieldsCommand.class);
        acceptedCommands.add(GetTasksOwnedCommand.class);
        acceptedCommands.add(TaskQueryWhereCommand.class);
        acceptedCommands.add(NominateTaskCommand.class);
        acceptedCommands.add(ReleaseTaskCommand.class);
        acceptedCommands.add(ResumeTaskCommand.class);
        acceptedCommands.add(SkipTaskCommand.class);
        acceptedCommands.add(StartTaskCommand.class);
        acceptedCommands.add(StopTaskCommand.class);
        acceptedCommands.add(SuspendTaskCommand.class);
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

    public static boolean isAcceptedCommandClass(Class<?> commandClass) { 
        if( ! commandClass.getPackage().getName().equals(AbortWorkItemCommand.class.getPackage().getName()) ) { 
           return false; 
        }
        return acceptedCommands.contains(commandClass);
    }
    
    private static Set<Class<?>> taskCommandClassNamesThatInfluenceKieSession = new HashSet<Class<?>>();
    static { 
        taskCommandClassNamesThatInfluenceKieSession.add(CompleteTaskCommand.class);
        taskCommandClassNamesThatInfluenceKieSession.add(ExitTaskCommand.class);
        taskCommandClassNamesThatInfluenceKieSession.add(FailTaskCommand.class);
        taskCommandClassNamesThatInfluenceKieSession.add(SkipTaskCommand.class);
        
        taskCommandClassNamesThatInfluenceKieSession = Collections.unmodifiableSet(taskCommandClassNamesThatInfluenceKieSession);
    }
    
    public static boolean isTaskCommandClassThatInfluencesKieSession(Class<?> commandClass) { 
        return taskCommandClassNamesThatInfluenceKieSession.contains(commandClass);
    }
    
    private static Set<Class<?>> sendObjectParameterCommandClasses = new HashSet<Class<?>>();
    static { 
        sendObjectParameterCommandClasses.add(CompleteWorkItemCommand.class);
        sendObjectParameterCommandClasses.add(SignalEventCommand.class);
        sendObjectParameterCommandClasses.add(StartCorrelatedProcessCommand.class);
        sendObjectParameterCommandClasses.add(StartProcessCommand.class);
        
        sendObjectParameterCommandClasses.add(SetGlobalCommand.class);
        sendObjectParameterCommandClasses.add(InsertObjectCommand.class);
        sendObjectParameterCommandClasses.add(UpdateCommand.class);

        sendObjectParameterCommandClasses.add(AddTaskCommand.class);
        sendObjectParameterCommandClasses.add(CompleteTaskCommand.class);
        sendObjectParameterCommandClasses.add(FailTaskCommand.class);

        sendObjectParameterCommandClasses.add(AddContentFromUserCommand.class);

        sendObjectParameterCommandClasses = Collections.unmodifiableSet(sendObjectParameterCommandClasses);
    }

    public static boolean isSendObjectParameterCommandClass(Class<?> commandClass) { 
        return sendObjectParameterCommandClasses.contains(commandClass);
    }
   
    private static Set<Class<?>> receiveObjectParameterCommandClasses = new HashSet<Class<?>>();
    static { 
        receiveObjectParameterCommandClasses.add(GetVariableCommand.class);
        receiveObjectParameterCommandClasses.add(GetGlobalCommand.class);
        
        receiveObjectParameterCommandClasses = Collections.unmodifiableSet(sendObjectParameterCommandClasses);
    }
    
    public static boolean isReceiveObjectParameterCommandClass(Class<?> commandClass) { 
        return receiveObjectParameterCommandClasses.contains(commandClass);
    }
}

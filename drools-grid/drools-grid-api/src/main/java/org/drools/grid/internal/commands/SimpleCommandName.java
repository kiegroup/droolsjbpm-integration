package org.drools.grid.internal.commands;


public enum SimpleCommandName {
    OperationRequest,
    OperationResponse,
    
    ClaimRequest,
    ClaimResponse,
    
    StartRequest,
    StartResponse,
    
    StopRequest,
    StopResponse,
    
    ReleaseRequest,
    ReleaseResponse,  
    
    SuspendRequest,
    SuspendResponse, 
    
    ResumeRequest,
    ResumeResponse, 
    
    SkipRequest,
    SkipResponse,  
    
    DelegateRequest,
    DelegateResponse,
    
    ForwardRequest,
    ForwardResponse,
    
    CompleteRequest,
    CompleteResponse,   
    
    FailRequest,
    FailResponse,
    
    GetTaskRequest,
    GetTaskResponse,
    
    AddTaskRequest,
    AddTaskResponse,
    
    AddAttachmentRequest,
    AddAttachmentResponse,    
    DeleteAttachmentRequest,
    DeleteAttachmentResponse,
        
    SetDocumentContentRequest,
    SetDocumentContentResponse,
    GetContentRequest,
    GetContentResponse,
    
    AddCommentRequest,
    AddCommentResponse,    
    DeleteCommentRequest,    
    DeleteCommentResponse,    
    
    QueryTasksOwned,    
    QueryTasksAssignedAsBusinessAdministrator,
    QueryTasksAssignedAsExcludedOwner,
    QueryTasksAssignedAsPotentialOwner,
    QueryTasksAssignedAsPotentialOwnerWithGroup,
    QueryTasksAssignedAsPotentialOwnerByGroup,
    QuerySubTasksAssignedAsPotentialOwner,
    QueryGetSubTasksByParentTaskId,
    QueryTasksAssignedAsRecipient,
    QueryTasksAssignedAsTaskInitiator,
    QueryTasksAssignedAsTaskStakeholder,    
    QueryTaskSummaryResponse,
    
    RegisterForEventRequest,
    EventTriggerResponse,
    
    RegisterClient,

    RegisterExecutor,
    UnRegisterExecutor,
    RegisterKBase,
    RequestKBaseId,
    UnRegisterKBase,
    RequestExecutorsMap,
    RequestKBasesMap,
    RequestLookupSessionId,
    ResponseLookupSession
}

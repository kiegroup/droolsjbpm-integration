package org.kie.remote.client.api;

import java.util.Map;

public interface RemoteTaskService {

    // MAIN TASK OPERATIONS

    RemoteApiResponse activate( long taskId );

    RemoteApiResponse claim( long taskId );

    RemoteApiResponse claimNextAvailable();

    RemoteApiResponse complete( long taskId, Map<String, Object> data );

    RemoteApiResponse delegate( long taskId, String targetUserId );

    RemoteApiResponse exit( long taskId );

    RemoteApiResponse fail( long taskId );

    RemoteApiResponse fail( long taskId, Map<String, Object> faultData );

    RemoteApiResponse forward( long taskId, String targetUserId );

    RemoteApiResponse release( long taskId );

    RemoteApiResponse resume( long taskId );

    RemoteApiResponse skip( long taskId );

    RemoteApiResponse start( long taskId );

    RemoteApiResponse stop( long taskId );

    RemoteApiResponse suspend( long taskId );

    RemoteApiResponse nominate( long taskId, String... potentialOwnerUserIds );

    // CONTENT OPERATIONS

    RemoteApiResponse<Long> addOutputContent( long taskId, Map<String, Object> params );
    
    RemoteApiResponse<Map<String, Object>> getOutputContentMap( long taskId );
    
}

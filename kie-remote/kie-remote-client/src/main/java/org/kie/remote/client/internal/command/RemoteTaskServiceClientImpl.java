package org.kie.remote.client.internal.command;

import static org.kie.remote.client.jaxb.ConversionUtil.*;
import static org.kie.remote.client.jaxb.ConversionUtil.convertStringListToGenOrgEntList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.remote.client.api.RemoteApiResponse;
import org.kie.remote.client.api.RemoteApiResponse.RemoteOperationStatus;
import org.kie.remote.client.api.RemoteTaskService;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.api.exception.RemoteTaskException;
import org.kie.remote.jaxb.gen.AddContentFromUserCommand;
import org.kie.remote.jaxb.gen.GetContentMapForUserCommand;
import org.kie.remote.jaxb.gen.NominateTaskCommand;

public class RemoteTaskServiceClientImpl implements RemoteTaskService {

    private final TaskServiceClientCommandObject delegate;
    
    RemoteTaskServiceClientImpl(RemoteConfiguration config) {
        this.delegate = new TaskServiceClientCommandObject(config);
    }

    private static <T> RemoteApiResponse<T> createRemoteApiResponse(RemoteClientException rce ) { 
        RemoteApiResponse<T> response;
        String message = rce.getShortMessage();
        if( message == null ) { 
            message = rce.getMessage();
        }
        Throwable exc = rce.getCause();
        if( exc == null ) { 
            exc = rce;
        }
        if( rce instanceof RemoteTaskException ) { 
            response = new RemoteApiResponse<T>(RemoteOperationStatus.PERMISSIONS_FAILURE, message, exc);
        } else if( rce instanceof RemoteCommunicationException ) { 
            response = new RemoteApiResponse<T>(RemoteOperationStatus.COMMUNICATION_FAILURE, message, exc);
        } else if( rce instanceof RemoteApiException ) { 
            if( exc instanceof RemoteClientException ) { 
                response = new RemoteApiResponse<T>(RemoteOperationStatus.SERVER_FAILURE, message, exc);
            } else { 
                response = new RemoteApiResponse<T>(RemoteOperationStatus.CLIENT_FAILURE, message, exc);
            }
        } else { 
            response = new RemoteApiResponse<T>(RemoteOperationStatus.UNKNOWN_FAILURE, message, exc);
        } 
        return response;
    }

    // RemoteTaskService methods
    
    @Override
    public RemoteApiResponse activate( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.activate(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

    @Override
    public RemoteApiResponse claim( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.claim(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

    @Override
    public RemoteApiResponse claimNextAvailable() {
        RemoteApiResponse<Void> response;
        try { 
            delegate.claimNextAvailable(delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

    @Override
    public RemoteApiResponse complete( long taskId, Map<String, Object> data ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.complete(taskId, delegate.getConfig().getUserName(), data);
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse delegate( long taskId, String targetUserId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.delegate(taskId, delegate.getConfig().getUserName(), targetUserId);
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse exit( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.exit(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse fail( long taskId ) {
        return fail(taskId, null);

    }

    @Override
    public RemoteApiResponse fail( long taskId, Map<String, Object> faultData ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.fail(taskId, delegate.getConfig().getUserName(), faultData);
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse forward( long taskId, String targetEntityId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.forward(taskId, delegate.getConfig().getUserName(), targetEntityId);
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse release( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.release(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse resume( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.resume(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse skip( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.skip(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse start( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.start(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse stop( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.stop(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse suspend( long taskId ) {
        RemoteApiResponse<Void> response;
        try { 
            delegate.suspend(taskId, delegate.getConfig().getUserName());
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;

    }

    @Override
    public RemoteApiResponse nominate( long taskId, String... potentialOwnerUserIds ) {
        RemoteApiResponse<Void> response;
        if( potentialOwnerUserIds == null || potentialOwnerUserIds.length == 0 ) { 
           return new RemoteApiResponse<Void>(RemoteOperationStatus.CLIENT_FAILURE, "Null or empty list of potential owner user ids received as argument");
        }
        try { 
            NominateTaskCommand cmd = new NominateTaskCommand();
            cmd.setTaskId(taskId);
            cmd.setUserId(delegate.getConfig().getUserName());
            List<org.kie.remote.jaxb.gen.OrganizationalEntity> genOrgEntList 
                = convertStringListToGenOrgEntList(Arrays.asList(potentialOwnerUserIds));
            if( genOrgEntList != null ) {
                cmd.getPotentialOwners().addAll(genOrgEntList);
            }
            delegate.executeCommand(cmd);
            response = new RemoteApiResponse<Void>();
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Void>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

    // CONTENT operations

    @Override
    public RemoteApiResponse<Long> addOutputContent( long taskId, Map<String, Object> params ) {
        RemoteApiResponse<Long> response;
        if( params == null ) { 
           return new RemoteApiResponse<Long>(RemoteOperationStatus.CLIENT_FAILURE, "Null Map<String, Object> received as argument");
        }
        try { 
            AddContentFromUserCommand cmd = new AddContentFromUserCommand();
            cmd.setTaskId(taskId);
            cmd.setUserId(delegate.getConfig().getUserName());
            cmd.setOutputContentMap(convertMapToJaxbStringObjectPairArray(params));
            
            Long contentId = delegate.executeCommand(cmd);
            response = new RemoteApiResponse<Long>(contentId);
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Long>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

    @Override
    public RemoteApiResponse<Map<String, Object>> getOutputContentMap( long taskId ) {
        RemoteApiResponse<Map<String, Object>> response;
        
        try { 
            GetContentMapForUserCommand cmd = new GetContentMapForUserCommand();
            cmd.setTaskId(taskId);
            cmd.setUserId(delegate.getConfig().getUserName());
            
            Map<String, Object> outputContentMap = delegate.executeCommand(cmd);
            response = new RemoteApiResponse<Map<String, Object>>(outputContentMap);
        } catch( RemoteClientException rce ) { 
            response = createRemoteApiResponse(rce);
        } catch( Exception e ) { 
            response = new RemoteApiResponse<Map<String, Object>>(RemoteOperationStatus.UNKNOWN_FAILURE, e);
        }
        return response;
    }

}

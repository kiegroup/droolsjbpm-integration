package org.drools.grid.remote;

import java.io.Serializable;
import java.util.Map;

import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

public class WorkItemManagerRemoteClient
    implements
    WorkItemManager,
    Serializable {

    private static final long    serialVersionUID = 1L;

    private GenericNodeConnector connector;
    private MessageSession       messageSession;
    private String               instanceId;

    public void abortWorkItem(long id) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void completeWorkItem(long id,
                                 Map<String, Object> results) {
        String kresultsId = "kresults_" + this.messageSession.getSessionId();
        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   true,
                                   new KnowledgeContextResolveFromContextCommand( new CompleteWorkItemCommand( id,
                                                                                                               results ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );
        try {
            this.connector.connect();
            this.connector.write( msg );
            this.connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public void registerWorkItemHandler(String workItemName,
                                        WorkItemHandler handler) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void setConnector(GenericNodeConnector connector) {
        this.connector = connector;
    }

    public void setMessageSession(MessageSession messageSession) {
        this.messageSession = messageSession;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}

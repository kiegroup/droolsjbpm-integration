package org.drools.grid.distributed;


import org.drools.grid.ExecutionNodeService;
import java.util.Properties;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;

import org.drools.command.FinishedCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.SetVariableCommand;
import org.drools.grid.GenericConnection;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;


public class KnowledgeBaseProviderGridClient
    implements
    KnowledgeBaseFactoryService {
    
    private GenericConnection connection;
    private String localId = "";   
    private GenericNodeConnector connector;
    private MessageSession messageSession;
    
    public KnowledgeBaseProviderGridClient(GenericNodeConnector connector, GenericConnection connection) {
        
        this.connection = connection;
        this.messageSession = new MessageSession();
        this.connector = new DistributedRioNodeConnector("client 1", SystemEventListenerFactory.getSystemEventListener(), 
                                            ((DistributedRioNodeConnector)connector).getExecutionNodeService());

    }

   
    public Environment newEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( ( KnowledgeBaseConfiguration ) null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        //return new NewKnowledgeBaseCommand(null);
        if(localId == null || localId.equals("")){
            localId = UUID.randomUUID().toString();
        }

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBaseCommand( conf ) ) );
        try {
            Object object = connector.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }

        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        } 

        return new KnowledgeBaseGridClient( localId,
                                              connector, messageSession, connection);

    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
    	// TODO: change this to use a remote implementation instead the local factory?
        return KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader classLoader) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties, ClassLoader... classLoader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   
    
}

package org.drools.grid.remote;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.command.FinishedCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.SetVariableCommand;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;

public class KnowledgeBaseProviderRemoteClient
    implements
    KnowledgeBaseFactoryService {
    
    private GenericNodeConnector connector;
    private MessageSession          messageSession;
    public KnowledgeBaseProviderRemoteClient(GenericNodeConnector connector) {
         this.connector = connector;
         this.messageSession = new MessageSession();
    }

    public Environment newEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( ( KnowledgeBaseConfiguration ) null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        

        String localId = UUID.randomUUID().toString();

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBaseCommand( conf ) ) );
        try {
            connector.connect();
            Object object = connector.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }
            connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBaseRemoteClient( localId,
                                              connector, messageSession );

    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
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
        //TODO: I need to replace this random id with the kbase ID and test it
        //String localId = UUID.randomUUID().toString();
        String localId = kbaseId;

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBaseCommand( null ) ) );
        registerKBaseInDirectories(kbaseId);

        try {
            connector.connect();
            Object object = connector.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }
            connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBaseRemoteClient( localId,
                                              connector, messageSession );
        
    }

    private void registerKBaseInDirectories(String kbaseId) {
        try {
            DirectoryNodeService directory = connector.getConnection().getDirectoryNode().get(DirectoryNodeService.class);
            directory.registerKBase(kbaseId, connector.getId());
            
        } catch (RemoteException ex) {
            Logger.getLogger(KnowledgeBaseProviderRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectorException ex) {
            Logger.getLogger(KnowledgeBaseProviderRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        //TODO: I need to replace this random id with the kbase ID and test it
        //String localId = UUID.randomUUID().toString();
        String localId = kbaseId;
        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBaseCommand( conf ) ) );
        registerKBaseInDirectories(kbaseId);

        try {
            connector.connect();
            Object object = connector.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }
            connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBaseRemoteClient( localId,
                                              connector, messageSession );
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties, ClassLoader... classLoader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

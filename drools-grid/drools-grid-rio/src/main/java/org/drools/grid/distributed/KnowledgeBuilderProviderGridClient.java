/**
 * 
 */
package org.drools.grid.distributed;


import org.drools.grid.ExecutionNodeService;
import java.util.Properties;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactoryService;

import org.drools.command.FinishedCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;
import org.drools.grid.distributed.rio.InternalRioNodeConnector;


public class KnowledgeBuilderProviderGridClient
    implements
    KnowledgeBuilderFactoryService {
    
    private MessageSession messageSession;
    private GenericNodeConnector client;



    public KnowledgeBuilderProviderGridClient(GenericNodeConnector currentService, GridConnection gridClient) {
            this.messageSession = new MessageSession();
            client = new InternalRioNodeConnector("client 1", SystemEventListenerFactory.getSystemEventListener(), (ExecutionNodeService) currentService);
        
    }

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        // TODO Auto-generated method stub
        return null; 
    }

    public KnowledgeBuilder newKnowledgeBuilder() {
        String localId = UUID.randomUUID().toString();

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBuilderCommand( null ) ) );

        try {
            Object object = client.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }

        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBuilderGridClient( localId,
                                                 client, messageSession );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader classLoader) {
        // TODO Auto-generated method stub
        return null;
    }

}
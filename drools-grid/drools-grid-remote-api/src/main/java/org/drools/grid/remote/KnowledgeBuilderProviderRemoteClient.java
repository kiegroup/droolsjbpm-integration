/**
 * 
 */
package org.drools.grid.remote;

import com.sun.tools.xjc.Options;
import java.util.Properties;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactoryService;
//import org.drools.command.FinishedCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;

public class KnowledgeBuilderProviderRemoteClient
    implements
    KnowledgeBuilderFactoryService {
    
    private GenericNodeConnector connector;
    private MessageSession messageSession;
    public KnowledgeBuilderProviderRemoteClient(GenericNodeConnector connector) {
        
        this.messageSession = new MessageSession();
        this.connector = connector;

    }

    public DecisionTableConfiguration newDecisionTableConfiguration() {
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
            connector.connect();
            Object object = connector.write( msg ).getPayload();

//            if ( !(object instanceof FinishedCommand) ) {
//                throw new RuntimeException( "Response was not correctly ended" );
//            }
            connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBuilderRemoteClient( localId,
                                                 connector, messageSession );
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

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties, ClassLoader... classLoader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts, String systemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
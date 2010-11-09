/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package org.drools.grid.remote;

import com.sun.tools.xjc.Options;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderProviderRemoteClient
    implements
    KnowledgeBuilderFactoryService {

    private ConversationManager    cm;
    private GridServiceDescription<GridNode>  gsd;

    public KnowledgeBuilderProviderRemoteClient(ConversationManager cm,
                                                GridServiceDescription gsd) {
        this.cm = cm;
        this.gsd = gsd;
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder() {

        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new SetVariableCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new NewKnowledgeBuilderCommand( null ) ) } ) );

        sendMessage( this.cm,
                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                     this.gsd.getId(),
                     cmd );

        return new KnowledgeBuilderRemoteClient( localId,
                                                 this.gsd,
                                                 this.cm );

    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts,
                                                  String systemId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public static Object sendMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String recipientId,
                                     Object body) {

        InetSocketAddress[] sockets = null;
        if ( addr instanceof InetSocketAddress[] ) {
            sockets = (InetSocketAddress[]) addr;
        } else if ( addr instanceof InetSocketAddress ) {
            sockets = new InetSocketAddress[ 1 ];
            sockets[0] = (InetSocketAddress) addr;
        }

        BlockingMessageResponseHandler handler = new BlockingMessageResponseHandler();
        Exception exception = null;
        for ( InetSocketAddress socket : sockets ) {
            try {
                Conversation conv = conversationManager.startConversation( socket,
                                                                           recipientId );
                conv.sendMessage( body,
                                  handler );
                exception = null;
            } catch ( Exception e ) {
                exception = e;
                conversationManager.endConversation();
            }
            if ( exception == null ) {
                break;
            }
        }
        if ( exception != null ) {
            throw new RuntimeException( "Unable to send message",
                                        exception );
        }
        try {
            return handler.getMessage().getBody();
        } finally {
            conversationManager.endConversation();
        }
    }
}

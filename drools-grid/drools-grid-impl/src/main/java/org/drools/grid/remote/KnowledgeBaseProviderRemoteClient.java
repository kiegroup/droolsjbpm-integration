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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.SetVariableCommand;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;

/**
 *
 * @author salaboy
 */
public class KnowledgeBaseProviderRemoteClient
    implements
    KnowledgeBaseFactoryService {

    private ConversationManager    cm;
    private GridServiceDescription gsd;

    public KnowledgeBaseProviderRemoteClient(ConversationManager cm,
                                             GridServiceDescription gsd) {
        this.cm = cm;
        this.gsd = gsd;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase newKnowledgeBase() {
        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new SetVariableCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new NewKnowledgeBaseCommand( null ) ) } ) );

        sendMessage( this.cm,
                     (InetSocketAddress[]) this.gsd.getAddresses().get( "socket" ).getObject(),
                     this.gsd.getServiceInterface().getName(),
                     cmd );

        return new KnowledgeBaseRemoteClient( localId,
                                              this.gsd,
                                              this.cm );

    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Environment newEnvironment() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public static Object sendMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String id,
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
                                                                           id );
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

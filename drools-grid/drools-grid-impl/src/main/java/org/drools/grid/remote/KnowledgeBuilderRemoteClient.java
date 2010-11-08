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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.command.builder.KnowledgeBuilderGetErrorsCommand;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.definition.KnowledgePackage;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CollectionClient;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.io.Resource;
import org.drools.runtime.ExecutionResults;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderRemoteClient
    implements
    KnowledgeBuilder {

    private String                 instanceId;
    private ConversationManager    cm;
    private GridServiceDescription gsd;

    public KnowledgeBuilderRemoteClient(String localId,
                                        GridServiceDescription gsd,
                                        ConversationManager cm) {
        this.instanceId = localId;
        this.gsd = gsd;
        this.cm = cm;
    }

    public void add(Resource resource,
                    ResourceType type) {
        add( resource,
             type,
             null );

    }

    public void add(Resource resource,
                    ResourceType type,
                    ResourceConfiguration configuration) {

        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderAddCommand( resource,
                                                                                                                                                       type,
                                                                                                                                                       configuration ),
                                                                                                                       this.instanceId,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       null ) } ) );

        sendMessage( this.cm,
                     (InetSocketAddress[]) this.gsd.getAddresses().get( "socket" ).getObject(),
                     this.gsd.getServiceInterface().getName(),
                     cmd );

    }

    public Collection<KnowledgePackage> getKnowledgePackages() {
        return new CollectionClient<KnowledgePackage>( this.instanceId );
    }

    public KnowledgeBase newKnowledgeBase() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean hasErrors() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilderErrors getErrors() {
        String commandId = "kbuilder.getErrors_" + this.gsd.getId();
        String kresultsId = "kresults_" + this.gsd.getId();
        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderGetErrorsCommand(),
                                                                                                                       this.instanceId,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       kresultsId ) } ) );

        Object result = sendMessage( this.cm,
                                     (InetSocketAddress[]) this.gsd.getAddresses().get( "socket" ).getObject(),
                                     this.gsd.getServiceInterface().getName(),
                                     cmd );

        return (KnowledgeBuilderErrors) result;

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

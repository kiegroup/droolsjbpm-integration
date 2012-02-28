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
import org.drools.grid.internal.responsehandlers.AsyncMessageResponseHandler;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;

public class ConversationUtil {

    public static Object sendMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String id,
                                     Object body) {
        // This method was added to provide a level of backwards compatability
        // until we have a correct way of setting senderId
        return sendMessage( conversationManager,
                            "",
                            addr,
                            id,
                            body );

    }
    public static void sendAsyncMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String id,
                                     Object body) {
        // This method was added to provide a level of backwards compatability
        // until we have a correct way of setting senderId
        sendAsyncMessage( conversationManager,
                            "",
                            addr,
                            id,
                            body );

    }

    public static Object sendMessage(ConversationManager conversationManager,
                                     String senderId,
                                     Serializable addr,
                                     String id,
                                     Object body) {

        InetSocketAddress[] sockets = null;
        if ( addr instanceof InetSocketAddress[] ) {
            sockets = (InetSocketAddress[]) addr;
        } else if ( addr instanceof InetSocketAddress ) {
            sockets = new InetSocketAddress[1];
            sockets[0] = (InetSocketAddress) addr;
        }

        BlockingMessageResponseHandler handler = new BlockingMessageResponseHandler();
        Exception exception = null;
        Conversation conv = null;
        for ( InetSocketAddress socket : sockets ) {
            try {
                conv = conversationManager.startConversation( senderId,
                                                              socket,
                                                              id );
                conv.sendMessage( body,
                                  handler );
                exception = null;
            } catch ( Exception e ) {
                exception = e;
                if ( conv != null ) {
                    conv.endConversation();
                }
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
            conv.endConversation();
        }
    }
    
    public static void sendAsyncMessage(ConversationManager conversationManager,
                                     String senderId,
                                     Serializable addr,
                                     String id,
                                     Object body) {

        InetSocketAddress[] sockets = null;
        if ( addr instanceof InetSocketAddress[] ) {
            sockets = (InetSocketAddress[]) addr;
        } else if ( addr instanceof InetSocketAddress ) {
            sockets = new InetSocketAddress[1];
            sockets[0] = (InetSocketAddress) addr;
        }

        AsyncMessageResponseHandler handler = new AsyncMessageResponseHandler();
        Exception exception = null;
        Conversation conv = null;
        for ( InetSocketAddress socket : sockets ) {
            try {
                conv = conversationManager.startConversation( senderId,
                                                              socket,
                                                              id );
                conv.sendMessage( body,
                                  handler );
                exception = null;
            } catch ( Exception e ) {
                exception = e;
                if ( conv != null ) {
                    conv.endConversation();
                }
            }
            if ( exception == null ) {
                break;
            }
        }
        if ( exception != null ) {
            throw new RuntimeException( "Unable to send message",
                                        exception );
        }
        //try {
        //    return handler.getMessage().getBody();
        //} 
        //finally {
            conv.endConversation();
        //}
    }
    
}

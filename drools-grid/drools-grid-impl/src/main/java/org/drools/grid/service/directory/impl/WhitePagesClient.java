package org.drools.grid.service.directory.impl;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesClient
    implements
    WhitePages,
    MessageReceiverHandlerFactoryService {
    private Grid                grid;

    public WhitePagesClient(Grid grid) {
        this.grid = grid;
    }

    public GridServiceDescription<WhitePages> getGsd() {
        return this.grid.get( CoreServicesLookup.class ).lookup( WhitePages.class );
    }

    public static Object sendMessage(ConversationManager conversationManager,
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
                conv = conversationManager.startConversation( "",
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

    public GridServiceDescription create( String serviceDescriptionId, String ownerId ) {
        GridServiceDescription<WhitePages> wpGsd = getGsd();
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) wpGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.create",
                                           Arrays.asList( new Object[]{serviceDescriptionId, ownerId} ) );
        ConversationManager convm = this.grid.get( ConversationManager.class );
        GridServiceDescription gsd = (GridServiceDescription) sendMessage( convm,
                                                                           sockets,
                                                                           wpGsd.getId(),
                                                                           cmd );
        return new GridServiceDescriptionClient( gsd,
                                                 wpGsd,
                                                 convm );
    }

    public GridServiceDescription lookup(String serviceDescriptionId) {
        GridServiceDescription<WhitePages> wpGsd = getGsd();
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) wpGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.lookup",
                                           Arrays.asList( new Object[]{serviceDescriptionId} ) );
        ConversationManager convm = this.grid.get( ConversationManager.class );
        GridServiceDescription gsd = (GridServiceDescription) sendMessage( convm,
                                                                           sockets,
                                                                           wpGsd.getId(),
                                                                           cmd );
        return (gsd == null) ? gsd : new GridServiceDescriptionClient( gsd,
                                                                       wpGsd,
                                                                       convm );
    }

    public void remove(String serviceDescriptionId) {
        GridServiceDescription<WhitePages> wpGsd = getGsd();

        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) wpGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.remove",
                                           Arrays.asList( new Object[]{serviceDescriptionId} ) );
        ConversationManager convm = this.grid.get( ConversationManager.class );
        sendMessage( convm,
                     sockets,
                     wpGsd.getId(),
                     cmd );
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new WhitePagesServer( this );
    }

    public void registerSocketService(Grid grid,
                                      String id,
                                      String ip,
                                      int port) {
        WhitePagesImpl.doRegisterSocketService( grid,
                                                id,
                                                ip,
                                                port );
    }

}

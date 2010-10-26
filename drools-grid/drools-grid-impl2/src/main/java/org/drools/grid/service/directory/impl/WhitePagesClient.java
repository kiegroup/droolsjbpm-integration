package org.drools.grid.service.directory.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.drools.SystemEventListenerFactory;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesClient
    implements
    WhitePages,
    MessageReceiverHandlerFactoryService {
    private GridServiceDescription whitePagesGsd;

    private ConversationManager    conversationManager;

    public WhitePagesClient(GridServiceDescription gsd,
                            ConversationManager conversationManager) {
        this.whitePagesGsd = gsd;
        this.conversationManager = conversationManager;
    }
    
    public WhitePagesClient(GridServiceDescription gsd) {
        this.whitePagesGsd = gsd;
        this.conversationManager = new ConversationManagerImpl("wpclient", new MinaConnector(), SystemEventListenerFactory.getSystemEventListener());
    }

    public static Object sendMessage(ConversationManager conversationManager,
                                     InetSocketAddress[] sockets,
                                     String id,
                                     Object body) {
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

    public GridServiceDescription create(String serviceDescriptionId) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.create",
                                           Arrays.asList( new Object[]{ serviceDescriptionId } ) );
        GridServiceDescription gsd = ( GridServiceDescription ) sendMessage( this.conversationManager,
                     sockets,
                     this.whitePagesGsd.getId(),
                     cmd );
        return new GridServiceDescriptionClient(gsd,
                                                this.whitePagesGsd,
                                                this.conversationManager );
    }

    public GridServiceDescription lookup(String serviceDescriptionId) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.lookup",
                                           Arrays.asList( new Object[]{ serviceDescriptionId } ) );
        GridServiceDescription gsd = ( GridServiceDescription ) sendMessage( this.conversationManager,
                                                                             sockets,
                                                                             this.whitePagesGsd.getId(),
                                                                             cmd );
        return (gsd == null ) ? gsd : new GridServiceDescriptionClient(gsd,
                                                                       this.whitePagesGsd,
                                                                       this.conversationManager );
    }

    public void remove(String serviceDescriptionId) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.remove",
                                           Arrays.asList( new Object[]{ serviceDescriptionId } ) ); 
        sendMessage( this.conversationManager,
                     sockets,
                     this.whitePagesGsd.getId(),
                     cmd );        
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new WhitePagesServer( this );
    }

    public List<GridServiceDescription> lookupServices(Class clazz) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "WhitePages.lookupServices",
                                           Arrays.asList( new Object[]{ clazz } ) );
        List<GridServiceDescription> gsds = ( List<GridServiceDescription> ) sendMessage( this.conversationManager,
                                                                             sockets,
                                                                             this.whitePagesGsd.getId(),
                                                                             cmd);
        List<GridServiceDescription> result = new ArrayList<GridServiceDescription>();                                                                    
        
        for(GridServiceDescription gsd : gsds){
           result.add( new GridServiceDescriptionClient(gsd,
                                        this.whitePagesGsd,
                                        this.conversationManager )); 
        }
         
             
        return result;
    }

    //    public void addAddress(String id,
    //                           Address address) {               
    //        InetSocketAddress[] sockets = ( InetSocketAddress[] ) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
    //        CommandImpl cmd = new CommandImpl( "addAddress", Arrays.asList( new Object[] { id, address } ) );
    //        sendMessage( sockets, cmd );
    //    }
    //
    //    public GridServiceDescription lookup(String id) {
    //        InetSocketAddress[] sockets = ( InetSocketAddress[] ) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
    //        CommandImpl cmd = new CommandImpl( "lookup", Arrays.asList( new Object[] { id } ) );
    //        return ( GridServiceDescription ) sendMessage( sockets, cmd );
    //    }
    //
    //    public GridServiceDescription create(GridServiceDescription serviceDescription) {
    //        InetSocketAddress[] sockets = ( InetSocketAddress[] ) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
    //        CommandImpl cmd = new CommandImpl( "register", Arrays.asList( new Object[] { serviceDescription } ) );
    //        sendMessage( sockets, cmd );        
    //    }
    //
    //    public void removeAddress(String id,
    //                              Address address) {
    //        InetSocketAddress[] sockets = ( InetSocketAddress[] ) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
    //        CommandImpl cmd = new CommandImpl( "removeAddress", Arrays.asList( new Object[] { id, address } ) );
    //        sendMessage( sockets, cmd );
    //    }
    //
    //    public void remove(String id) {
    //        InetSocketAddress[] sockets = ( InetSocketAddress[] ) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
    //        CommandImpl cmd = new CommandImpl( "unregister", Arrays.asList( new Object[] { id } ) );
    //        sendMessage( sockets, cmd );      
    //    }

}

package org.drools.grid.service.directory.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.io.impl.ExceptionMessage;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhitePagesServer
    implements
    MessageReceiverHandler {
    
    private static Logger logger = LoggerFactory.getLogger(WhitePagesServer.class);
    
    private WhitePages whitePages;

    public WhitePagesServer( WhitePages whitePages ) {
        this.whitePages = whitePages;
    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        final CommandImpl cmd = (CommandImpl) msg.getBody();
        try{
            this.execs.get( cmd.getName() ).execute( whitePages,
                                                 conversation,
                                                 msg,
                                                 cmd );
        } catch (Throwable t){
            conversation.respondError(t);
        }
    }

    private Map<String, Exec> execs = new HashMap<String, Exec>() {
                                        {
                                            put( "WhitePages.create",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription gsd = whitePages.create( (String) list.get( 0 ), (String) list.get( 1 ) );
                                                         con.respond( gsd );
                                                     }
                                                 } );
                                            put( "WhitePages.remove",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         whitePages.remove( (String) list.get( 0 ) );
                                                         con.respond( null );
                                                     }
                                                 } );
                                            put( "WhitePages.lookup",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription gsd = whitePages.lookup( (String) list.get( 0 ) );
                                                         if ( gsd != null ) {
                                                             gsd.setServiceInterface( null ); // FIXME URGENT (mdp) workaround due to mina serialization issues.
                                                         }
                                                         con.respond( gsd );
                                                     }
                                                 } );
                                            put( "GridServiceDescription.addAddress",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription gsd = whitePages.lookup( (String) list.get( 0 ) );
                                                         Address address = gsd.addAddress( (String) list.get( 1 ) );
                                                         con.respond( address );
                                                     }
                                                 } );
                                            put( "GridServiceDescription.removeAddress",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription gsd = whitePages.lookup( (String) list.get( 0 ) );
                                                         gsd.removeAddress( (String) list.get( 1 ) );
                                                         con.respond( null );
                                                     }
                                                 } );
                                            put( "GridServiceDescription.setServiceInterface",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription gsd = whitePages.lookup( (String) list.get( 0 ) );
                                                         gsd.setServiceInterface( (Class) list.get( 1 ) );
                                                         con.respond( null );
                                                     }
                                                 } );
                                            put( "Address.setObject",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         WhitePages whitePages = (WhitePages) object;
                                                         final List list = cmd.getArguments();
                                                         GridServiceDescription<WhitePages> gsd = whitePages.lookup( (String) list.get( 0 ) );
                                                         Address address = gsd.getAddresses().get( (String) list.get( 1 ) );
                                                         address.setObject( list.get( 2 ) );
                                                         con.respond( null );
                                                     }
                                                 } );
                                        }
                                    };

    public void exceptionReceived(Conversation conversation, ExceptionMessage msg) {
        logger.error("WhitePagesServer received and exception when it shouldn't");
    }

    public static interface Exec {
        void execute(Object object,
                     Conversation con,
                     Message msg,
                     CommandImpl cmd);
    }
}

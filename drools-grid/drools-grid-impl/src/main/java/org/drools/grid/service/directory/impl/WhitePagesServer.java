package org.drools.grid.service.directory.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.timer.impl.ServiceConfiguration;

public class WhitePagesServer
    implements
    MessageReceiverHandler {
    private WhitePages whitePages;

    public WhitePagesServer(WhitePages whitePages) {
        this.whitePages = whitePages;
    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        final CommandImpl cmd = (CommandImpl) msg.getBody();
        this.execs.get( cmd.getName() ).execute( whitePages,
                                                 conversation,
                                                 msg,
                                                 cmd );
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
                                                         GridServiceDescription gsd = whitePages.create( (String) list.get( 0 ) );
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

    public static interface Exec {
        void execute(Object object,
                     Conversation con,
                     Message msg,
                     CommandImpl cmd);
    }    
}

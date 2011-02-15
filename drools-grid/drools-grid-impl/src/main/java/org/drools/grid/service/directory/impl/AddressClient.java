package org.drools.grid.service.directory.impl;

import static org.drools.grid.service.directory.impl.WhitePagesClient.sendMessage;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;

public class AddressClient
    implements
    Address {
    private Address                detachedLocal;

    private GridServiceDescription whitePagesGsd;

    private ConversationManager    conversationManager;

    public AddressClient(Address detachedLocal,
                         GridServiceDescription gsd,
                         ConversationManager conversationManager) {
        this.detachedLocal = detachedLocal;
        this.whitePagesGsd = gsd;
        this.conversationManager = conversationManager;
    }

    public GridServiceDescription getGridServiceDescription() {
        return new GridServiceDescriptionClient( this.detachedLocal.getGridServiceDescription(),
                                                 this.whitePagesGsd,
                                                 this.conversationManager );
    }

    public Object getObject() {
        return this.detachedLocal.getObject();
    }

    public String getTransport() {
        return this.detachedLocal.getTransport();
    }

    public void setObject(Object object) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "Address.setObject",
                                           Arrays.asList( new Object[]{ this.detachedLocal.getGridServiceDescription().getId(), this.detachedLocal.getTransport(), object } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     this.whitePagesGsd.getId(),
                     cmd );
        this.detachedLocal.setObject( object );
    }

}

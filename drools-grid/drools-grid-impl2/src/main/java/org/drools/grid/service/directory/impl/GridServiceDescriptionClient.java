package org.drools.grid.service.directory.impl;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;

import static org.drools.grid.service.directory.impl.WhitePagesClient.sendMessage;

public class GridServiceDescriptionClient
    implements
    GridServiceDescription {
    private GridServiceDescription whitePagesGsd;

    private ConversationManager    conversationManager;

    private GridServiceDescription detachedLocal;

    public GridServiceDescriptionClient(GridServiceDescription detachedLocal,
                                        GridServiceDescription whitePagesGsd,
                                        ConversationManager conversationManager) {
        this.detachedLocal = detachedLocal;
        this.whitePagesGsd = whitePagesGsd;
        this.conversationManager = conversationManager;
    }

    public Address addAddress(String transport) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.addAddress",
                                           Arrays.asList( new Object[]{ detachedLocal.getId(), transport } ) );
        Address address = (Address) sendMessage( this.conversationManager,
                                                 sockets,
                                                 whitePagesGsd.getId(),
                                                 cmd );
        return new AddressClient( address,
                                  whitePagesGsd,
                                  this.conversationManager );
    }

    public Map<String, Address> getAddresses() {
        Map<String, Address> addresses = new HashMap<String, Address>();
        for ( Address address : this.detachedLocal.getAddresses().values() ) {
            addresses.put( address.getTransport(), new AddressClient( address,
                                                                      this.whitePagesGsd,
                                                                      this.conversationManager ) );
        }
        return Collections.unmodifiableMap( addresses );
    }

    public String getId() {
        return this.detachedLocal.getId();
    }

    public Class getImplementedClass() {
        return this.detachedLocal.getImplementedClass();
    }

    public void removeAddress(String transport) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.removeAddress",
                                           Arrays.asList( new Object[]{ detachedLocal.getId(), transport } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     whitePagesGsd.getId(),
                     cmd );
    }

    public void setImplementedClass(Class cls) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.setImplementedClass",
                                           Arrays.asList( new Object[]{ detachedLocal.getId(), cls } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     whitePagesGsd.getId(),
                     cmd );
    }

    @Override
    public boolean equals(Object obj) {
        //@TODO: improve equals comparision
        final GridServiceDescription other = (GridServiceDescription) obj;
        if (!this.getId().equals(other.getId() )) {
            return false;
        }
        return true;
    }


    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.whitePagesGsd != null ? this.whitePagesGsd.hashCode() : 0);
        hash = 47 * hash + (this.conversationManager != null ? this.conversationManager.hashCode() : 0);
        hash = 47 * hash + (this.detachedLocal != null ? this.detachedLocal.hashCode() : 0);
        return hash;
    }

    
    

}

package org.drools.grid.service.directory.impl;

import static org.drools.grid.service.directory.impl.WhitePagesClient.sendMessage;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;

public class GridServiceDescriptionClient
    implements
    GridServiceDescription {
    private GridServiceDescription whitePagesGsd;

    private ConversationManager    conversationManager;

//    private GridServiceDescription detachedLocal;
    
    private String               id;

    private Class                serviceInterface;

    private Map<String, Address> addresses = new HashMap<String, Address>();

    private Serializable         data;
    
    private String               ownerGridId;

    public GridServiceDescriptionClient(GridServiceDescription gsd,
                                        GridServiceDescription whitePagesGsd,
                                        ConversationManager conversationManager) {
        this.id = gsd.getId();
        this.serviceInterface = gsd.getServiceInterface();
        this.addresses = new HashMap( gsd.getAddresses() );
        this.data = gsd.getData();
        this.whitePagesGsd = whitePagesGsd;
        this.conversationManager = conversationManager;
    }

    public Address addAddress(String transport) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.addAddress",
                                           Arrays.asList( new Object[]{ this.id, transport } ) );
        Address address = (Address) sendMessage( this.conversationManager,
                                                 sockets,
                                                 whitePagesGsd.getId(),
                                                 cmd );
        
        this.addresses.put( transport, address );
        
        return new AddressClient( address,
                                  whitePagesGsd,
                                  this.conversationManager );
    }

    public Map<String, Address> getAddresses() {
        Map<String, Address> addresses = new HashMap<String, Address>();
        for ( Address address : this.addresses.values() ) {
            addresses.put( address.getTransport(),
                           new AddressClient( address,
                                              this.whitePagesGsd,
                                              this.conversationManager ) );
        }
        return Collections.unmodifiableMap( addresses );
    }

    public String getId() {
        return this.id;
    }

    public void removeAddress(String transport) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.removeAddress",
                                           Arrays.asList( new Object[]{ id, transport } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     whitePagesGsd.getId(),
                     cmd );
        this.addresses.remove( transport );
    }

    public Class getServiceInterface() {
        return this.serviceInterface;
    }

    public void setServiceInterface(Class cls) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.setServiceInterface",
                                           Arrays.asList( new Object[]{ id, cls } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     whitePagesGsd.getId(),
                     cmd );
        this.serviceInterface = cls;
    }

    @Override
    public boolean equals(Object obj) {
        //@TODO: improve equals comparision
        final GridServiceDescription other = (GridServiceDescription) obj;
        if ( !this.getId().equals( other.getId() ) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.whitePagesGsd != null ? this.whitePagesGsd.hashCode() : 0);
        hash = 47 * hash + (this.conversationManager != null ? this.conversationManager.hashCode() : 0);
        hash = 47 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public Serializable getData() {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.getData",
                                           null );
        Serializable data = (Serializable) sendMessage( this.conversationManager,
                                                        sockets,
                                                        whitePagesGsd.getId(),
                                                        cmd );
        return data;
    }

    public void setData(Serializable data) {
        InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) whitePagesGsd.getAddresses().get( "socket" )).getObject();
        CommandImpl cmd = new CommandImpl( "GridServiceDescription.setData",
                                           Arrays.asList( new Object[]{ data } ) );
        sendMessage( this.conversationManager,
                     sockets,
                     whitePagesGsd.getId(),
                     cmd );

    }

    public String getOwnerGridId() {
        return ownerGridId;
    }

    public void setOwnerGridId(String ownerGridId) {
        this.ownerGridId = ownerGridId;
    }
}

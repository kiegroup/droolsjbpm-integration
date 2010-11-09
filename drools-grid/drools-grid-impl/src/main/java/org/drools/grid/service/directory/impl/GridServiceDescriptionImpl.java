package org.drools.grid.service.directory.impl;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.Address;

public class GridServiceDescriptionImpl
    implements
    GridServiceDescription,
    Serializable {

    private String               id;

    private Class                serviceInterface;

    private Map<String, Address> addresses = new HashMap<String, Address>();

    private Serializable         data;

    public GridServiceDescriptionImpl() {

    }

    public GridServiceDescriptionImpl(Class cls) {
        this.serviceInterface = cls;
        this.id = cls.getName();
    }

    public GridServiceDescriptionImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getServiceInterface() {
        return this.serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public synchronized Address addAddress(String protocol) {
        AddressImpl address = new AddressImpl( this,
                                               protocol,
                                               null );
        this.addresses.put( address.getTransport(),
                            address );
        return address;
    }

    public synchronized Map<String, Address> getAddresses() {
        return Collections.unmodifiableMap( addresses );
    }

    public synchronized void removeAddress(String transport) {
        this.addresses.remove( transport );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
    public String toString() {
        String result = id + "@";

        Set<String> keys = addresses.keySet();
        for ( String key : keys ) {
            if ( addresses.get( key ).getObject() instanceof InetSocketAddress[] ) {
                result += key + "=[" + ((InetSocketAddress[]) addresses.get( key ).getObject())[0].getHostName() + ":" +
                          ((InetSocketAddress[]) addresses.get( key ).getObject())[0].getPort() + "]/" + addresses.get( key ).getTransport();
            }
        }
        return result;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

}

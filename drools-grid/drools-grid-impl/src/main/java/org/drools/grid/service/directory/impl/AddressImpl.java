package org.drools.grid.service.directory.impl;

import java.io.Serializable;
import java.util.UUID;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.Address;

public class AddressImpl
    implements
    Address,
    Serializable {
    
    private String                 addressId;

    private GridServiceDescription gridServiceDescription;

    private String                 transport;

    private Serializable           addressObject;

    public AddressImpl() {

    }

    public AddressImpl(GridServiceDescription gridServiceDescription,
                       String transport,
                       Serializable object) {
        this.gridServiceDescription = gridServiceDescription;
        this.transport = transport;
        this.addressObject = object;
        this.addressId = UUID.randomUUID().toString()+"-address";
    }
    
    public String getId() {
        return addressId;
    }

    public void setId(String id) {
        this.addressId = id;
    }

    public GridServiceDescription getGridServiceDescription() {
        return gridServiceDescription;
    }

    public String getTransport() {
        return transport;
    }

    public Serializable getObject() {
        return addressObject;
    }

    public void setObject(Object object) {
        this.addressObject = (Serializable) object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((gridServiceDescription == null) ? 0 : gridServiceDescription.getId().hashCode());
        result = prime * result + ((addressObject == null) ? 0 : addressObject.hashCode());
        result = prime * result
                 + ((transport == null) ? 0 : transport.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        AddressImpl other = (AddressImpl) obj;
        if ( gridServiceDescription == null ) {
            if ( other.gridServiceDescription != null ) return false;
        } else if ( !gridServiceDescription.getId().equals( other.gridServiceDescription.getId() ) ) return false;
        if ( addressObject == null ) {
            if ( other.addressObject != null ) return false;
        } else if ( !addressObject.equals( other.addressObject ) ) return false;
        if ( transport == null ) {
            if ( other.transport != null ) return false;
        } else if ( !transport.equals( other.transport ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AddressImpl{" +
                "addressId='" + addressId + '\'' +
                ", gridServiceDescription=" + gridServiceDescription +
                ", transport='" + transport + '\'' +
                ", addressObject=" + addressObject +
                '}';
    }
}

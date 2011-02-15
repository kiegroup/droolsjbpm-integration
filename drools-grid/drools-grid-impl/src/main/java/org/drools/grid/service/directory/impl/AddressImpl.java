package org.drools.grid.service.directory.impl;

import java.io.Serializable;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.Address;

public class AddressImpl
    implements
    Address,
    Serializable {
    private String                 id;

    private GridServiceDescription gridServiceDescription;

    private String                 transport;

    private Serializable           object;

    public AddressImpl() {

    }

    public AddressImpl(GridServiceDescription gridServiceDescription,
                       String transport,
                       Serializable object) {
        this.gridServiceDescription = gridServiceDescription;
        this.transport = transport;
        this.object = object;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GridServiceDescription getGridServiceDescription() {
        return gridServiceDescription;
    }

    public String getTransport() {
        return transport;
    }

    public Serializable getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = (Serializable) object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((gridServiceDescription == null) ? 0 : gridServiceDescription.getId().hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
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
        if ( object == null ) {
            if ( other.object != null ) return false;
        } else if ( !object.equals( other.object ) ) return false;
        if ( transport == null ) {
            if ( other.transport != null ) return false;
        } else if ( !transport.equals( other.transport ) ) return false;
        return true;
    }

    public String toString() {
        return "Address id=" + id + " tranport=" + transport + " object=" + object;
    }

}

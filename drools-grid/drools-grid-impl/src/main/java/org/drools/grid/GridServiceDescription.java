package org.drools.grid;

import java.io.Serializable;
import java.util.Map;

import org.drools.grid.service.directory.Address;

public interface GridServiceDescription<T> {
    public String getId();

    public String getOwnerGridId();

    public void setOwnerGridId( String id );

    public Class<T> getServiceInterface();

    public void setServiceInterface( Class<T> cls );

    public Map<String, Address> getAddresses();

    public Address addAddress( String transport );

    public void removeAddress( String transport );

    public Serializable getData();

    public void setData( Serializable data );
    }

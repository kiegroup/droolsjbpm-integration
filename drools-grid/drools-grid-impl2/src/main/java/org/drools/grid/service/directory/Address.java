package org.drools.grid.service.directory;

import java.net.URL;

import javax.persistence.Entity;

import org.drools.grid.GridServiceDescription;

public interface Address {
    public GridServiceDescription getGridServiceDescription();
    
    public String getTransport();
    
    public Object getObject();
    public void setObject(Object object);
    
}

package org.drools.grid.service.directory;

import org.drools.grid.GridServiceDescription;

public interface Address {

    public GridServiceDescription getGridServiceDescription();

    public String getTransport();

    public Object getObject();

    public void setObject(Object object);

}

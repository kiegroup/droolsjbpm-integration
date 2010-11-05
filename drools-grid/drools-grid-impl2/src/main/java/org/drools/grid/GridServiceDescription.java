package org.drools.grid;

import java.io.Serializable;
import java.util.Map;


import org.drools.grid.service.directory.Address;

public interface GridServiceDescription {
    public String getId();

    public Class getServiceInterface();

    public void setServiceInterface(Class cls);
    
    public Class getImplementedClass();

    public void setImplementedClass(Class cls);

    public Map<String, Address> getAddresses();

    public Address addAddress(String transport);

    public void removeAddress(String transport);
    
    public Serializable getData();
    
    public void setData(Serializable data);
}

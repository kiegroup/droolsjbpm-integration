package org.drools.grid;

import java.util.Map;


import org.drools.grid.service.directory.Address;

public interface GridServiceDescription {
    public String getId();

    public Class getImplementedClass();

    public void setImplementedClass(Class cls);

    public Map<String, Address> getAddresses();

    public Address addAddress(String transport);

    public void removeAddress(String transport);
}

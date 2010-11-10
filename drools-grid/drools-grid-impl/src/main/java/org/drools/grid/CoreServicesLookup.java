package org.drools.grid;

import java.util.Map;

public interface CoreServicesLookup {
    public GridServiceDescription lookup(Class cls);

    public Map<String, GridServiceDescription> getServices();
}


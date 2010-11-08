package org.drools.grid;

import java.util.Map;

public interface CoreServicesWhitePages {
    public GridServiceDescription lookup(Class cls);

    public Map<String, GridServiceDescription> getServices();
}

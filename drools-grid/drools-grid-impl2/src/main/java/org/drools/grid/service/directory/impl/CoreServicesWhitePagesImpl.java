package org.drools.grid.service.directory.impl;

import java.util.Map;

import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.GridServiceDescription;

public class CoreServicesWhitePagesImpl implements CoreServicesWhitePages {
    private Map<String, GridServiceDescription> services;
    
    public CoreServicesWhitePagesImpl(Map<String, GridServiceDescription> services) {
        this.services = services;
    }
    
    public Map<String, GridServiceDescription> getServices() {
        return services;
    }

    public GridServiceDescription lookup(Class cls) {
        return this.services.get( cls.getName() );
    }

}

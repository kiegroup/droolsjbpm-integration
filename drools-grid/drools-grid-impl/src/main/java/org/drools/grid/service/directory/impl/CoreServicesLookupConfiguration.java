/**
 * 
 */
package org.drools.grid.service.directory.impl;

import java.util.Map;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;

public class CoreServicesLookupConfiguration
    implements
    GridPeerServiceConfiguration {
    private Map<String, GridServiceDescription> services;

    public CoreServicesLookupConfiguration(Map<String, GridServiceDescription> services) {
        this.services = services;
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( CoreServicesLookup.class,
                                      new CoreServicesLookupImpl( this.services ) );
    }
}
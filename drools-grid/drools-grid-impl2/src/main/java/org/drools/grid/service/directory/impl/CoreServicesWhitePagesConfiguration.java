/**
 * 
 */
package org.drools.grid.service.directory.impl;

import java.util.Map;

import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.impl.CoreServicesWhitePagesImpl;

public class CoreServicesWhitePagesConfiguration
    implements
    GridPeerServiceConfiguration {
    private Map<String, GridServiceDescription> services;

    public CoreServicesWhitePagesConfiguration(Map<String, GridServiceDescription> services) {
        this.services = services;
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( CoreServicesWhitePages.class,
                                      new CoreServicesWhitePagesImpl( this.services ) );
    }
}
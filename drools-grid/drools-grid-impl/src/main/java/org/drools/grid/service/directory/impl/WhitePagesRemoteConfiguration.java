/**
 * 
 */
package org.drools.grid.service.directory.impl;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesRemoteConfiguration
    implements
    GridPeerServiceConfiguration {
    ConversationManager cm;

    public WhitePagesRemoteConfiguration(ConversationManager cm) {
        this.cm = cm;
    }

    public void configureService(Grid grid) {
        CoreServicesWhitePagesImpl coreServices = (CoreServicesWhitePagesImpl) grid.get( CoreServicesLookup.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServices.lookup( WhitePages.class );

        WhitePages wp = new WhitePagesClient( grid,
                                              cm );
        ((GridImpl) grid).addService( WhitePages.class,
                                      wp );
    }
}
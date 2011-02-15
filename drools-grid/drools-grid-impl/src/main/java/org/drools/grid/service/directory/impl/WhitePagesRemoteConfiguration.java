/**
 * 
 */
package org.drools.grid.service.directory.impl;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesRemoteConfiguration
    implements
    GridPeerServiceConfiguration {

    public WhitePagesRemoteConfiguration() {
    }

    public void configureService(Grid grid) {
        WhitePages wp = new WhitePagesClient( grid );
        ((GridImpl) grid).addService( WhitePages.class,
                                      wp );
    }
}
/**
 * 
 */
package org.drools.grid.service.directory.impl;

import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesLocalConfiguration
    implements
    GridPeerServiceConfiguration {

    private WhitePages whitePages;

    public WhitePagesLocalConfiguration() {

    }

    public void setWhitePages(WhitePages whitePages) {
        this.whitePages = whitePages;
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( WhitePages.class,
                                      getWhitePages() );
    }
    
    public WhitePages getWhitePages() {
        if ( this.whitePages == null ) {
            this.whitePages = new WhitePagesImpl();
        }
        return this.whitePages;
    }

}
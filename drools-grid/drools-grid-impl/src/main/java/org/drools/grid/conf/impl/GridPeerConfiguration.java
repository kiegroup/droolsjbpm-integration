/**
 * 
 */
package org.drools.grid.conf.impl;

import java.util.ArrayList;
import java.util.List;

import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;

public class GridPeerConfiguration {
    private List<GridPeerServiceConfiguration> configurations;

    public GridPeerConfiguration() {
        this.configurations = new ArrayList<GridPeerServiceConfiguration>();
    }

    public void addConfiguration(GridPeerServiceConfiguration configuration) {
        this.configurations.add( configuration );
    }

    public void configure(Grid grid) {
        for ( GridPeerServiceConfiguration entry : this.configurations ) {
            entry.configureService( grid );
        }

    }
}
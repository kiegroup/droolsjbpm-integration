/**
 * 
 */
package org.drools.grid;

import java.util.ArrayList;
import java.util.List;

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
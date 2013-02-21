/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.drools.grid.conf.impl;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.GridNodeImpl;

public class GridNodeLocalConfiguration
    implements
    GridPeerServiceConfiguration {

    private GridNode gnode;

    public GridNodeLocalConfiguration() {
    }

    public void setGnode(GridNode gnode) {
        this.gnode = gnode;
    }

    public void configureService( Grid grid ) {
        GridNode gnode = (this.gnode != null) ? this.gnode : new GridNodeImpl( grid );
        ((GridImpl) grid).addService( GridNode.class,
                                      gnode );
    }

}

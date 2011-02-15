/*
 * Copyright 2010 JBoss Inc
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
 */

package org.drools.container.spring.beans;

import java.util.HashMap;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.SocketService;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

public class GridNodeBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private String   id;
    private Grid     grid;
    private GridNode node;

    private String   port;

    //
    public Object getObject() throws Exception {
        return node;
    }

    public Class< ? extends GridNode> getObjectType() {
        return GridNode.class;
    }

    //
    public boolean isSingleton() {
        return true;
    }

    //
    public void afterPropertiesSet() throws Exception {
        if ( grid == null ) {
            this.grid = new GridImpl( new HashMap<String, Object>() );
            ((GridImpl) this.grid).addService( WhitePages.class,
                                               new WhitePagesImpl() );
        }
        this.node = this.grid.createGridNode( id );

        if ( StringUtils.hasText( this.port ) ) {
            this.grid.get( SocketService.class ).addService( id,
                                                             Integer.parseInt( port ),
                                                             this.node );
        }
        //        connection.addExecutionNode(new LocalNodeConnector());
        //        connection.addDirectoryNode(new LocalDirectoryConnector());
        //        node = connection.getExecutionNode();
        //        node.setId( id );
    }

    //
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}

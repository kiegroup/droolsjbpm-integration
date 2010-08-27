/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.local;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.ConnectorType;
import org.drools.grid.DirectoryNodeLocalImpl;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.NodeConnectionType;

/**
 *
 * @author salaboy 
 */
public class LocalConnectionDirectory
    implements
    NodeConnectionType {
    private final Map<Class< ? >, Object> services  = new ConcurrentHashMap<Class< ? >, Object>();
    private static DirectoryNodeService   directory = new DirectoryNodeLocalImpl();

    public LocalConnectionDirectory() {
        this.services.put( DirectoryNodeService.class,
                           directory );

    }

    public Set<Class< ? >> getServicesKeys() {
        return this.services.keySet();
    }

    public <T> T getServiceImpl(Class<T> clazz) {
        return (T) this.services.get( clazz );
    }

    public void setConnector(GenericNodeConnector connector) {
        //do nothing, we don't need a connector here
    }

    public void setConnection(GenericConnection connection) {
        // do nothing, we don't need a connection here
    }

    public void init() {
        // do nothing
    }

    public ConnectorType getConnectorType() {
        return ConnectorType.LOCAL;
    }

}

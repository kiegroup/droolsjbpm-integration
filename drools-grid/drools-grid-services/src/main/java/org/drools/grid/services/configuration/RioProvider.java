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
package org.drools.grid.services.configuration;

import java.util.ArrayList;
import java.util.List;

import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.GenericConnectorFactory;
import org.drools.grid.GenericNodeConnector;

public class RioProvider
    implements
    GenericProvider {

    private List<ExecutionNodeService> executionNodes;
    private List<DirectoryNodeService> directoryNodes;

    public RioProvider() {
        this.executionNodes = new ArrayList<ExecutionNodeService>();
        this.directoryNodes = new ArrayList<DirectoryNodeService>();
    }

    public String getId() {
        return "RioProvider:";
    }

    public ProviderType getProviderType() {
        return ProviderType.DistributedRio;
    }

    public ExecutionNodeService getExecutionNode() {
        //Need a strategy
        return this.executionNodes.get( 0 );
    }

    public DirectoryNodeService getDirectoryNode() {
        //Need a strategy
        return this.directoryNodes.get( 0 );
    }

    public List<ExecutionNodeService> getExecutionNodes() {
        return this.executionNodes;
    }

    public List<DirectoryNodeService> getDirectoryNodes() {
        return this.directoryNodes;
    }

    public GenericNodeConnector getConnector(String connectorString) {

        return GenericConnectorFactory
                .newConnector( connectorString );

    }
}

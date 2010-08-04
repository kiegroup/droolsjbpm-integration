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

package org.drools.grid.services;

import java.util.List;

import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNode;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.strategies.NodeSelectionStrategy;
import org.drools.grid.strategies.ReturnAlwaysTheFirstSelectionStrategy;

/**
 *
 * @author salaboy
 */
public class ExecutionEnvironment  {

    private String name;
    private GenericNodeConnector  connector;


    public ExecutionEnvironment(String name, GenericNodeConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    public ExecutionNode getExecutionNode() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getExecutionNode(new ReturnAlwaysTheFirstSelectionStrategy());
    }

    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getExecutionNode(strategy);
    }

    public List<ExecutionNode> getExecutionNodes() throws ConnectorException{
        GenericConnection connection = getConnector().getConnection();
        return connection.getExecutionNodes();
    }

    public GenericNodeConnector getConnector(){
        return this.connector;
    }

    

    public String getName() {
        return name;
    }



    
}

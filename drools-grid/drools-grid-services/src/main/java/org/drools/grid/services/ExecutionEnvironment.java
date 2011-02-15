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
 *
 * This class represents a remote/distributed execution environment where we can
 * create and execute our knowledge (remote/distributed knowledge sessions). The concept of
 * ExecutionEnvironment encapsulate one or a set of executionNodes.
 * Depending on the underlaying implementation the executionEnvironment will be able to
 * create a connection to the remote/distributed ExecutionNode that we can use to
 * create and execute knowledge.
 */
public class ExecutionEnvironment {

    private String                name;
    private GenericNodeConnector  connector;
    private NodeSelectionStrategy defaultStrategy = new ReturnAlwaysTheFirstSelectionStrategy();

    /*
     * Creates a new ExecutionEnvironment using a name and a GenericNodeConnector
     */
    public ExecutionEnvironment(String name,
                                GenericNodeConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    /*
     * When we want a reference to a remote/distributed execution environment, we
     * ask for an ExecutionNode. Based on the default strategy this method will return
     * the selected execution node that can be used to create kbases, ksessions and execute
     * rules remotely.
     */
    public ExecutionNode getExecutionNode() throws ConnectorException {
        return getExecutionNode( this.defaultStrategy );
    }

    /*
     * Based on the provided NodeSelectionStrategy this method will choose one of the
     * ExecutionNodes available and it will create a connection to it.
     */
    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getExecutionNode( strategy );
    }

    /*
     * This method will create a connection to all the ExecutionNodes provided by this
     * ExecutionEnvironments. This can be expensive, but it's useful for monitoring purposes.
     */
    public List<ExecutionNode> getExecutionNodes() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getExecutionNodes();
    }

    /*
     * Return the ExecutionEnvironment connector
     */
    public GenericNodeConnector getConnector() {
        return this.connector;
    }

    /*
     * Return the ExecutionEnvironment Name
     */
    public String getName() {
        return this.name;
    }

}

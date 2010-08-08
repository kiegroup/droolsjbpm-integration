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
package org.drools.grid;

import java.util.List;
import org.drools.grid.strategies.NodeSelectionStrategy;



/**
 *
 * @author salaboy
 */
public interface GenericConnection {

    //Add Connectors to get new Connections
    public void addExecutionNode(GenericNodeConnector execNodeConnector);

    public void addDirectoryNode(GenericNodeConnector directoryNodeConnector);

    public void addHumanTaskNode(GenericNodeConnector humanTaskNodeConnector);

    //Get ExecutionNode(s) with live connections
    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) throws ConnectorException;

    public ExecutionNode getExecutionNode() throws ConnectorException;

    public List<ExecutionNode> getExecutionNodes() throws ConnectorException;

    //Get DirectoryNode(s) with live connections
    public DirectoryNode getDirectoryNode(NodeSelectionStrategy strategy) throws ConnectorException;

    public DirectoryNode getDirectoryNode() throws ConnectorException;

    public List<DirectoryNode> getDirectoryNodes() throws ConnectorException;

    //Get HumanTaskNode(s) with live connections
    public HumanTaskNode getHumanTaskNode(NodeSelectionStrategy strategy) throws ConnectorException;

    public HumanTaskNode getHumanTaskNode() throws ConnectorException;

    public List<HumanTaskNode> getHumanTaskNodes() throws ConnectorException;

    // Dispose all the live connections
    public void dispose() throws ConnectorException;


}

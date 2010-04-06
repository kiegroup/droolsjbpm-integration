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

package org.drools.grid.generic;

import java.util.List;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.NodeSelectionStrategy;
import org.drools.grid.strategies.DirectoryServiceSelectionStrategy;

/**
 *
 * @author salaboy
 */
public interface GenericConnection { 
    public void addNodeConnector(GenericNodeConnector nodeConnector);
    public void addDirectoryNode(DirectoryNodeService directory);
    public List<GenericNodeConnector> getNodeConnectors();
    public List<DirectoryNodeService> getDirectories();
    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy);
    public ExecutionNode getExecutionNode();
    public DirectoryNodeService getDirectoryNode(DirectoryServiceSelectionStrategy directorySelectionStrategy);

    


}

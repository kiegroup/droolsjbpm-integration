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
package org.drools.grid.distributed.strategies;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.strategies.NodeSelectionStrategy;

public class GetMinimalKsessionNodes implements NodeSelectionStrategy {



    public GenericNodeConnector getBestNode(List<GenericNodeConnector> connectors) {
         Double min = null;
        GenericNodeConnector selectedConnector = null;
        for (GenericNodeConnector nodeConnector : connectors) {
            try {
                Double currentCounter = null;
                try {
                    currentCounter = ((ExecutionNodeService) nodeConnector).getKsessionCounter();
                } catch (RemoteException ex) {
                    Logger.getLogger(GetMinimalKsessionNodes.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (min == null) {
                    min = currentCounter;
                    selectedConnector = nodeConnector;
                }
                if (min > currentCounter) {
                    min = currentCounter;
                    selectedConnector = nodeConnector;
                }
            } catch (ConnectorException ex) {
                Logger.getLogger(GetMinimalKsessionNodes.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        return selectedConnector;
    }
}

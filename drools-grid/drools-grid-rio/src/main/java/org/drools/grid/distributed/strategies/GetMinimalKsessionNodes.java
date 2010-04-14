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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.NodeSelectionStrategy;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;

/**
 *
 * @author salaboy
 */
public class GetMinimalKsessionNodes implements NodeSelectionStrategy {

    private GenericConnection connection;

    public GetMinimalKsessionNodes() {
    }

    @Override
    public GenericNodeConnector getBestNode() {
        List<GenericNodeConnector> nodeConnectors = this.connection.getNodeConnectors();
        Double min = null;
        GenericNodeConnector resultConnector = null;
        for (GenericNodeConnector nodeConnector : nodeConnectors) {
            try {
            System.out.println("Node Connector = " + nodeConnector.getId() + " - With Counter = "+ ((ExecutionNodeService) nodeConnector).getKsessionCounter());
        } catch (RemoteException ex) {
            Logger.getLogger(GetMinimalKsessionNodes.class.getName()).log(Level.SEVERE, null, ex);
        }
            Double currentCounter = null;
            try {
                currentCounter = ((ExecutionNodeService) nodeConnector).getKsessionCounter();

            } catch (RemoteException ex) {
                Logger.getLogger(GetMinimalKsessionNodes.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (min == null) {

                min = currentCounter;
                resultConnector = nodeConnector;

            }
            if (min > currentCounter) {
                min = currentCounter;
                resultConnector = nodeConnector;
            }

        }
        try {
            System.out.println("Result Connector = " + resultConnector.getId() + " - With Counter = "+ ((ExecutionNodeService) resultConnector).getKsessionCounter());
        } catch (RemoteException ex) {
            Logger.getLogger(GetMinimalKsessionNodes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultConnector;
    }

    public void setConnection(GenericConnection connection) {
        this.connection = connection;
    }
}

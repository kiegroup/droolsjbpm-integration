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

import java.util.concurrent.atomic.AtomicInteger;
import org.drools.grid.ConnectorException;
import org.drools.grid.ConnectorType;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.GridConnection;
import org.drools.grid.NodeConnectionType;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;

/**
 *
 * @author salaboy
 */
public class LocalNodeConnector implements GenericNodeConnector {

    private GridConnection connection = new GridConnection();;
    private NodeConnectionType connectionNode = new LocalConnectionNode(this, connection);
    public LocalNodeConnector() {
        
        
    }

    public void connect() throws ConnectorException {
        //do nothing
    }

    public void disconnect() throws ConnectorException {
        //do nothing, it's local!
    }

    public Message write(Message msg) throws ConnectorException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() throws ConnectorException {
        return "Local:";
    }

    public void write(Message msg, MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public GenericConnection getConnection() {
        return this.connection;
    }

   public NodeConnectionType getNodeConnectionType() throws ConnectorException {
        return this.connectionNode;
    }

    public ConnectorType getConnectorType() {
        return ConnectorType.LOCAL;
    }

    public int getSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AtomicInteger getCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

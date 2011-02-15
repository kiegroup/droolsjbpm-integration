/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.grid;

import java.rmi.RemoteException;

import org.drools.grid.internal.Message;

public interface ExecutionNodeService
    extends
    NodeService {

    public Message write(Message msg) throws ConnectorException,
                                     RemoteException;

    double getKsessionCounter() throws ConnectorException,
                               RemoteException;

    void incrementKsessionCounter() throws ConnectorException,
                                   RemoteException;

}

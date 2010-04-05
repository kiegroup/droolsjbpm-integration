/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.grid;

import java.rmi.RemoteException;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;


/**
 *
 * @author salaboy
 */
public interface ExecutionNodeService extends GenericNodeConnector{
    public String  getId() throws RemoteException;
    public Message write(Message msg) throws RemoteException;
    double getLoad() throws RemoteException;
    void setLoad(double load) throws RemoteException;
    
}

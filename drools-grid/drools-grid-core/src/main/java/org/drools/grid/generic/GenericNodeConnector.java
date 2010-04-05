package org.drools.grid.generic;

import java.rmi.RemoteException;

public interface GenericNodeConnector extends GenericIoWriter {

    boolean connect() throws RemoteException;;

    void disconnect() throws RemoteException;;

    Message write(Message msg) throws RemoteException;;

    String getId() throws RemoteException;;

   // void setSession(Object object) throws RemoteException;;
}

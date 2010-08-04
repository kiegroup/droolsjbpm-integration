package org.drools.grid;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.Message;

public interface GenericNodeConnector extends GenericIoWriter {

    void connect() throws ConnectorException, RemoteException;

    void disconnect() throws ConnectorException, RemoteException;

    Message write(Message msg) throws ConnectorException, RemoteException;

    String getId() throws ConnectorException, RemoteException;

    GenericConnection getConnection();

    NodeConnectionType getNodeConnectionType()  throws ConnectorException, RemoteException ;

    public ConnectorType getConnectorType() ;

    public int getSessionId();

    public AtomicInteger getCounter();

    

}

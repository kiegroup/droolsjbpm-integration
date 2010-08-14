package org.drools.grid.distributed.impl;

import java.io.IOException;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.SystemEventListener;
import org.drools.grid.ConnectorException;
import org.drools.grid.ConnectorType;
import org.drools.grid.GenericConnection;
import org.drools.grid.NodeConnectionType;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.GridConnection;
import org.drools.grid.distributed.DistributedConnectionNode;
import org.drools.grid.distributed.util.RioResourceLocator;

public class DistributedRioNodeConnector
        implements GenericNodeConnector {

    protected final String name;
    protected AtomicInteger counter;
    protected ExecutionNodeService executionNodeService;
    protected SocketAddress address;
    protected SystemEventListener eventListener;
    protected GenericConnection connection;
    protected String executionNodeId;

     public DistributedRioNodeConnector(String name,
            SystemEventListener eventListener) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.connection = new GridConnection();

    }



    public DistributedRioNodeConnector(String name,
            SystemEventListener eventListener,
            ExecutionNodeService executionNodeService) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.executionNodeService = executionNodeService;
        this.connection = new GridConnection();

    }

    public DistributedRioNodeConnector(String name,
            SystemEventListener eventListener,
            String executionNodeId) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.connection = new GridConnection();
        this.executionNodeId = executionNodeId;

    }

    
    public Message write(Message msg) throws ConnectorException, RemoteException {
        connect();
        if (executionNodeService != null) {


            Message returnMessage = this.executionNodeService.write(msg);
            return returnMessage;


        }
        throw new IllegalStateException("executionNode should not be null");
    }

    public void write(Message msg,
            MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException();
    }

    public String getId() throws ConnectorException, RemoteException {

        return executionNodeService.getId();


    }

    public void setExecutionNodeService(ExecutionNodeService executionNode) {
        this.executionNodeService = executionNode;


    }

    public ExecutionNodeService getExecutionNodeService() {
        return executionNodeService;
    }

    

    public void connect() throws ConnectorException {
        if(this.executionNodeService != null){
            return;
        }
        if( !this.executionNodeId.equals("")){
            try {
                this.executionNodeService = RioResourceLocator.locateExecutionNodeById(this.executionNodeId);
            } catch (IOException ex) {
                Logger.getLogger(DistributedRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            try {
                this.executionNodeService = RioResourceLocator.locateExecutionNodes().get(0);
            } catch (IOException ex) {
                Logger.getLogger(DistributedRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void disconnect() throws ConnectorException {
        //I don't need to be disconected
    }

    public GenericConnection getConnection() {
        return this.connection;
    }

    public NodeConnectionType getNodeConnectionType() throws ConnectorException {
        return new DistributedConnectionNode();
    }

    public ConnectorType getConnectorType() {
        return ConnectorType.DISTRIBUTED;
    }

    public int getSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AtomicInteger getCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}

package org.drools.grid.remote.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.ConnectorException;
import org.drools.grid.ConnectorType;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.NodeConnectionType;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.GridConnection;
import org.drools.grid.remote.RemoteConnectionNode;

public class RemoteMinaNodeConnector
        implements
        GenericNodeConnector {

    protected IoSession session;
    protected final String name;
    protected AtomicInteger counter;
    protected SocketConnector connector;
    protected SocketAddress address;
    protected SystemEventListener eventListener;
    protected GridConnection connection;

    public RemoteMinaNodeConnector(String name,
            String providerAddress, Integer providerPort,
            SystemEventListener eventListener) {

        SocketConnector minaconnector = new NioSocketConnector();
        minaconnector.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener()));
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.address = new InetSocketAddress(providerAddress, providerPort);
        this.connector = minaconnector;
        this.eventListener = eventListener;
        this.connection = new GridConnection();
    }

    public void connect() throws ConnectorException {
        if (session != null && session.isConnected()) {
            return;
            //throw new IllegalStateException("Already connected. Disconnect first.");
        }

        try {
            this.connector.getFilterChain().addLast(this.name+"codec",
                    new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

            ConnectFuture future1 = this.connector.connect(this.address);
            future1.await(2000);
            if (!future1.isConnected()) {
                eventListener.info("unable to connect : " + address + " : " + future1.getException());
                Logger.getLogger(RemoteMinaNodeConnector.class.getName()).log(Level.SEVERE, null, "The Node Connection Failed!");
                throw new ConnectorException("unable to connect : " + address + " : " + future1.getException());
            }
            eventListener.info("connected : " + address);
            this.session = future1.getSession();
        } catch (Exception e) {
             throw new ConnectorException(e);
        }
    }

    public void disconnect() throws ConnectorException {
        this.connector.getFilterChain().clear();
        if (session != null && session.isConnected()) {
            session.close(false);
            session.getCloseFuture().join();
        }
        //this.connector.dispose();
    }

    private void addResponseHandler(int id,
            MessageResponseHandler responseHandler) {
        ((MinaIoHandler) this.connector.getHandler()).addResponseHandler(id,
                responseHandler);
    }

    public void write(Message msg,
            MessageResponseHandler responseHandler) {
        if (responseHandler != null) {
            addResponseHandler(msg.getResponseId(),
                    responseHandler);
        }
        this.session.write(msg);
    }

    public Message write(Message msg) throws ConnectorException {
        BlockingMessageResponseHandler responseHandler = new BlockingMessageResponseHandler();

        if (responseHandler != null) {
            addResponseHandler(msg.getResponseId(),
                    responseHandler);
        }
        this.session.write(msg);

        Message returnMessage = responseHandler.getMessage();
        if (responseHandler.getError() != null) {
            throw responseHandler.getError();
        }

        return returnMessage;
    }

    public String getId() {
        String hostName = ((InetSocketAddress) this.address).getHostName();
        int hostPort = ((InetSocketAddress) this.address).getPort();
        return "Mina:" + this.name + ":" + hostName + ":" + hostPort;
    }


    public GenericConnection getConnection() {
        return this.connection;
    }


    public NodeConnectionType getNodeConnectionType() throws ConnectorException{
        return new RemoteConnectionNode();
    }

    public ConnectorType getConnectorType() {
        return ConnectorType.REMOTE;
    }

    public int getSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AtomicInteger getCounter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

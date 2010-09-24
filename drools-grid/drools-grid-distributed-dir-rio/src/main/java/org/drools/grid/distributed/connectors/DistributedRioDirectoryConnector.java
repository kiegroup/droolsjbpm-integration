package org.drools.grid.distributed.connectors;

import java.io.IOException;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.KnowledgeBase;

import org.drools.SystemEventListener;
import org.drools.distributed.directory.impl.DistributedConnectionDirectory;
import org.drools.grid.ConnectorException;
import org.drools.grid.ConnectorType;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.NodeConnectionType;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.GridConnection;
import org.drools.grid.distributed.util.RioResourceLocator;

public class DistributedRioDirectoryConnector
        implements GenericNodeConnector, DirectoryNodeService {

    protected final String name;
    protected AtomicInteger counter;
    protected DirectoryNodeService directoryNodeService;
    protected SocketAddress address;
    protected SystemEventListener eventListener;
    protected GenericConnection connection;
    private String directoryNodeServiceId;

    public DistributedRioDirectoryConnector(String name,
            SystemEventListener eventListener) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.connection = new GridConnection();

    }

    public DistributedRioDirectoryConnector(String name,
            SystemEventListener eventListener,
            DirectoryNodeService directoryNode) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.directoryNodeService = directoryNode;
        this.connection = new GridConnection();

    }

    public DistributedRioDirectoryConnector(String name,
            SystemEventListener eventListener, String directoryNodeServiceId) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.directoryNodeServiceId = directoryNodeServiceId;
        this.connection = new GridConnection();

    }

    @Override
    public Message write(Message msg) throws ConnectorException, RemoteException {
//        if (directoryNodeService != null) {
//
//
//            Message returnMessage = this.directoryNodeService.write(msg);
//            return returnMessage;
//
//
//        }
//        throw new IllegalStateException("directoryService should not be null");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(Message msg,
            MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() throws ConnectorException, RemoteException {

        return directoryNodeService.getId();


    }

    public void setDirectoryNodeService(DirectoryNodeService directoryNodeService) {
        this.directoryNodeService = directoryNodeService;


    }

    public DirectoryNodeService getDirectoryNodeService() {
        return directoryNodeService;
    }

    public void connect() throws ConnectorException {
        if(this.directoryNodeService == null && this.directoryNodeServiceId != null && !"".equals(this.directoryNodeServiceId)){
            try {
                this.directoryNodeService = RioResourceLocator.locateDirectoryNodeById(this.directoryNodeServiceId);
            } catch (IOException ex) {
                Logger.getLogger(DistributedRioDirectoryConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedRioDirectoryConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            try {
                this.directoryNodeService = RioResourceLocator.locateDirectoryNodes().get(0);
            } catch (IOException ex) {
                Logger.getLogger(DistributedRioDirectoryConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedRioDirectoryConnector.class.getName()).log(Level.SEVERE, null, ex);
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
        return new DistributedConnectionDirectory();
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

    public void register(String executorId, String resourceId) throws ConnectorException, RemoteException {
        this.directoryNodeService.register(executorId, resourceId);
    }

    public void register(String executorId, GenericNodeConnector resourceConnector) throws ConnectorException, RemoteException {
        this.directoryNodeService.register(executorId, resourceConnector);
    }

    public void unregister(String executorId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericNodeConnector lookup(String resourceId) throws ConnectorException, RemoteException {
        return this.directoryNodeService.lookup(resourceId);
    }

    public String lookupId(String resourceId) throws ConnectorException, RemoteException {
        return this.directoryNodeService.lookupId(resourceId);
    }

    public void registerKBase(String kbaseId, String resourceId) throws ConnectorException, RemoteException {
        this.directoryNodeService.registerKBase(kbaseId, resourceId);
    }

    public void registerKBase(String kbaseId, KnowledgeBase kbase) throws ConnectorException, RemoteException {
        this.directoryNodeService.registerKBase(kbaseId, kbase);
    }

    public void unregisterKBase(String kbaseId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase lookupKBase(String kbaseId) throws ConnectorException, RemoteException {
        return this.directoryNodeService.lookupKBase(kbaseId);
    }

    public Map<String, String> getExecutorsMap() throws ConnectorException, RemoteException {
        return this.directoryNodeService.getExecutorsMap();
    }

    public Map<String, String> getKBasesMap() throws ConnectorException, RemoteException {
        return this.directoryNodeService.getKBasesMap();
    }

    public void dispose() throws ConnectorException, RemoteException {
        this.directoryNodeService.dispose();
    }

    @Override
    public ServiceType getServiceType() throws ConnectorException, RemoteException {
        return ServiceType.DISTRIBUTED;
    }

    public String lookupKBaseLocationId(String kbaseId) throws ConnectorException, RemoteException {
        return this.directoryNodeService.lookupKBaseLocationId(kbaseId);
    }
}

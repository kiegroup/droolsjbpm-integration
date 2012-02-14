package org.drools.grid.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.io.ConnectorFactoryService;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.remote.mina.MinaConnectorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridImpl implements Grid {

    private String id;
    private Map<String, Object> services;
    private Map<String, GridNode> localNodes = new HashMap<String, GridNode>();
    private Map<String, GridPeerServiceConfiguration> serviceConfigurators = new HashMap();
    private static Logger logger = LoggerFactory.getLogger(GridImpl.class);

    public GridImpl() {
        this(UUID.randomUUID().toString(), null);
    }

    public GridImpl(String id) {
        this(id, null);
    }

    public GridImpl(Map<String, Object> services) {
        this(UUID.randomUUID().toString(), services);
    }

    public GridImpl(String id, Map<String, Object> services) {
        if (services == null) {
            this.services = new ConcurrentHashMap<String, Object>();
        } else {
            this.services = services;
        }

        this.id = id;
        init();
    }

    private void init() {
        // TODO hardcoding these for now, should probably be configured
        SystemEventListener listener = SystemEventListenerFactory.getSystemEventListener();
        this.services.put(SystemEventListener.class.getName(), listener);
        this.services.put(AcceptorFactoryService.class.getName(), new MinaAcceptorFactoryService());
        this.services.put(ConnectorFactoryService.class.getName(), new MinaConnectorFactoryService());
        this.services.put(ConversationManager.class.getName(), new ConversationManagerImpl(this, listener));

        ConnectionFactoryService conn = new ConnectionFactoryServiceImpl(this);
        this.services.put(ConnectionFactoryService.class.getName(), conn);

        this.serviceConfigurators.put(WhitePages.class.getName(), new WhitePagesRemoteConfiguration());
    }

    public Object get(String str) {
        return this.services.get(str);
    }

    public <T> T get(Class<T> serviceClass) {
        T service = (T) this.services.get(serviceClass.getName());

        if (service == null) {
            // If the service does not exist, it'll lazily create it
            GridPeerServiceConfiguration configurator = this.serviceConfigurators.get(serviceClass.getName());
            if (configurator != null) {
                configurator.configureService(this);
                service = (T) this.services.get(serviceClass.getName());
            }
        }
        return service;
    }

    public void addService(Class cls,
            Object service) {
        addService(cls.getName(),
                service);
    }

    public void addService(String id,
            Object service) {
        this.services.put(id,
                service);
    }

    public GridNode createGridNode(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug(" ### GridImpl: Registering in white pages (grid = " + getId() + ") new node = " + id);
        }
        WhitePages wp = get(WhitePages.class);
        GridServiceDescription gsd = wp.create(id );
        gsd.setServiceInterface(GridNode.class);
        GridNode node = new GridNodeImpl(id);
        this.localNodes.put(id , node);
        return node;
    }

    public void removeGridNode(String id) {
        WhitePages wp = get(WhitePages.class);
        wp.remove(id);
        this.localNodes.remove(id);
    }

    public GridNode getGridNode(String id) {
        return this.localNodes.get(id);
    }

    public String getId() {
        return id;
    }
}

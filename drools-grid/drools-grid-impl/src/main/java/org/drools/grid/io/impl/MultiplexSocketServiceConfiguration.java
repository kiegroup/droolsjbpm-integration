package org.drools.grid.io.impl;

import java.util.ArrayList;
import java.util.List;

import org.drools.grid.Grid;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;

public class MultiplexSocketServiceConfiguration
    implements
    GridPeerServiceConfiguration {
    private SocketService     service;

    private List<SocketEntry> services;

    public MultiplexSocketServiceConfiguration(SocketService service) {
        this.service = service;
        this.services = new ArrayList<SocketEntry>();
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( SocketService.class,
                                      service );
        for ( SocketEntry entry : services ) {
            this.service.addService( entry.getId(),
                                     entry.getPort(),
                                     (MessageReceiverHandlerFactoryService) entry.getObject() );
        }
    }

    public void addService(String id,
                           Object object,
                           int port) {
        this.services.add( new SocketEntry( id,
                                            object,
                                            port ) );
    }

    public static class SocketEntry {
        private String id;
        private Object object;
        private int    port;

        public SocketEntry(String id,
                           Object object,
                           int port) {
            this.id = id;
            this.object = object;
            this.port = port;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

}

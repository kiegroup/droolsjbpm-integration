package org.drools.grid.service.directory.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesImpl implements WhitePages, MessageReceiverHandlerFactoryService {
    private Map<String, GridServiceDescription> directory = new ConcurrentHashMap<String, GridServiceDescription>();

    public GridServiceDescription create(String serviceDescriptionId) {
        GridServiceDescription gsd = new GridServiceDescriptionImpl( serviceDescriptionId );
        this.directory.put( serviceDescriptionId, gsd );
        return gsd;
    }

    public GridServiceDescription lookup(String serviceDescriptionId) {
        return this.directory.get( serviceDescriptionId );
    }

    public void remove(String serviceDescriptionId) {
        this.directory.remove( serviceDescriptionId );
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new WhitePagesServer( this );
    }

}
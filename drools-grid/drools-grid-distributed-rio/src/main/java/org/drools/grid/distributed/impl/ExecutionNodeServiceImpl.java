/*
 *  Copyright 2009 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.grid.distributed.impl;

import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.ConnectorException;
import org.drools.grid.internal.BlockingGenericIoWriter;
import org.drools.grid.internal.GenericMessageHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.internal.NodeData;
import org.drools.grid.distributed.GenericMessageGridHandlerImpl;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.distributed.util.IDEntry;
import org.rioproject.core.jsb.ServiceBeanContext;
import org.rioproject.watch.CounterWatch;

 public class ExecutionNodeServiceImpl implements ExecutionNodeService {

    private GenericMessageHandler handler;
    private String id;
   
    private CounterWatch ksessionCounter; 
    private Long instanceID;

    public ExecutionNodeServiceImpl() {
        this.id = "Distributed:Rio:Node:" + UUID.randomUUID().toString();
        handler = new GenericMessageGridHandlerImpl(new NodeData(), SystemEventListenerFactory.getSystemEventListener());
    }

    public ExecutionNodeServiceImpl(NodeData data,
            SystemEventListener systemEventListener) {
        handler = new GenericMessageGridHandlerImpl(data, systemEventListener);
    }

    @Override
    public Message write(Message msg) throws ConnectorException, RemoteException {
        BlockingGenericIoWriter blockingWriter = new BlockingGenericIoWriter();
        try {
            handler.messageReceived(blockingWriter, msg);
        } catch (Exception ex) {
            Logger.getLogger(ExecutionNodeServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return blockingWriter.getMessage();
    }

    public GenericMessageHandler getGenericMessageHandler() {
        return this.handler;
    }

    
    public void write(Message msg,
            MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException();
    }

    public void setServiceBeanContext(ServiceBeanContext context) {
        context.addAttribute(new IDEntry(this.id));
        ksessionCounter = new CounterWatch("ksessionCounter");
        context.getWatchRegistry().register(ksessionCounter);
        this.instanceID = context.getServiceBeanConfig().getInstanceID();
    }

    @Override
    public String getId() {
        return id;
    }

   

   

    @Override
    public double getKsessionCounter() throws ConnectorException, RemoteException {
        return ksessionCounter.getLastCalculableValue();
    }

    @Override
    public void incrementKsessionCounter() throws ConnectorException, RemoteException {
        double last = ksessionCounter.getLastCalculableValue();
        ksessionCounter.increment();


    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.DISTRIBUTED;
    }

   

    

  
}

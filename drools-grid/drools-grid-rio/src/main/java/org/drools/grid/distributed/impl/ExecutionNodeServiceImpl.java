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

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.generic.BlockingGenericIoWriter;
import org.drools.grid.generic.GenericMessageHandler;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageResponseHandler;
import org.drools.grid.generic.NodeData;
import org.drools.grid.distributed.GenericMessageGridHandlerImpl;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.distributed.util.IDEntry;

import org.rioproject.core.jsb.ServiceBeanContext;
import org.rioproject.watch.GaugeWatch;

/**
 *
 * @author salaboy
 */
public class ExecutionNodeServiceImpl implements ExecutionNodeService {

    private GenericMessageHandler handler;
    private String id;
    private GaugeWatch loadWatch;
    private Long instanceID;

    public ExecutionNodeServiceImpl() {
        this.id = UUID.randomUUID().toString();
        handler = new GenericMessageGridHandlerImpl(new NodeData(), SystemEventListenerFactory.getSystemEventListener());
    }

    public ExecutionNodeServiceImpl(NodeData data,
            SystemEventListener systemEventListener) {
        handler = new GenericMessageGridHandlerImpl(data, systemEventListener); 
    }

    public Message write(Message msg) throws RemoteException {
        BlockingGenericIoWriter blockingWriter = new BlockingGenericIoWriter();
        try {
            handler.messageReceived(blockingWriter, msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
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
        loadWatch = new GaugeWatch("load");
        context.getWatchRegistry().register(loadWatch);
        instanceID = context.getServiceBeanConfig().getInstanceID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean connect() throws RemoteException {
        //this is always connected if the lookup was successful
        return true;
    }

    @Override
    public void disconnect() throws RemoteException {
        //we don't need to desconnect the grid
    }

    @Override
    public double getLoad() throws RemoteException {
        return loadWatch.getLastCalculableValue();
    }

    @Override
    public void setLoad(double load) throws RemoteException {
        double last = loadWatch.getLastCalculableValue();
        loadWatch.addValue(load);
        boolean verified = loadWatch.getLastCalculableValue() == load;
        if (!verified)
            System.err.println(System.currentTimeMillis() + " "+
                "---> ["+instanceID+"] was [" + loadWatch.getLastCalculableValue() +
                "], SET FAILED [" + load + "] " +
                "breached=" +
                loadWatch.getThresholdManager().getThresholdCrossed());
        else
            System.err.println(System.currentTimeMillis() + " "+
                "---> ["+instanceID+"] Load now [" + load + "] " +
                "breached=" +
                loadWatch.getThresholdManager().getThresholdCrossed());
    }

  
}

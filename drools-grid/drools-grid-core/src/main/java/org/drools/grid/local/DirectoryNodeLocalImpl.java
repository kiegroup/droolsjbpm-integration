/*
 *  Copyright 2010 salaboy.
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

package org.drools.grid.local;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.generic.GenericNodeConnector;

/**
 *
 * @author salaboy
 */
public class DirectoryNodeLocalImpl implements DirectoryNodeService {
    private String id;
    private Map<String, String> directoryMap = new HashMap<String, String>();
    private List<GenericNodeConnector> services = new ArrayList<GenericNodeConnector>();

    public DirectoryNodeLocalImpl() {
        this.id = UUID.randomUUID().toString();
    }


     public String getId() throws RemoteException {
        return this.id;
    }

    public void register(String executorId, String sessionServiceId) throws RemoteException {
        System.out.println("Registering: "+  executorId + " -- "+sessionServiceId);
        directoryMap.put(executorId, sessionServiceId);
    }

    public GenericNodeConnector lookup(String executorId) throws RemoteException {
        GenericNodeConnector sessionService = null;
        String sessionServiceId = (String)directoryMap.get(executorId);
        System.out.println("Registry = "+ directoryMap.toString());
        System.out.println("Nodes Services = "+services);
        for(GenericNodeConnector ss : services){
            System.out.println("Session Service id = "+ss.getId() + "needs to match with ="+sessionServiceId);
            if(ss.getId().equals(sessionServiceId)){
                sessionService = ss;
            }
        }

        return sessionService;
    }

    public void registerKBase(String kbaseId, String sessionServiceId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase lookupKBase(String kbaseId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public void addService(GenericNodeConnector service){
        services.add(service);
    }

    public Map<String, String> getDirectoryMap() {
        return directoryMap;
    }



}

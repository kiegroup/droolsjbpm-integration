/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.grid;

import java.rmi.RemoteException;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.grid.generic.GenericNodeConnector;


/**
 *
 * @author salaboy
 */

public interface DirectoryNodeService {
    public void register(String executorId, String nodeServiceId) throws RemoteException;
    public GenericNodeConnector lookup(String executorId) throws RemoteException;
    public void registerKBase(String kbaseId, String nodeServiceId) throws RemoteException;
    public KnowledgeBase lookupKBase(String kbaseId) throws RemoteException;
    public void addService(GenericNodeConnector service);
    public Map<String, String> getDirectoryMap() throws RemoteException;
    public String getId() throws RemoteException;
}

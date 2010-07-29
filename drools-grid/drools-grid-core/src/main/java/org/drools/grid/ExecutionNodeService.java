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
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;


/**
 *
 * @author salaboy
 */
public interface ExecutionNodeService extends GenericNodeConnector{
    public String  getId() throws RemoteException;
    public Message write(Message msg) throws RemoteException;
    double getLoad() throws RemoteException;
    void setLoad(double load) throws RemoteException;
    double getKsessionCounter() throws RemoteException;
    void incrementKsessionCounter() throws RemoteException;
    
}

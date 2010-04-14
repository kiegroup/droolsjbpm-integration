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
package org.drools.grid.remote;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.command.FinishedCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.command.LookupCommand;
import org.drools.grid.command.RegisterCommand;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;

/**
 *
 * @author salaboy
 */
public class DirectoryLookupProviderRemoteClient implements DirectoryLookupFactoryService {

    //The current node connector

    private GenericNodeConnector client;
   
   
    private GenericConnection connection;


    private MessageSession messageSession;

    public DirectoryLookupProviderRemoteClient(GenericNodeConnector client, GenericConnection connection) {
        this.client = client;
        this.messageSession = new MessageSession();
        this.connection = connection;
    }

    public void register(String identifier,
            CommandExecutor executor)  {
        try {
            String commandId = "client.lookup" + messageSession.getNextId();
            String kresultsId = "kresults_" + messageSession.getSessionId();
            int type;
            if (executor instanceof StatefulKnowledgeSession) {
                type = 0;
            } else {
                throw new IllegalArgumentException("Type is not supported for registration");
            }
            Message msg = new Message(messageSession.getSessionId(), messageSession.getCounter().incrementAndGet(), false, new KnowledgeContextResolveFromContextCommand(new RegisterCommand(identifier, ((StatefulKnowledgeSessionRemoteClient) executor).getInstanceId(), type), null, null, null, null));


            //System.out.println("Registering " + identifier + " - - " + client.getId());
            connection.getDirectoryNode(null).register(identifier, client.getId());
            try {
                Object object = client.write(msg).getPayload();
                if (!(object instanceof FinishedCommand)) {
                    throw new RuntimeException("Response was not correctly ended");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute message", e);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(DirectoryLookupProviderRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public CommandExecutor lookup(String identifier) {

        try {
            String commandId = "client.lookup" + messageSession.getNextId();
            String kresultsId = "kresults_" + messageSession.getSessionId();
            Message msg = new Message(messageSession.getSessionId(), messageSession.getCounter().incrementAndGet(), false, new KnowledgeContextResolveFromContextCommand(new LookupCommand(identifier, commandId), null, null, null, kresultsId));
            //System.out.println("Looking up the session with identifier = " + identifier);
            client = connection.getDirectoryNode(null).lookup(identifier);
            try {
                Object object = client.write(msg).getPayload();
                if (object == null) {
                    throw new RuntimeException("Response was not correctly received");
                }
                String value = (String) ((ExecutionResults) object).getValue(commandId);
                String type = String.valueOf(value.charAt(0));
                String instanceId = value.substring(2);
                CommandExecutor executor = null;
                switch (Integer.parseInt(type)) {
                    case 0:
                        {
                            executor = new StatefulKnowledgeSessionRemoteClient(instanceId, client, messageSession);
                            break;
                        }
                    default:
                        {
                        }
                }
                return executor;
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute message", e);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(DirectoryLookupProviderRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Map<String, Map<String, String>> getDirectoryMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   

   
   
   


   
    
    
}

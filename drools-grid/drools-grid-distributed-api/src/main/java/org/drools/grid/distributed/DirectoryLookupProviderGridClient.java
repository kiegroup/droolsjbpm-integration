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
package org.drools.grid.distributed;

import java.rmi.RemoteException;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.builder.DirectoryLookupFactoryService;
//import org.drools.command.FinishedCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.DirectoryNode;
import org.drools.grid.GenericConnection;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;
import org.drools.grid.internal.commands.LookupCommand;
import org.drools.grid.internal.commands.RegisterCommand;
import org.drools.grid.GenericConnectorFactory;

/**
 *
 * @author salaboy
 */
public class DirectoryLookupProviderGridClient implements DirectoryLookupFactoryService {

    private GenericConnection connection;
    private GenericNodeConnector currentConnector;
    private MessageSession messageSession;

    public DirectoryLookupProviderGridClient(GenericNodeConnector connector, GenericConnection connection) {
        this.currentConnector = connector;
        this.connection = connection;
        this.messageSession = new MessageSession();
    }

    public void register(String identifier,
            CommandExecutor executor) {

        try {
            String commandId = "client.lookup" + messageSession.getNextId();
            String kresultsId = "kresults_" + messageSession.getSessionId();
            int type;
            if (executor instanceof StatefulKnowledgeSession) {
                type = 0;
            } else {
                throw new IllegalArgumentException("Type is not supported for registration");
            }
            Message msg = new Message(messageSession.getSessionId(), 
                                            messageSession.getCounter().incrementAndGet(),
                                            false, new KnowledgeContextResolveFromContextCommand(
                                                    new RegisterCommand(identifier, ((StatefulKnowledgeSessionGridClient) executor)
                                                                        .getInstanceId(), type), null, null, null, null));

            for (DirectoryNode directory : connection.getDirectoryNodes()) {
                try {
                    try {

                        directory.get(DirectoryNodeService.class).register(identifier, currentConnector.getId());
                    } catch (RemoteException ex) {
                        Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (ConnectorException ex) {
                    Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Object object = currentConnector.write(msg).getPayload();
//                if (!(object instanceof FinishedCommand)) {
//                    throw new RuntimeException("Response was not correctly ended");
//                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute message", e);
            }
        } catch (ConnectorException ex) {
            Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public CommandExecutor lookup(String identifier) {

        CommandExecutor executor = null;
        try {
            String commandId = "client.lookup" + messageSession.getNextId();
            String kresultsId = "kresults_" + messageSession.getSessionId();
            Message msg = new Message(messageSession.getSessionId(), messageSession.getCounter().incrementAndGet(),
                    false, new KnowledgeContextResolveFromContextCommand(
                    new LookupCommand(identifier, commandId), null, null, null, kresultsId));

            //First I need to get the correct client ExecutionNodeService with the identifier
            //Look in all the DirectoryNodes

            for (DirectoryNode directory : connection.getDirectoryNodes()) {

                try {


                    String connectorString = directory.get(DirectoryNodeService.class).lookupId(identifier);

                    currentConnector = GenericConnectorFactory.newConnector(connectorString);

                    currentConnector.connect();


                    if (currentConnector != null) {
                        break;
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ConnectorException ex) {
                    Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Object object = currentConnector.write(msg).getPayload();
                if (object == null) {
                    throw new RuntimeException("Response was not correctly received");
                }
                String value = (String) object;
                String type = String.valueOf(value.charAt(0));
                String instanceId = value.substring(2);

                switch (Integer.parseInt(type)) {
                    case 0: {
                        executor = new StatefulKnowledgeSessionGridClient(instanceId, currentConnector, messageSession);
                        break;
                    }
                    default: {
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Unable to execute message", e);
            }
        } //    public Map<String, Map<String, String>> getDirectoryMap(){
        catch (ConnectorException ex) {
            Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return executor;
    }


    public Map<String, String> getDirectoryMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unregister(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

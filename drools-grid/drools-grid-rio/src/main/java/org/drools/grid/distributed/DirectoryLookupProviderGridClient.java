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

import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNodeService;
import java.rmi.RemoteException;
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
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;

/**
 *
 * @author salaboy
 */
public class DirectoryLookupProviderGridClient implements DirectoryLookupFactoryService {

    private GridConnection gridClient;
    private GenericNodeConnector currentService;
    private MessageSession messageSession;

  

    public DirectoryLookupProviderGridClient(GenericNodeConnector currentService, GridConnection gridClient) {
        this.currentService = currentService;
        this.gridClient = gridClient;
        this.messageSession = new MessageSession();
    }

    public void register(String identifier,
                         CommandExecutor executor) {
        try {
            String commandId = "client.lookup" + messageSession.getNextId();
            String kresultsId = "kresults_" + messageSession.getSessionId();
            int type;


            if ( executor instanceof StatefulKnowledgeSession ) {
                type = 0;
            } else {
                throw new IllegalArgumentException("Type is not supported for registration");
            }
            Message msg = new Message(messageSession.getSessionId(), messageSession.getCounter().incrementAndGet(), false, new KnowledgeContextResolveFromContextCommand(new RegisterCommand(identifier, ((StatefulKnowledgeSessionGridClient) executor).getInstanceId(), type), null, null, null, null));
            System.out.println("Registering " + identifier + " - - " + currentService.getId());
            try {
               // DirectoryNodeService directory = (DirectoryNodeService) gridClient.getDirectories().iterator().next();
                for(DirectoryNodeService directory : gridClient.getDirectories() ){

                        directory.register(identifier, currentService.getId());
                    
                }
            } catch (RemoteException ex) {
                Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Object object = currentService.write(msg).getPayload();
                if (!(object instanceof FinishedCommand)) {
                    throw new RuntimeException("Response was not correctly ended");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to execute message", e);
            }
        } catch ( RemoteException ex ) {
            Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public CommandExecutor lookup(String identifier) {
        String commandId = "client.lookup" + messageSession.getNextId();
        String kresultsId = "kresults_" + messageSession.getSessionId();

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.getCounter().incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new LookupCommand( identifier,
                                                                                                     commandId ),
                                                                                  null,
                                                                                  null,
                                                                                  null,
                                                                                  kresultsId ) );
        System.out.println("Looking up the session with identifier = "+identifier);
            try {
            //First I need to get the correct client ExecutionNodeService with the identifier
            //Look in all the DirectoryNodes
            //DirectoryNodeService directory = (DirectoryNodeService) gridClient.getDirectories().iterator().next();
            
             for(DirectoryNodeService directory : gridClient.getDirectories() )  {
                currentService = directory.lookup(identifier);
             }
            
        } catch (RemoteException ex) {
            Logger.getLogger(DirectoryLookupProviderGridClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Object object = currentService.write( msg ).getPayload();

            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            String value = (String) ((ExecutionResults) object).getValue( commandId );
            String type = String.valueOf( value.charAt( 0 ) );
            String instanceId = value.substring( 2 );

            CommandExecutor executor = null;
            switch ( Integer.parseInt( type ) ) {
                case 0 : {
                    executor = new StatefulKnowledgeSessionGridClient( instanceId, currentService, messageSession );
                    break;
                }
                default : {

                }

            }

            return executor;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

}

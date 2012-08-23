/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid.remote;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SessionConfiguration;
import org.drools.command.SetVariableCommandFromCommand;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.commands.KnowledgeBaseConfigurationRemoteCommands;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnowledgeBaseProviderRemoteClient
    implements
    KnowledgeBaseFactoryService {

    private static Logger logger = LoggerFactory.getLogger(KnowledgeBaseProviderRemoteClient.class);
    
    private Grid                             grid;
    private GridServiceDescription<GridNode> gsd;

    public KnowledgeBaseProviderRemoteClient(Grid grid,
                                             GridServiceDescription gsd) {
        this.grid = grid;
        this.gsd = gsd;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        
        String localId = UUID.randomUUID().toString();
        logger.info("This InstanceId (just generated) = "+localId);
        CommandImpl cmd = new CommandImpl("execute", Arrays.asList(
            new Object[]{
                new KnowledgeBaseConfigurationRemoteCommands.NewKnowledgeBaseConfigurationRemoteCommand(localId)
            }
        ));
        
        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBaseConfigurationRemoteClient(localId, grid, gsd);
        
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        return new SessionConfiguration();
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( "",
                                 null );

    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        return newKnowledgeBase( kbaseId,
                                 null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        return newKnowledgeBase( null,
                                 conf );
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        String localId = "";
        if ( kbaseId == null || kbaseId.equals( "" ) ) {
            localId = UUID.randomUUID().toString();
        } else {
            localId = kbaseId;
        }
        
        String kbaseConfId = ((KnowledgeBaseConfigurationRemoteClient)conf).getId();
        

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommandFromCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new NewKnowledgeBaseRemoteCommand( kbaseConfId ) )} ) );
        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBaseRemoteClient( localId,
                                              this.gsd,
                                              connm,
                                              (KnowledgeBaseConfigurationRemoteClient)conf);
    }

    public Environment newEnvironment() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}

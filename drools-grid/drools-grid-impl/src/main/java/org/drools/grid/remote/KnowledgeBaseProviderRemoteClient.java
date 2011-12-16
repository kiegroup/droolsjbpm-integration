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
import org.drools.RuleBaseConfiguration;
import org.drools.SessionConfiguration;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.SetVariableCommandFromCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;

public class KnowledgeBaseProviderRemoteClient
    implements
    KnowledgeBaseFactoryService {

    private Grid                             grid;
    private GridServiceDescription<GridNode> gsd;

    public KnowledgeBaseProviderRemoteClient(Grid grid,
                                             GridServiceDescription gsd) {
        this.grid = grid;
        this.gsd = gsd;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        return new RuleBaseConfiguration();
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

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommandFromCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new NewKnowledgeBaseCommand( conf ) )} ) );
        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBaseRemoteClient( localId,
                                              this.gsd,
                                              connm );
    }

    public Environment newEnvironment() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}

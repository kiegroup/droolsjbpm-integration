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

import com.sun.tools.xjc.Options;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.builder.*;
import org.drools.command.SetVariableCommandFromCommand;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.commands.KnowledgeBuilderConfigurationRemoteCommands;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnowledgeBuilderProviderRemoteClient
    implements
    KnowledgeBuilderFactoryService {

    private static Logger logger = LoggerFactory.getLogger(KnowledgeBaseProviderRemoteClient.class);
    
    private Grid                             grid;
    private GridServiceDescription<GridNode> gsd;

    public KnowledgeBuilderProviderRemoteClient(Grid grid,
                                                GridServiceDescription gsd) {
        this.grid = grid;
        this.gsd = gsd;
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        String localId = UUID.randomUUID().toString();
        logger.info("This InstanceId (just generated) = "+localId);
        CommandImpl cmd = new CommandImpl("execute", Arrays.asList(
            new Object[]{
                new KnowledgeBuilderConfigurationRemoteCommands.NewKnowledgeBuilderConfigurationRemoteCommand(localId)
            }
        ));
        
        
        

        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBuilderConfigurationRemoteClient(localId, grid, gsd);
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder() {
        return newKnowledgeBuilder( null,
                                    null );

    }

    public KnowledgeJarBuilder newKnowledgeJarBuilder() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        return newKnowledgeBuilder( null,
                                    conf );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        return newKnowledgeBuilder( kbase,
                                    null );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        String localId = UUID.randomUUID().toString();
        String remoteConfId = null;
        if(conf != null) {
            remoteConfId = ((KnowledgeBuilderConfigurationRemoteClient)conf).getId();
        }
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommandFromCommand( "__TEMP__",
                                                                                                          localId,
                                                                                                          new NewKnowledgeBuilderRemoteCommand( remoteConfId ) )} ) );

        ConversationManager connm = this.grid.get( ConversationManager.class );
        ConversationUtil.sendMessage( connm,
                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                      this.gsd.getId(),
                                      cmd );

        return new KnowledgeBuilderRemoteClient( localId,
                                                 this.gsd,
                                                 connm,
                                                 (KnowledgeBuilderConfigurationRemoteClient)conf );
    }

    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts,
                                                  String systemId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}

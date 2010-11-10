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
import java.util.Collection;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.command.builder.KnowledgeBuilderGetErrorsCommand;
import org.drools.definition.KnowledgePackage;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CollectionClient;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.io.Resource;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderRemoteClient
    implements
    KnowledgeBuilder {

    private String                 instanceId;
    private ConversationManager    cm;
    private GridServiceDescription<GridNode>  gsd;

    public KnowledgeBuilderRemoteClient(String localId,
                                        GridServiceDescription gsd,
                                        ConversationManager cm) {
        this.instanceId = localId;
        this.gsd = gsd;
        this.cm = cm;
    }

    public void add(Resource resource,
                    ResourceType type) {
        add( resource,
             type,
             null );

    }

    public void add(Resource resource,
                    ResourceType type,
                    ResourceConfiguration configuration) {

        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderAddCommand( resource,
                                                                                                                                                       type,
                                                                                                                                                       configuration ),
                                                                                                                       this.instanceId,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       null ) } ) );

        ConversationUtil.sendMessage( this.cm,
                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                     this.gsd.getId(),
                     cmd );

    }

    public Collection<KnowledgePackage> getKnowledgePackages() {
        return new CollectionClient<KnowledgePackage>( this.instanceId );
    }

    public KnowledgeBase newKnowledgeBase() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean hasErrors() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBuilderErrors getErrors() {
        String commandId = "kbuilder.getErrors_" + this.gsd.getId();
        String kresultsId = "kresults_" + this.gsd.getId();
        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderGetErrorsCommand(),
                                                                                                                       this.instanceId,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       kresultsId ) } ) );

        Object result = ConversationUtil.sendMessage( this.cm,
                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                     this.gsd.getId(),
                                     cmd );

        return (KnowledgeBuilderErrors) result;

    }
 
}

/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.grid.remote;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.ChangeSet;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListener;
import org.drools.agent.KnowledgeAgent;
import org.drools.command.BatchExecutionCommand;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.event.knowledgeagent.KnowledgeAgentEventListener;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.ApplyChangeSetRemoteCommand;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatelessKnowledgeSession;

/**
 *
 * @author salaboy
 */
public class KnowledgeAgentRemoteClient implements KnowledgeAgent {

    private String id;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager cm;

    public KnowledgeAgentRemoteClient(String id, GridServiceDescription<GridNode> gsd, ConversationManager cm) {
        this.id = id;
        this.gsd = gsd;
        this.cm = cm;
    }

    public void addEventListener(KnowledgeAgentEventListener kl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeEventListener(KnowledgeAgentEventListener kl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase getKnowledgeBase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession(KnowledgeSessionConfiguration ksc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void monitorResourceChangeEvents(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void applyChangeSet( Resource rsrc ) {
        
        
        
        List<Command> commands = new ArrayList<Command>();
            commands.add( new ApplyChangeSetRemoteCommand( this.id, rsrc ) );
            commands.add( CommandFactory.newFireAllRules() );
        BatchExecutionCommand batch = CommandFactory.newBatchExecution( commands );

        Command c = new KnowledgeContextResolveFromContextCommand( batch,
                null,
                null,
                id,
                "" );
        
        
        CommandImpl cmd = new CommandImpl( "execute",
                    Arrays.asList( new Object[] { c, id } )
                );
        
        
        ConversationUtil.sendAsyncMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                cmd);
    }

    public void applyChangeSet( ChangeSet cs ) {
        List<Command> commands = new ArrayList<Command>();
            commands.add( new ApplyChangeSetRemoteCommand( this.id, cs ) );
            commands.add( CommandFactory.newFireAllRules() );
        BatchExecutionCommand batch = CommandFactory.newBatchExecution( commands );

        Command c = new KnowledgeContextResolveFromContextCommand( batch,
                        null,
                        null,
                        id,
                        "" );


        CommandImpl cmd = new CommandImpl( "execute",
                    Arrays.asList( new Object[] { c, id } )
                );

        ConversationUtil.sendAsyncMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                cmd);
    }

    public void setSystemEventListener(SystemEventListener sl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

/*
 * Copyright 2011 JBoss Inc..
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
import java.util.Arrays;
import java.util.Iterator;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.GetQueryIdentifiersRemoteCommand;
import org.drools.grid.remote.command.GetQueryResultsSizeRemoteCommand;
import org.drools.grid.remote.command.SetQueryIteratorRemoteCommand;
import org.kie.runtime.rule.QueryResults;
import org.kie.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class QueryResultsRemoteClient implements QueryResults {
    private String localId;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager cm;
    private String instanceId;
    private String queryName;
    public QueryResultsRemoteClient(String queryName, String instanceId, String localId,
                                                GridServiceDescription gsd,
                                                ConversationManager cm) {
        
        this.queryName = queryName;
        this.instanceId = instanceId;
        this.localId = localId;
        this.gsd = gsd;
        this.cm = cm;
    }

    public InternalQueryResultsClient getResults(){
        return new InternalQueryResultsClient(this.queryName,this.instanceId, this.localId, this.gsd, this.cm);
    }
    
    public String[] getIdentifiers() {
        
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetQueryIdentifiersRemoteCommand(this.queryName, this.localId  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        String[] results = (String[]) ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );

        return results;

    }

    public Iterator<QueryResultsRow> iterator() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new SetQueryIteratorRemoteCommand(this.queryName, this.localId  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return new QueryResultsRowIteratorRemoteClient(this.queryName, this.localId, this.instanceId, this.gsd, this.cm);
        
    }

    public int size() {
        String kresultsId = "kresults_" + this.gsd.getId();
        
         CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetQueryResultsSizeRemoteCommand(this.queryName, this.localId  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        Integer result = (Integer) ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );

        return result;
    }
    
}

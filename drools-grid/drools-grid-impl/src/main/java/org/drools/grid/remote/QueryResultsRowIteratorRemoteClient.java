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

import org.drools.core.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.HasNextQueryResultsRowRemoteCommand;
import org.drools.grid.remote.command.NextQueryResultsRowRemoteCommand;
import org.kie.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class QueryResultsRowIteratorRemoteClient implements Iterator<QueryResultsRow>{

    private String queryName;
    private String localId;
    private GridServiceDescription<GridNode> gsd;
    private String instanceId;
    private ConversationManager cm;

    public QueryResultsRowIteratorRemoteClient(String queryName, String localId, String instanceId, GridServiceDescription gsd,
                                                ConversationManager cm) {
        this.queryName = queryName;
        this.localId = localId;
        this.cm = cm;
        this.gsd = gsd;
        this.instanceId = instanceId;
    }
    
    
    
    public boolean hasNext() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new HasNextQueryResultsRowRemoteCommand(this.queryName, this.localId  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        Boolean result = (Boolean)ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return result;
    }

    public QueryResultsRow next() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new NextQueryResultsRowRemoteCommand(this.queryName, this.localId  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        String rowId = (String)ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return new QueryResultsRowRemoteClient(rowId, this.queryName, this.localId,this.instanceId, this.gsd, this.cm);
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
}

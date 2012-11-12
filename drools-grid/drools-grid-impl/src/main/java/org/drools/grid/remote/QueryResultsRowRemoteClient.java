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
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.GetFactHandleFromQueryResultsRowRemoteCommand;
import org.drools.grid.remote.command.GetObjectFromQueryResultsRowRemoteCommand;
import org.kie.runtime.rule.FactHandle;
import org.kie.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class QueryResultsRowRemoteClient implements QueryResultsRow{

    private String queryName;
    private String localId;
    private String instanceId;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager cm;
    private String rowId;

    public QueryResultsRowRemoteClient(String rowId, String queryName, String localId, String instanceId, GridServiceDescription<GridNode> gsd, ConversationManager cm) {
        this.queryName = queryName;
        this.localId = localId;
        this.instanceId = instanceId;
        this.gsd = gsd;
        this.cm = cm;
        this.rowId = rowId;
    }
    
    
    
    
    
    
    public Object get(String identifier) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetObjectFromQueryResultsRowRemoteCommand(rowId, this.queryName, this.localId, identifier  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        Object result = ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return result;
    }

    public FactHandle getFactHandle(String identifier) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetFactHandleFromQueryResultsRowRemoteCommand(rowId, this.queryName, this.localId, identifier  ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );
        
        
        
        FactHandle result = (FactHandle)ConversationUtil.sendMessage( this.cm,
                                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return result;
    }
    
}

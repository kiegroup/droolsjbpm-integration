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
import java.util.Arrays;

import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;

/**
 *
 * @author salaboy
 */
public class InternalQueryResultsClient {
    private String queryName;
    private String localId;
    private String instanceId;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager cm;
    public InternalQueryResultsClient(String queryName,String instanceId, String localId, GridServiceDescription<GridNode> gsd, ConversationManager cm) {
        this.queryName = queryName;
        this.instanceId = instanceId;
        this.localId = localId;
        this.gsd = gsd;
        this.cm = cm;
    }
    
    public String[] getParameters(){
        String kresultsId = "kresults_" + this.gsd.getId();
        
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetQueryParametersRemoteCommand(this.queryName, this.localId  ),
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
    
    public Object getObject(String key){
        //String kresultsId = "kresults_" + this.gsd.getId();
        String kresultsId = "kresults_execute" ;
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new GetQueryObjectRemoteCommand(this.localId, key ),
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
    
    
    
}

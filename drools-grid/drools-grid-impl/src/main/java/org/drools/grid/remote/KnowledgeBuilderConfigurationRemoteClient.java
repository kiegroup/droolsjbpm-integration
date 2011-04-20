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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Set;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.conf.AccumulateFunctionOption;
import org.drools.builder.conf.KnowledgeBuilderOption;
import org.drools.builder.conf.MultiValueKnowledgeBuilderOption;
import org.drools.builder.conf.SingleValueKnowledgeBuilderOption;
import org.drools.command.CommandFactory;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderConfigurationRemoteClient implements KnowledgeBuilderConfiguration, Serializable{
    private String instanceId;
    private GridServiceDescription<GridNode> gsd;
    private Grid  grid;
    
    KnowledgeBuilderConfigurationRemoteClient(String instanceId, Grid grid, GridServiceDescription<GridNode> gsd) {
        this.instanceId = instanceId;
        this.gsd = gsd;
        this.grid = grid;
    }

    public void setProperty(String name, String value) {
        System.out.println("This InstanceId (ConfRemoteClient) = "+instanceId);
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{CommandFactory.newKBuilderSetPropertyCommand(instanceId, name, value)} ) );

        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        

    }

    public String getProperty(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends KnowledgeBuilderOption> void setOption(T option) {
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{CommandFactory.newKBuilderSetPropertyCommand(instanceId, option.getPropertyName(), ((AccumulateFunctionOption)option).getFunction().getClass().getCanonicalName())} ) );
        
        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
    }

    public <T extends SingleValueKnowledgeBuilderOption> T getOption(Class<T> option) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends MultiValueKnowledgeBuilderOption> T getOption(Class<T> option, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends MultiValueKnowledgeBuilderOption> Set<String> getOptionKeys(Class<T> option) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        return instanceId;
    }

    public void setId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    

}

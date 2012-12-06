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

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.commands.KnowledgeBaseConfigurationRemoteCommands;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.kie.KnowledgeBaseConfiguration;
import org.kie.conf.KnowledgeBaseOption;
import org.kie.conf.MultiValueKnowledgeBaseOption;
import org.kie.conf.SingleValueKnowledgeBaseOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class KnowledgeBaseConfigurationRemoteClient implements KnowledgeBaseConfiguration, Serializable{
    
    public final static String PROPERTY_MESSAGE_TIMEOUT = "grid.kbase.message.timeout";
    public final static String PROPERTY_MESSAGE_MINIMUM_WAIT_TIME = "grid.kbase.message.min.wait";
    
    private static Logger logger = LoggerFactory.getLogger(KnowledgeBaseConfigurationRemoteClient.class);
    
    private String instanceId;
    private GridServiceDescription<GridNode> gsd;
    private Grid  grid;
    
    KnowledgeBaseConfigurationRemoteClient(String instanceId, Grid grid, GridServiceDescription<GridNode> gsd) {
        this.instanceId = instanceId;
        this.gsd = gsd;
        this.grid = grid;
    }

    public void setProperty(final String name, final String value) {
        logger.info("This InstanceId (ConfRemoteClient) = "+instanceId);
        
        
        
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeBaseConfigurationRemoteCommands.SetPropertyRemoteCommand(instanceId, name, value)
        } ) );

        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        

    }

    public String getProperty(final String name) {
        
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeBaseConfigurationRemoteCommands.GetPropertyRemoteCommand(instanceId, name)
        } ) );

        return (String) ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
    }

    public String getId() {
        return instanceId;
    }

    public void setId(String instanceId) {
        this.instanceId = instanceId;
    }

    public <T extends KnowledgeBaseOption> void setOption(T option) {
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeBaseConfigurationRemoteCommands.SetOptionRemoteCommand(instanceId, option)
        } ) );

        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
    }

    public <T extends SingleValueKnowledgeBaseOption> T getOption(Class<T> option) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends MultiValueKnowledgeBaseOption> T getOption(Class<T> option, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    

}

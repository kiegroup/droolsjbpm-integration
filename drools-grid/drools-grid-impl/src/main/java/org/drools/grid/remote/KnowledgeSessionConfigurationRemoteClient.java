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
import org.drools.grid.internal.commands.KnowledgeSessionConfigurationRemoteCommands;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.conf.KnowledgeSessionOption;
import org.drools.runtime.conf.MultiValueKnowledgeSessionOption;
import org.drools.runtime.conf.SingleValueKnowledgeSessionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class KnowledgeSessionConfigurationRemoteClient implements KnowledgeSessionConfiguration, Serializable{
    
    public final static String PROPERTY_MESSAGE_TIMEOUT = "grid.ksession.message.timeout";
    public final static String PROPERTY_MESSAGE_MINIMUM_WAIT_TIME = "grid.ksession.message.min.wait";
    
    private static Logger logger = LoggerFactory.getLogger(KnowledgeSessionConfigurationRemoteClient.class);
    
    private String instanceId;
    private GridServiceDescription<GridNode> gsd;
    private Grid  grid;
    
    KnowledgeSessionConfigurationRemoteClient(String instanceId, Grid grid, GridServiceDescription<GridNode> gsd) {
        this.instanceId = instanceId;
        this.gsd = gsd;
        this.grid = grid;
    }

    public void setProperty(final String name, final String value) {
        logger.info("This InstanceId (KSessionConfRemoteClient) = "+instanceId);
        
        
        
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeSessionConfigurationRemoteCommands.SetPropertyRemoteCommand(instanceId, name, value)
        } ) );

        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        

    }

    public String getProperty(final String name) {
        
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeSessionConfigurationRemoteCommands.GetPropertyRemoteCommand(instanceId, name)
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

    public <T extends KnowledgeSessionOption> void setOption(T option) {
        CommandImpl cmd = new CommandImpl( "execute",
            Arrays.asList( new Object[]{
                new KnowledgeSessionConfigurationRemoteCommands.SetOptionRemoteCommand(instanceId, option)
        } ) );

        ConversationUtil.sendMessage( this.grid.get(ConversationManager.class),
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
    }

    public <T extends SingleValueKnowledgeSessionOption> T getOption(Class<T> option) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends MultiValueKnowledgeSessionOption> T getOption(Class<T> option, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    

}

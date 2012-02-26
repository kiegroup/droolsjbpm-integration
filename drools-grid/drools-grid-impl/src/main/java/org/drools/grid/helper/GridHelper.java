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
package org.drools.grid.helper;

import java.util.HashMap;
import javax.persistence.Persistence;
import org.drools.agent.KnowledgeAgent;
import org.drools.grid.*;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.remote.KnowledgeAgentRemoteClient;
import org.drools.grid.remote.QueryResultsRemoteClient;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author salaboy
 */
public class GridHelper {

    private static Grid gridHelper;
    public static Logger logger = LoggerFactory.getLogger(GridHelper.class);

    public static Grid getGrid() {

        gridHelper = new GridImpl(new HashMap<String, Object>());
        //Local Grid Configuration, for our client
        GridPeerConfiguration conf = new GridPeerConfiguration();


        //Configuring the a local WhitePages service that is being shared with all the grid peers
        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();

        wplConf.setWhitePages(new JpaWhitePages(Persistence.createEntityManagerFactory("org.drools.grid")));
        conf.addConfiguration(wplConf);

        conf.configure(gridHelper);


        return gridHelper;

    }

    public static synchronized GridNode getGridNode(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper trying to locate GridNode: " + name);
        }

        GridServiceDescription<GridNode> nGsd = getGrid().get(WhitePages.class).lookup(name);

        if (nGsd == null) {
            if (logger.isDebugEnabled()) {
                logger.error(" ### Grid Helper DOES NOT Found a Node Descriptor for: " + name);
            }
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper Found Node Descriptor: " + nGsd);
            logger.debug(" ### \t id: " + nGsd.getId());
            logger.debug(" ### \t Address size: " + nGsd.getAddresses().size());
            logger.debug(" ### \t Addresses: " + nGsd.getAddresses());
            for (String key : nGsd.getAddresses().keySet()) {
                logger.debug(" \t ### Address: " + nGsd.getAddresses().get(key));
            }

            logger.debug(" ### \t Interface: " + nGsd.getServiceInterface());
            logger.debug(" ### \t DATA: " + nGsd.getData());
        }
        GridConnection<GridNode> conn = getGrid().get(ConnectionFactoryService.class).createConnection(nGsd);
        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper Create a Conection: " + name);
        }
        GridNode node = conn.connect();
        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper found GridNode: (" + name + ") -> " + node);
        }
        return node;

    }

    public static QueryResultsRemoteClient getQueryResultRemoteClient(String nodeId, String sessionId, String queryName, String remoteResultsId) {

        GridServiceDescription<GridNode> gsd = getGridServiceDescriptor(nodeId);
        GridNode node = getGridNode(nodeId);
        String reverseId = node.get(sessionId, String.class);
        return new QueryResultsRemoteClient(queryName, reverseId, remoteResultsId, gsd, getGrid().get(ConversationManager.class));
    }
    
    
    public static KnowledgeAgent getKnowledgeAgentRemoteClient(String nodeId, String sessionId){
        GridServiceDescription<GridNode> gsd = getGridServiceDescriptor(nodeId);
        GridNode node = getGridNode(nodeId);
        String reverseId = node.get(sessionId, String.class);
        return new KnowledgeAgentRemoteClient(reverseId, gsd, getGrid().get(ConversationManager.class));
    }

    private static GridServiceDescription<GridNode> getGridServiceDescriptor(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper trying to locate GridNode: " + name);
        }


        if (logger.isDebugEnabled()) {
            logger.debug(" ### Grid Helper Looking up: " + name);
        }
        GridServiceDescription<GridNode> nGsd = getGrid().get(WhitePages.class).lookup(name);
        return nGsd;
    }
}

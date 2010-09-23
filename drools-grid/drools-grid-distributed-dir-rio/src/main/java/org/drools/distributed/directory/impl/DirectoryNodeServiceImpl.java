/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.distributed.directory.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.grid.distributed.directory.commands.GetKnowledgeBaseGridCommand;
import org.drools.grid.ConnectorException;
import org.drools.grid.internal.Message;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnectorFactory;
import org.drools.grid.GenericNodeConnector;

/**
 *
 * @author salaboy
 */
public class DirectoryNodeServiceImpl implements DirectoryNodeService {

    private String id;
    private Map<String, String> directoryMap = new HashMap<String, String>();
    private Map<String, String> kbaseDirectoryMap = new HashMap<String, String>();
    private Iterable<ExecutionNodeService> executionNodes;

    public DirectoryNodeServiceImpl() {
        executionNodes = new ArrayList<ExecutionNodeService>();
        this.id = "Distributed:Rio:Directory"+UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void register(String sessionId, String nodeServiceId) throws ConnectorException, RemoteException {

        directoryMap.put(sessionId, nodeServiceId);

        for (ExecutionNodeService node : executionNodes) {

            if (node.getId().equals(nodeServiceId)) {

                node.incrementKsessionCounter();
            }
        }
    }

    @Override
    public GenericNodeConnector lookup(String sessionId) throws ConnectorException, RemoteException {
        ExecutionNodeService nodeService = null;
        System.out.println("SessionID inside Lookup = "+sessionId);
        String sessionServiceId = (String) directoryMap.get(sessionId);
        System.out.println("SessionID from directoryMap = "+sessionServiceId);
        for (ExecutionNodeService ss : executionNodes) {
            System.out.println("NodeService = "+ss.getId() +" must match with = "+sessionServiceId);
            if (ss.getId().equals(sessionServiceId)) {
                nodeService = ss;
            }
        }
        
        return GenericConnectorFactory.newConnector(nodeService.getId());
    }

    public void setExecutionNodes(Iterable<ExecutionNodeService> executionNodes) {
        this.executionNodes = executionNodes;
    }

    @Override
    public void registerKBase(String kbaseId, String nodeServiceId) throws ConnectorException, RemoteException {

        kbaseDirectoryMap.put(kbaseId, nodeServiceId);
    }

    @Override
    public KnowledgeBase lookupKBase(String kbaseId) throws ConnectorException, RemoteException {
        ExecutionNodeService executionNode = null;
        String execNodeId = (String) kbaseDirectoryMap.get(kbaseId);

        for (ExecutionNodeService ns : executionNodes) {

            if (ns.getId().equals(execNodeId)) {
                executionNode = ns;
            }
        }


        try {
            //@TODO: This is a bad hack.. I need to improve this a lot
            Message msg = executionNode.write(new Message(999, 1000, false, new KnowledgeContextResolveFromContextCommand(new GetKnowledgeBaseGridCommand(), null, kbaseId, null, null)));

            if (msg.getPayload() instanceof KnowledgeBase) {
                return (KnowledgeBase) msg.getPayload();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute message",
                    e);
        }
    }

    @Override
    public Map<String, String> getExecutorsMap() throws ConnectorException, RemoteException {
        return directoryMap;
    }

    @Override
    public void register(String executorId, GenericNodeConnector resourceConnector) throws ConnectorException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String lookupId(String sessionId) throws ConnectorException {
        return directoryMap.get(sessionId);
    }

    @Override
    public void dispose() throws ConnectorException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

    @Override
    public void unregister(String executorId) throws ConnectorException, RemoteException {
        directoryMap.remove(executorId);
    }

    @Override
    public Map<String, String> getKBasesMap() throws ConnectorException, RemoteException {
        return kbaseDirectoryMap;
    }

    @Override
    public void registerKBase(String kbaseId, KnowledgeBase kbase) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unregisterKBase(String kbaseId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

    @Override
    public ServiceType getServiceType() throws ConnectorException, RemoteException {
        return ServiceType.LOCAL;
    }
}

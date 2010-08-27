/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.grid;

import java.rmi.RemoteException;
import java.util.Map;

import org.drools.KnowledgeBase;

/**
 *
 * @author salaboy
 */

public interface DirectoryNodeService
    extends
    NodeService {

    public void register(String executorId,
                         String resourceId) throws ConnectorException,
                                           RemoteException;

    public void register(String executorId,
                         GenericNodeConnector resourceConnector) throws ConnectorException,
                                                                RemoteException;

    public void unregister(String executorId) throws ConnectorException,
                                             RemoteException;

    public GenericNodeConnector lookup(String resourceId) throws ConnectorException,
                                                         RemoteException;

    // Returns the ID of the GenericNodeConnector found
    public String lookupId(String resourceId) throws ConnectorException,
                                             RemoteException;

    public void registerKBase(String kbaseId,
                              String resourceId) throws ConnectorException,
                                                RemoteException;

    public void registerKBase(String kbaseId,
                              KnowledgeBase kbase) throws ConnectorException,
                                                  RemoteException;

    public void unregisterKBase(String kbaseId) throws ConnectorException,
                                               RemoteException;

    public KnowledgeBase lookupKBase(String kbaseId) throws ConnectorException,
                                                    RemoteException;

    public Map<String, String> getExecutorsMap() throws ConnectorException,
                                                RemoteException;

    public Map<String, String> getKBasesMap() throws ConnectorException,
                                             RemoteException;

    public void dispose() throws ConnectorException,
                         RemoteException;

}

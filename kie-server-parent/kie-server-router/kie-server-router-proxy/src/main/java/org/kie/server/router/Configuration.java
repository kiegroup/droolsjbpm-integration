/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Configuration {

    private Map<String, List<String>> hostsPerServer = new ConcurrentHashMap<>();
    private Map<String, List<String>> hostsPerContainer = new ConcurrentHashMap<>();
    private Map<String, List<ContainerInfo>> containerInfosPerContainer = new ConcurrentHashMap<>();

    private Set<ConfigurationListener> listeners = new CopyOnWriteArraySet<>();
    
    public Map<String, List<String>> getHostsPerServer() {
        return hostsPerServer;
    }
    
    public Map<String, List<String>> getHostsPerContainer() {
        return hostsPerContainer;
    }

    public Map<String, List<ContainerInfo>> getContainerInfosPerContainer() {
        return containerInfosPerContainer;
    }

    public void addContainerHost(String containerId, String serverUrl) {
        List<String> hosts = hostsPerContainer.get(containerId);
        if (hosts == null) {
            hosts = new ArrayList<>();
            hostsPerContainer.put(containerId, hosts);
        }
        hosts.add(serverUrl);

        this.listeners.forEach(l -> l.onContainerAdded(containerId, serverUrl));
    }
    
    public void addServerHost(String serverId, String serverUrl) {
        List<String> hosts = hostsPerServer.get(serverId);
        if (hosts == null) {
            hosts = new ArrayList<>();
            hostsPerServer.put(serverId, hosts);
        }
        hosts.add(serverUrl);

        this.listeners.forEach(l -> l.onServerAdded(serverId, serverUrl));
    }

    public void addContainerInfo(ContainerInfo containerInfo) {
        List<ContainerInfo> containersByAlias = containerInfosPerContainer.get(containerInfo.getAlias());
        if (containersByAlias == null) {
            containersByAlias = new ArrayList<>();
            containerInfosPerContainer.put(containerInfo.getAlias(), containersByAlias);
        }
        containersByAlias.add(containerInfo);

        List<ContainerInfo> containersById = containerInfosPerContainer.get(containerInfo.getContainerId());
        if (containersById == null) {
            containersById = new ArrayList<>();
            containerInfosPerContainer.put(containerInfo.getContainerId(), containersById);
        }
        containersById.add(containerInfo);
    }
    
    public void removeContainerHost(String containerId, String serverUrl) {
        List<String> hosts = hostsPerContainer.get(containerId);
        if (hosts != null) {
            hosts.remove(serverUrl);
        }

        this.listeners.forEach(l -> l.onContainerRemoved(containerId, serverUrl));
    }
    
    public void removeServerHost(String serverId, String serverUrl) {
        List<String> hosts = hostsPerServer.get(serverId);
        if (hosts != null) {
            hosts.remove(serverUrl);
        }

        this.listeners.forEach(l -> l.onServerRemoved(serverId, serverUrl));
    }

    public void removeContainerInfo(ContainerInfo containerInfo) {
        List<ContainerInfo> containersById = containerInfosPerContainer.get(containerInfo.getContainerId());
        containersById.remove(containerInfo);
        
        List<String> hosts = hostsPerContainer.getOrDefault(containerInfo.getContainerId(), Collections.emptyList());
        if (hosts.isEmpty()) {
            containerInfosPerContainer.remove(containerInfo.getContainerId());
        }
        
        List<ContainerInfo> containersByAlias = containerInfosPerContainer.get(containerInfo.getAlias());
        containersByAlias.remove(containerInfo);
        
        hosts = hostsPerContainer.getOrDefault(containerInfo.getAlias(), Collections.emptyList());
        if (hosts.isEmpty()) {
            containerInfosPerContainer.remove(containerInfo.getAlias());
        }
    }

    public synchronized void removeUnavailableServer(String requestURL) {
        String serverUrl = null;
        String serverId = null;

        // locate server id for request url
        for (Map.Entry<String, List<String>> entry : hostsPerServer.entrySet()) {
            List<String> hosts = entry.getValue();

            for (String host : hosts) {
                if (requestURL.startsWith(host)) {
                    serverUrl = host;
                    serverId = entry.getKey();

                    break;
                }
            }
        }

        if (serverId != null && serverUrl != null) {
            removeServerHost(serverId, serverUrl);
        }

        // locate containers for request url
        Set<String> containers = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : hostsPerContainer.entrySet()) {
            List<String> hosts = entry.getValue();

            for (String host : hosts) {
                if (requestURL.startsWith(host)) {
                    serverUrl = host;
                    containers.add(entry.getKey());
                }
            }
        }

        if (!containers.isEmpty()) {
            final String actualServerUrl = serverUrl;
            containers.forEach( container ->
                    removeContainerHost(container, actualServerUrl)
            );
        }
    }

    public void addListener(ConfigurationListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ConfigurationListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public String toString() {
        return "{hostsPerServer=" + hostsPerServer + ", hostsPerContainer=" + hostsPerContainer + "}";
    }

    public void addEmptyContainerHost(String containerId) {
        List<String> hosts = hostsPerContainer.get(containerId);
        if (hosts == null) {
            hosts = new ArrayList<>();
            hostsPerContainer.put(containerId, hosts);
        }
    }

    public void addEmptyServerHost(String serverId) {
        List<String> hosts = hostsPerServer.get(serverId);
        if (hosts == null) {
            hosts = new ArrayList<>();
            hostsPerServer.put(serverId, hosts);
        }
    }
}

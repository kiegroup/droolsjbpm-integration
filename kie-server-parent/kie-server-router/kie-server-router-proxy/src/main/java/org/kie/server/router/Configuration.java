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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration {

    private Map<String, Set<String>> hostsPerServer = new ConcurrentHashMap<>();
    private Map<String, Set<String>> hostsPerContainer = new ConcurrentHashMap<>();
    
    public Map<String, Set<String>> getHostsPerServer() {
        return hostsPerServer;
    }
    
    public Map<String, Set<String>> getHostsPerContainer() {
        return hostsPerContainer;
    }
    
    public void addContainerHost(String containerId, String serverUrl) {
        Set<String> hosts = hostsPerContainer.get(containerId);
        if (hosts == null) {
            hosts = new LinkedHashSet<>();
            hostsPerContainer.put(containerId, hosts);
        }
        hosts.add(serverUrl);
    }
    
    public void addServerHost(String serverId, String serverUrl) {
        Set<String> hosts = hostsPerServer.get(serverId);
        if (hosts == null) {
            hosts = new LinkedHashSet<>();
            hostsPerServer.put(serverId, hosts);
        }
        hosts.add(serverUrl);
    }
    
    public void removeContainerHost(String containerId, String serverUrl) {
        Set<String> hosts = hostsPerContainer.get(containerId);
        if (hosts != null) {
            hosts.remove(serverUrl);
        } 
    }
    
    public void removeServerHost(String serverId, String serverUrl) {
        Set<String> hosts = hostsPerServer.get(serverId);
        if (hosts != null) {
            hosts.remove(serverUrl);
        } 
    }

    @Override
    public String toString() {
        return "{hostsPerServer=" + hostsPerServer + ", hostsPerContainer=" + hostsPerContainer + "}";
    }
}

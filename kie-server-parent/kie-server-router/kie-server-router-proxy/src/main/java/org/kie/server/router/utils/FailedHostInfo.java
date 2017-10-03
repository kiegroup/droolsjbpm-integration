/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.utils;

import java.io.Serializable;
import java.util.Set;


public class FailedHostInfo implements Serializable {

    private static final long serialVersionUID = -4296456873314339089L;
    
    private String serverId;
    private String serverUrl;
    private Set<String> containers;
    
    private int attempts = 0;
    
    public FailedHostInfo(String serverId, String serverUrl, Set<String> containers) {
        this.serverId = serverId;
        this.serverUrl = serverUrl;
        this.containers = containers;
    }
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public Set<String> getContainers() {
        return containers;
    }
    
    public void setContainers(Set<String> containers) {
        this.containers = containers;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    
    public void attempted() {
        this.attempts++;
    }

    @Override
    public String toString() {
        return "FailedHostInfo [serverId=" + serverId + ", serverUrl=" + serverUrl + ", containers=" + containers + "]";
    }

}

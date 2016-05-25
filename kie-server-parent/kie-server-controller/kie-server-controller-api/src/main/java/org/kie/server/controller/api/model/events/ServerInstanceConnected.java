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

package org.kie.server.controller.api.model.events;

import org.kie.server.controller.api.model.runtime.ServerInstance;

public class ServerInstanceConnected {

    private ServerInstance serverInstance;

    public ServerInstanceConnected() {
    }

    public ServerInstanceConnected(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    public void setServerInstance(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    @Override
    public String toString() {
        return "ServerInstanceConnected{" +
                "serverInstance=" + serverInstance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerInstanceConnected that = (ServerInstanceConnected) o;

        if (serverInstance != null ? !serverInstance.equals(that.serverInstance) : that.serverInstance != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serverInstance != null ? serverInstance.hashCode() : 0;
    }
}

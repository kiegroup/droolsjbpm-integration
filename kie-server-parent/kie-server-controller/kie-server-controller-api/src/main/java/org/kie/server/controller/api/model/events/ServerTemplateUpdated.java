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

import org.kie.server.controller.api.model.spec.ServerTemplate;

public class ServerTemplateUpdated {

    private ServerTemplate serverTemplate;

    public ServerTemplateUpdated() {
    }

    public ServerTemplateUpdated(ServerTemplate serverTemplate) {
        this.serverTemplate = serverTemplate;
    }

    public ServerTemplate getServerTemplate() {
        return serverTemplate;
    }

    public void setServerInstance(ServerTemplate serverInstance) {
        this.serverTemplate = serverInstance;
    }

    @Override
    public String toString() {
        return "Updated server template{" +
                 serverTemplate +
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

        ServerTemplateUpdated that = (ServerTemplateUpdated) o;

        if (serverTemplate != null ? !serverTemplate.equals(that.serverTemplate) : that.serverTemplate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serverTemplate != null ? serverTemplate.hashCode() : 0;
    }
}

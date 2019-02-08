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

import java.util.Objects;

import org.kie.server.controller.api.model.spec.ServerTemplate;

public class ServerTemplateUpdated implements KieServerControllerEvent {

    private ServerTemplate serverTemplate;
    private boolean resetBeforeUpdate;

    public ServerTemplateUpdated() {
    }

    public ServerTemplateUpdated(ServerTemplate serverTemplate) {
        this(serverTemplate, false);
    }

    public ServerTemplateUpdated(ServerTemplate serverTemplate, boolean resetBeforeUpdate) {
        this.serverTemplate = serverTemplate;
        this.resetBeforeUpdate = resetBeforeUpdate;
    }

    public ServerTemplate getServerTemplate() {
        return serverTemplate;
    }

    public void setServerInstance(ServerTemplate serverInstance) {
        this.serverTemplate = serverInstance;
    }

    public boolean isResetBeforeUpdate() {
        return resetBeforeUpdate;
    }

    public void setResetBeforeUpdate(boolean resetBeforeUpdate) {
        this.resetBeforeUpdate = resetBeforeUpdate;
    }

    @Override
    public String toString() {
        return "Updated server template{" +
                "serverTemplate=" + serverTemplate +
                ", resetBeforeUpdate=" + resetBeforeUpdate +
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

        return resetBeforeUpdate == that.resetBeforeUpdate && Objects.equals(serverTemplate, that.serverTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverTemplate, resetBeforeUpdate);
    }
}

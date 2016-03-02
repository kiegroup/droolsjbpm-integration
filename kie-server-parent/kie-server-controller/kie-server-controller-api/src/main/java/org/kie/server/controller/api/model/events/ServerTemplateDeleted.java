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

public class ServerTemplateDeleted {

    private String serverTemplateId;

    public ServerTemplateDeleted() {
    }

    public ServerTemplateDeleted(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    public String getServerTemplateId() {
        return serverTemplateId;
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    @Override
    public String toString() {
        return "Deleted server template (id) {" +
                serverTemplateId + '\'' +
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

        ServerTemplateDeleted that = (ServerTemplateDeleted) o;

        if (serverTemplateId != null ? !serverTemplateId.equals(that.serverTemplateId) : that.serverTemplateId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serverTemplateId != null ? serverTemplateId.hashCode() : 0;
    }
}

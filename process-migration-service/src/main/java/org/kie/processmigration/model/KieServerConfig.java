/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesClient;

public class KieServerConfig {

    private String id;

    private String host;

    @JsonIgnore
    private CredentialsProvider credentialsProvider;

    @JsonIgnore
    private KieServicesClient client;

    public String getId() {
        if (client == null) {
            return null;
        }
        try {
            if (this.id == null) {
                ServiceResponse<KieServerInfo> info = client.getServerInfo();
                this.id = info.getResult().getServerId();
            }
        } catch (Exception e) {
            return null;
        }
        return id;
    }

    public KieServerConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getHost() {
        return host;
    }

    public KieServerConfig setHost(String host) {
        this.host = host;
        return this;
    }


    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public KieServerConfig setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public KieServicesClient getClient() {
        return client;
    }

    public KieServerConfig setClient(KieServicesClient client) {
        this.client = client;
        return this;
    }

    public String getStatus() {
        if (client == null) {
            return Status.UNKNOWN;
        }
        try {
            return client.getServerInfo().getType().name();
        } catch (Exception e) {
            return Status.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("KieServerConfig [id=").append(id).append(", host=").append(host).append(", status=").append(getStatus()).append("]");
        return builder.toString();
    }
}

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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

@ApplicationScoped
public class KieServerConfig {

    @Inject
    @ConfigurationValue("kieserver.host")
    private String host;
    @Inject
    @ConfigurationValue("kieserver.port")
    private Integer port;
    @Inject
    @ConfigurationValue("kieserver.contextRoot")
    private String contextRoot;
    @Inject
    @ConfigurationValue("kieserver.path")
    private String path;
    @Inject
    @ConfigurationValue("kieserver.protocol")
    private String protocol;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUrl() {
        return new StringBuilder(protocol)
                                          .append("://")
                                          .append(host)
                                          .append(":")
                                          .append(port)
                                          .append(contextRoot)
                                          .append(path)
                                          .toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("KieServerConfig [host=").append(host).append(", port=").append(port).append(", contextRoot=")
               .append(contextRoot).append(", path=").append(path).append(", protocol=").append(protocol).append("]");
        return builder.toString();
    }

}

/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.gateway;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Configuration;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieControllerGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieControllerGateway.class);
    private final KieServerControllerClient client;
    private final String targetUrl;

    public KieControllerGateway(String protocol, String hostname, Integer port, String username, String password, Integer connectionTimeout, Integer socketTimeout, String contextPath, String controllerPath) {

        targetUrl = new StringBuilder(protocol)
            .append("://").append(hostname)
            .append(":").append(port)
            .append("/").append(contextPath)
            .append(controllerPath)
            .toString();

        final Configuration configuration =
                new ResteasyClientBuilder()
                        .connectionPoolSize(1)
                        .establishConnectionTimeout(connectionTimeout,
                                                    TimeUnit.SECONDS)
                        .socketTimeout(socketTimeout,
                                       TimeUnit.SECONDS)
                        .getConfiguration();

        client = KieServerControllerClientFactory.newRestClient(targetUrl,
                                                                username,
                                                                password,
                                                                MarshallingFormat.JSON,
                                                                configuration);
    }

    public ServerTemplateList getServerTemplateList() {
        return client.listServerTemplates();

    }

    public ServerTemplate getServerTemplate(String templateId) {
        return client.getServerTemplate(templateId);
    }

    public void createServerTemplate(ServerTemplate serverTemplate) {
        client.saveServerTemplate(serverTemplate);
    }

    public void deleteServerTemplate(String templateId) {
        client.deleteServerTemplate(templateId);
    }

    public ContainerSpecList getContainers(String templateId) {
        return client.listContainerSpec(templateId);
    }

    public ContainerSpec getContainer(String templateId, String containerId) {
        return client.getContainerInfo(templateId, containerId);
    }

    public void createContainer(String templateId, ContainerSpec containerSpec) {
        client.saveContainerSpec(templateId, containerSpec);
    }

    public void disposeContainer(String templateId, String containerId) {
        client.deleteContainerSpec(templateId, containerId);
    }

    public void startContainer(String templateId, String containerId) {
        client.startContainer(new ContainerSpecKey(containerId, containerId, new ServerTemplateKey(templateId, templateId)));
    }

    public void stopContainer(String templateId, String containerId) {
        client.stopContainer(new ContainerSpecKey(containerId, containerId, new ServerTemplateKey(templateId, templateId)));
    }

    public void close() {
        try {
            client.close();
        } catch (IOException ex){
            LOGGER.warn("Failed to close Kie Server Controller Client due to: " + ex.getMessage(), ex);
        }
    }

}

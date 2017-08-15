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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;

public class KieControllerGateway {

    private final ResteasyClient client;
    private final String targetUrl;
    private final JSONMarshaller jsonMarshaller;

    public KieControllerGateway(String protocol, String hostname, Integer port, String username, String password, Integer connectionTimeout, Integer socketTimeout, String contextPath, String controllerPath) {

        targetUrl = new StringBuilder(protocol)
            .append("://").append(hostname)
            .append(":").append(port)
            .append("/").append(contextPath)
            .append(controllerPath)
            .toString();

        client = new ResteasyClientBuilder()
            .connectionPoolSize(1)
            .establishConnectionTimeout(connectionTimeout, TimeUnit.SECONDS)
            .socketTimeout(socketTimeout, TimeUnit.SECONDS)
            .register(new Authenticator(username, password))
            .register(new ErrorResponseFilter())
            .build();

        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProcessConfig.class);
        classes.add(RuleConfig.class);

        // using kie marshaller
        jsonMarshaller = new JSONMarshaller(classes, Thread.currentThread().getContextClassLoader());

    }

    public ServerTemplateList getServerTemplateList() {

        String response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        return jsonMarshaller.unmarshall(response, ServerTemplateList.class);

    }

    public ServerTemplate getServerTemplate(String templateId) {

        String response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        return jsonMarshaller.unmarshall(response, ServerTemplate.class);

    }

    public void createServerTemplate(ServerTemplate serverTemplate) {

        String payload = jsonMarshaller.marshall(serverTemplate);

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(serverTemplate.getId())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(payload));

        response.close();

    }

    public void deleteServerTemplate(String templateId) {

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        response.close();

    }

    public ContainerSpecList getContainers(String templateId) {

        String response = client.target(targetUrl)
            .path("management")
            .path("servers")
            .path(templateId)
            .path("containers")
            .request(MediaType.APPLICATION_JSON)
            .get(String.class);

        return jsonMarshaller.unmarshall(response, ContainerSpecList.class);

    }

    public ContainerSpec getContainer(String templateId, String containerId) {

        String response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .path("containers")
                .path(containerId)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        return jsonMarshaller.unmarshall(response, ContainerSpec.class);

    }

    public void createContainer(String templateId, String containerId, ContainerSpec containerSpec) {

        String payload = jsonMarshaller.marshall(containerSpec);

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .path("containers")
                .path(containerId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(payload));

        response.close();

    }

    public void disposeContainer(String templateId, String containerId) {

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .path("containers")
                .path(containerId)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        response.close();

    }

    public void startContainer(String templateId, String containerId) {

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .path("containers")
                .path(containerId)
                .path("status")
                .path("started")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));

        response.close();

    }

    public void stopContainer(String templateId, String containerId) {

        Response response = client.target(targetUrl)
                .path("management")
                .path("servers")
                .path(templateId)
                .path("containers")
                .path(containerId)
                .path("status")
                .path("stopped")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));

        response.close();

    }

    public void close() {

        client.close();
        jsonMarshaller.dispose();

    }

}

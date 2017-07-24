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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.*;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerGateway {

    public static final Logger LOG = LoggerFactory.getLogger(KieServerGateway.class);

    private final KieServicesConfiguration config;
    private KieServicesClient client;

    public KieServerGateway(String protocol, String hostname, Integer port, String username, String password, Integer timeout, String contextPath) {

        String serverUrl = new StringBuilder(protocol)
                .append("://").append(hostname)
                .append(":").append(port)
                .append("/").append(contextPath)
                .append("/services/rest/server")
                .toString();

        LOG.info("Server Url {}", serverUrl);

        config = KieServicesFactory.newRestConfiguration(serverUrl, username, password);
        config.setMarshallingFormat(MarshallingFormat.JSON);
        config.setTimeout(timeout);
    }

    public void init() throws MojoFailureException {

        try {
            client = KieServicesFactory.newKieServicesClient(config);
        } catch (RuntimeException kieEx) {
            throw new MojoFailureException("error on establish connection with remote server: " + kieEx.getMessage(), kieEx.getCause());
        }

    }

    public void deploy(MavenProject project, String container, RuntimeStrategy strategy) throws MojoFailureException {

        KieContainerResource kieContainer = new KieContainerResource();
        kieContainer.setContainerId(container);

        ReleaseId releaseId = new ReleaseId();
        releaseId.setGroupId(project.getGroupId());
        releaseId.setArtifactId(project.getArtifactId());
        releaseId.setVersion(project.getVersion());
        kieContainer.setReleaseId(releaseId);

        if (strategy != null) {
            KieServerConfigItem configItem = new KieServerConfigItem();
            configItem.setName("RuntimeStrategy");
            configItem.setValue(strategy.name());
            configItem.setType("BPM");
            kieContainer.addConfigItem(configItem);
        }

        ServiceResponse<KieContainerResource> serviceResponse = client.createContainer(container, kieContainer);
        ServiceResponse.ResponseType type = serviceResponse.getType();
        String msg = serviceResponse.getMsg();

        LOG.info("Deploy Response: {}", serviceResponse);

        if (ServiceResponse.ResponseType.FAILURE.equals(type)) {
            throw new MojoFailureException(msg);
        }

    }

    public void dispose(String container) throws MojoFailureException {

        ServiceResponse<Void> serviceResponse = client.disposeContainer(container);
        ServiceResponse.ResponseType type = serviceResponse.getType();
        String msg = serviceResponse.getMsg();

        LOG.info("Dispose Response: {}", serviceResponse);

        if (ServiceResponse.ResponseType.FAILURE.equals(type)) {
            throw new MojoFailureException(msg);
        }

    }

    public void update(MavenProject project, String container) throws MojoFailureException {

        ReleaseId releaseId = new ReleaseId();
        releaseId.setGroupId(project.getGroupId());
        releaseId.setArtifactId(project.getArtifactId());
        releaseId.setVersion(project.getVersion());

        ServiceResponse<ReleaseId> serviceResponse = client.updateReleaseId(container, releaseId);
        ServiceResponse.ResponseType type = serviceResponse.getType();
        String msg = serviceResponse.getMsg();

        LOG.info("Replace Response: {}", serviceResponse);

        if (ServiceResponse.ResponseType.FAILURE.equals(type)) {
            throw new MojoFailureException(msg);
        }

    }

}

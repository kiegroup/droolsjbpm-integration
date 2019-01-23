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
package org.kie.server.services.impl.storage.file;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_CONTAINER_DEPLOYMENT;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_REPO;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.KieServerContainerDeployment;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.KieServerStateRepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialization class meant to be invoked by the OpenShift S2I process in bootstrapping an image with pre-installed KIE Containers.
 */
public class KieServerStateFileInit {

    private static final Logger logger = LoggerFactory.getLogger(KieServerStateFileInit.class);

    private KieServerStateFileInit() {}

    public static void main(String... args) {
        File serverStateFile = init();
        logger.info("Initialized kie server state file: {}", serverStateFile);
    }

    public static File init() {
        String serverRepo = getValue(KIE_SERVER_STATE_REPO, "KIE_SERVER_REPO", ".");
        String serverId = getValue(KIE_SERVER_ID, "KIE_SERVER_ID", "kieserver");
        KieServerEnvironment.setServerId(serverId);
        KieServerEnvironment.setServerName(serverId);

        File serverRepoDir = new File(serverRepo);
        File serverStateFile = new File(serverRepoDir, serverId + ".xml");
        if (serverStateFile.exists()) {
            throw new IllegalStateException(String.format(
                    "%s already exists. %s should only be used for pre-bootstrapping creation of server state file.",
                    serverStateFile,
                    KieServerStateFileInit.class.getSimpleName()));
        }
        KieServerStateRepository repository = new KieServerStateFileRepository(serverRepoDir);
        KieServerState serverState = new KieServerState();

        KieServerConfig config = new KieServerConfig();
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.put(KIE_SERVER_STATE_REPO, serverRepo);
        properties.put(KIE_SERVER_ID, serverId);
        KieServerStateRepositoryUtils.populateWithProperties(config, properties);
        serverState.setConfiguration(config);

        Set<KieContainerResource> containers = new LinkedHashSet<KieContainerResource>();
        String serverContainerDeployment = getValue(KIE_SERVER_CONTAINER_DEPLOYMENT, "KIE_SERVER_CONTAINER_DEPLOYMENT", null);
        Set<KieServerContainerDeployment> deployments = KieServerContainerDeployment.fromString(serverContainerDeployment);
        for (KieServerContainerDeployment deployment : deployments) {
            KieContainerResource container = new KieContainerResource(
                    deployment.getContainerId(),
                    new ReleaseId(deployment.getReleaseId()),
                    KieContainerStatus.STARTED);
            containers.add(container);
        }
        serverState.setContainers(containers);

        repository.store(serverId, serverState);
        return serverStateFile;
    }

    private static String getValue(String propName, String envName, String defaultValue) {
        String value = null;
        if (propName != null) {
            value = trimToNull(System.getProperty(propName));
        }
        if (value == null && envName != null) {
            value = trimToNull(System.getenv(envName));
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private static String trimToNull(String s) {
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                s = null;
            }
        }
        return s;
    }

}

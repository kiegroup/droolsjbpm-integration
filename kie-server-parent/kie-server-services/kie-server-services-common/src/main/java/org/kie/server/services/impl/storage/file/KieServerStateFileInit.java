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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.KieServerContainerDeployment;
import org.kie.server.services.impl.StartupStrategyProvider;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.KieServerStateRepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_CONTAINER_DEPLOYMENT;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE_INIT;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_REPO;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_REPO_TYPE_DEFAULT;

/**
 * Initialization class meant to be invoked by the OpenShift S2I process in bootstrapping an image with pre-installed KIE Containers.
 */
public class KieServerStateFileInit {

    private static final Logger logger = LoggerFactory.getLogger(KieServerStateFileInit.class);
    private static final ServiceLoader<KieServerStateRepository> serverStateRepos = ServiceLoader.load(KieServerStateRepository.class);

    private KieServerStateFileInit() {}

    public static void main(String... args) {
        File serverStateFile = init();
        logger.info("Initialized kie server state file: {}", serverStateFile);
    }

    public static File init() {
        String serverId = KieServerStateRepositoryUtils.getServerId();
        String serverRepoType = StartupStrategyProvider.get().getStrategy().getRepositoryType();
        KieServerEnvironment.setServerId(serverId);
        KieServerEnvironment.setServerName(serverId);
        
        KieServerStateRepository repository = null;
        File serverStateFile = KieServerStateRepositoryUtils.getStateFile();
        
        for (KieServerStateRepository repo : serverStateRepos) {
            if (repo.getClass().getSimpleName().equals(serverRepoType)) {
                repository = repo;
                break;
            }
        }
        if (repository == null) {
            repository = new KieServerStateFileRepository(KieServerStateRepositoryUtils.getFileRepoDir());
            logger.warn("ServiceLoader failed to initiate kie server repository: {}, fall back to KieServerStateFileRepository.", serverRepoType);
        } else {
            logger.info("Initialized with '{}' kie server repository", serverRepoType);
        }
        
        if (KIE_SERVER_STATE_REPO_TYPE_DEFAULT.equals(serverRepoType) &&
                serverStateFile.exists()) {
                throw new IllegalStateException(String.format(
                        "%s already exists. %s should only be used for pre-bootstrapping creation of server state file.",
                        serverStateFile,
                        KieServerStateFileInit.class.getSimpleName()));
        }

        KieServerState serverState = new KieServerState();

        KieServerConfig config = new KieServerConfig();
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.put(KIE_SERVER_STATE_REPO, KieServerStateRepositoryUtils.getFileRepoPath());
        properties.put(KIE_SERVER_ID, serverId);
        properties.put(KIE_SERVER_STATE_IMMUTABLE, "true");
        KieServerStateRepositoryUtils.populateWithProperties(config, properties);
        
        /**
         * This ephemeral system property, which is not persisted at KieServerState,
         * provide a signal to KieServerStateRepo to create a NEW server state within
         * KieServerStateFileInit process only. 
         */
        System.setProperty(KIE_SERVER_STATE_IMMUTABLE_INIT, "true");
        serverState.setConfiguration(config);

        Set<KieContainerResource> containers = new LinkedHashSet<>();
        String serverContainerDeployment = KieServerStateRepositoryUtils.getValue(KIE_SERVER_CONTAINER_DEPLOYMENT, "KIE_SERVER_CONTAINER_DEPLOYMENT", null);
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
}

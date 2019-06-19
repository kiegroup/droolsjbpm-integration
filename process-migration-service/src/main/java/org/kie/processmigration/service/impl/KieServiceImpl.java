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

package org.kie.processmigration.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.kie.processmigration.model.KieServerConfig;
import org.kie.processmigration.model.ProcessInfo;
import org.kie.processmigration.model.RunningInstance;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.model.exceptions.ProcessDefinitionNotFoundException;
import org.kie.processmigration.service.KieService;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.NodeDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.credentials.EnteredCredentialsProvider;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

@ApplicationScoped
@Startup
public class KieServiceImpl implements KieService {

    private static final String HOST = "host";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final long CONFIGURATION_TIMEOUT = 60000;
    private static final long AWAIT_EXECUTOR = 5;
    private static final long RETRY_DELAY = 2;
    private static final Logger logger = LoggerFactory.getLogger(KieServiceImpl.class);

    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    final Map<String, KieServerConfig> configs = new HashMap<>();
    final ConfigKey kieServersKey = new SimpleKey("kieservers");

    @Inject
    ConfigView configView;

    @PostConstruct
    public void loadConfigs() {
        if (configView.hasKeyOrSubkeys(kieServersKey)) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> value = configView.resolve(kieServersKey).as(List.class).getValue();
            value.stream().forEach(this::loadConfig);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(AWAIT_EXECUTOR, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @Override
    public Collection<KieServerConfig> getConfigs() {
        return Collections.unmodifiableCollection(configs.values());
    }

    @Override
    public boolean hasKieServer(String kieServerId) {
        return configs
                .values()
                .stream()
                .anyMatch(config -> config.getId() != null && config.getId().equals(kieServerId));
    }

    @Override
    public ProcessAdminServicesClient getProcessAdminServicesClient(String kieServerId) throws
            InvalidKieServerException {
        return getClient(kieServerId).getServicesClient(ProcessAdminServicesClient.class);
    }

    @Override
    public QueryServicesClient getQueryServicesClient(String kieServerId) throws InvalidKieServerException {
        return getClient(kieServerId).getServicesClient(QueryServicesClient.class);
    }

    @Override
    public List<RunningInstance> getRunningInstances(String containerId, String kieServerId, Integer page, Integer
            pageSize) throws InvalidKieServerException {
        ProcessServicesClient processServicesClient = getProcessServicesClient(kieServerId);
        List<ProcessInstance> instanceList = processServicesClient.findProcessInstances(containerId, page, pageSize);

        int i = 0;
        List<RunningInstance> result = new ArrayList<>();
        for (ProcessInstance instance : instanceList) {
            i++;
            result.add(new RunningInstance(i, instance));
        }

        return result;
    }

    @Override
    public boolean existsProcessDefinition(String containerId, String processId, String kieServerId) throws
            InvalidKieServerException {
        QueryServicesClient queryService = getQueryServicesClient(kieServerId);
        return queryService.findProcessByContainerIdProcessId(containerId, processId) != null;
    }

    @Override
    public ProcessInfo getDefinition(String kieServerId, String containerId, String processId) throws
            ProcessDefinitionNotFoundException, InvalidKieServerException {
        ProcessInfo processInfo = new ProcessInfo();

        //get SVG file
        String svgFile;
        try {
            svgFile = getUIServicesClient(kieServerId).getProcessImage(containerId, processId);
        } catch (KieServicesHttpException e) {
            if (Response.Status.NOT_FOUND.getStatusCode() == e.getHttpCode()) {
                logger.debug("Process definition {}:{} not found in {}", containerId, processId, kieServerId);
                throw new ProcessDefinitionNotFoundException(kieServerId, containerId, processId);
            } else {
                logger.warn("Unable to fetch SVG file from {}", kieServerId, e);
                throw e;
            }
        }

        //Add this replacement here because in react-svgmt, ? and = are not allowed.
        svgFile = svgFile.replaceAll("\\?shapeType=BACKGROUND", "_shapeType_BACKGROUND");
        processInfo.setSvgFile(svgFile);

        ProcessDefinition pd = getProcessServicesClient(kieServerId).getProcessDefinition(containerId, processId);
        if (!pd.getContainerId().equals(containerId)) {
            throw new ProcessDefinitionNotFoundException(kieServerId, containerId, processId);
        }
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        if (pd.getNodes() != null) {
            Collection<NodeDefinition> nodes = pd.getNodes();
            for (NodeDefinition node : nodes) {
                if (node.getType().equals("HumanTaskNode")) {
                    values.add(node.getUniqueId());
                    labels.add(node.getName() + ":" + node.getUniqueId());
                }
            }
        }
        processInfo.setValues(values);
        processInfo.setLabels(labels);
        processInfo.setContainerId(containerId);
        return processInfo;
    }

    private void loadConfig(Map<String, String> config) {
        CredentialsProvider credentialsProvider = new EnteredCredentialsProvider(config.get(USERNAME), config.get(PASSWORD));
        KieServerConfig kieConfig = new KieServerConfig();
        kieConfig.setHost(config.get(HOST))
                .setCredentialsProvider(credentialsProvider);
        configs.put(kieConfig.getHost(), kieConfig);
        try {
            kieConfig.setClient(createKieServicesClient(kieConfig));
            logger.info("Loaded kie server configuration: {}", kieConfig);
        } catch (Exception e) {
            logger.info("Unable to create kie server configuration for {}. Retry asynchronously", kieConfig);
            retryConnection(kieConfig);
        }
        configs.put(kieConfig.getHost(), kieConfig);
        logger.info("Loaded kie server configuration: {}", kieConfig);
    }

    private KieServicesClient createKieServicesClient(KieServerConfig config) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(config.getHost(), config.getCredentialsProvider());
        configuration.setTimeout(CONFIGURATION_TIMEOUT);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

    private KieServicesClient getClient(String kieServerId) throws InvalidKieServerException {
        return configs.values()
                .stream()
                .filter(config -> kieServerId.equals(config.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidKieServerException(kieServerId))
                .getClient();
    }

    private UIServicesClient getUIServicesClient(String kieServerId) throws InvalidKieServerException {
        return getClient(kieServerId).getServicesClient(UIServicesClient.class);
    }

    private ProcessServicesClient getProcessServicesClient(String kieServerId) throws InvalidKieServerException {
        return getClient(kieServerId).getServicesClient(ProcessServicesClient.class);
    }

    void retryConnection(KieServerConfig kieConfig) {
        executorService.schedule(new KieServerClientConnector(kieConfig), RETRY_DELAY, TimeUnit.SECONDS);
    }

    /*
     * Runnable for checks on failed endpoints
     */
    class KieServerClientConnector implements Runnable {

        final KieServerConfig kieConfig;

        KieServerClientConnector(KieServerConfig kieConfig) {
            this.kieConfig = kieConfig;
        }

        @Override
        public void run() {
            logger.debug("Trying to create KieServerClient for {}", kieConfig);
            if (kieConfig.getClient() == null) {
                try {
                    kieConfig.setClient(createKieServicesClient(kieConfig));
                } catch (NoEndpointFoundException e) {
                    logger.warn("Unable to connect to KieServer: {}. The client will try to reconnect in the background", kieConfig);
                } catch (Exception e) {
                    if (e.getCause() != null && NoEndpointFoundException.class.isInstance(e.getCause())) {
                        logger.warn("Unable to connect to KieServer: {}. The client will try to reconnect in the background", kieConfig);
                    } else {
                        logger.warn("Unable to create KieServer client: {}", kieConfig, e);
                    }
                } finally {
                    if (kieConfig.getClient() == null) {
                        logger.debug("KieServerClient for {} could not be created. Retrying...", kieConfig);
                        retryConnection(kieConfig);
                    } else {
                        logger.debug("KieServerClient for {} created.", kieConfig);
                    }
                }
            }
        }
    }
}

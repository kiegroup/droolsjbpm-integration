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

import org.kie.processmigration.model.KieServerConfig;
import org.kie.processmigration.model.ProcessInfo;
import org.kie.processmigration.model.ProcessInfos;
import org.kie.processmigration.model.RunningInstance;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.service.KieService;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.NodeDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.*;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.credentials.EnteredCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
@Startup
public class KieServiceImpl implements KieService {

    private static final String HOST = "host";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final Logger logger = LoggerFactory.getLogger(KieServiceImpl.class);

    private Map<String, KieServerConfig> configs = new HashMap<>();
    private Map<String, KieServicesClient> kieServices = new HashMap<>();
    private ConfigKey kieServersKey = new SimpleKey("kieservers");

    @Inject
    private ConfigView configView;

    @PostConstruct
    public void loadConfigs() {
        if (configView.hasKeyOrSubkeys(kieServersKey)) {
            List<Map<String, String>> value = configView.resolve(kieServersKey).as(List.class).getValue();
            value.stream().forEach(this::loadConfig);
        }
        if (kieServices.size() == 0) {
            logger.warn("Process Instance Migration service running in DISCONNECTED mode. It won't be able to run any migration.");
        }
    }

    @Override
    public Map<String, KieServerConfig> getConfigs() {
        return configs;
    }

    @Override
    public Set<String> getKieServerIDs() {
        return Collections.unmodifiableSet(configs.keySet());
    }

    @Override
    public ProcessAdminServicesClient getProcessAdminServicesClient(String kieServerId) throws InvalidKieServerException {
        if (!kieServices.containsKey(kieServerId)) {
            throw new InvalidKieServerException(kieServerId);
        }
        return kieServices.get(kieServerId).getServicesClient(ProcessAdminServicesClient.class);
    }

    @Override
    public QueryServicesClient getQueryServicesClient(String kieServerId) throws InvalidKieServerException {
        if (!kieServices.containsKey(kieServerId)) {
            throw new InvalidKieServerException(kieServerId);
        }
        return kieServices.get(kieServerId).getServicesClient(QueryServicesClient.class);
    }

    @Override
    public List<RunningInstance> getRunningInstances(String containerId, String kieServerId, Integer page, Integer pageSize) throws InvalidKieServerException {
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
    public ProcessInfos getProcessDefinitions(String sourceContainerId, String sourceProcessId, String targetContainerId, String targetProcessId, String kieServerId) throws InvalidKieServerException {
        ProcessInfos bothInfo = new ProcessInfos();
        ProcessInfo sourceInfo = getProcessInfo(kieServerId, sourceContainerId, sourceProcessId);
        ProcessInfo targetInfo = getProcessInfo(kieServerId, targetContainerId, targetProcessId);
        bothInfo.setSourceInfo(sourceInfo);
        bothInfo.setTargetInfo(targetInfo);
        return bothInfo;
    }

    public boolean existsProcessDefinition(String containerId, String processId, String kieServerId) throws InvalidKieServerException {
        QueryServicesClient queryService = getQueryServicesClient(kieServerId);
        return queryService.findProcessByContainerIdProcessId(containerId, processId) != null;
    }

    private UIServicesClient getUIServicesClient(String kieServerId) throws InvalidKieServerException {
        if (!kieServices.containsKey(kieServerId)) {
            throw new InvalidKieServerException(kieServerId);
        }
        return kieServices.get(kieServerId).getServicesClient(UIServicesClient.class);
    }

    private ProcessServicesClient getProcessServicesClient(String kieServerId) throws InvalidKieServerException {
        if (!kieServices.containsKey(kieServerId)) {
            throw new InvalidKieServerException(kieServerId);
        }
        return kieServices.get(kieServerId).getServicesClient(ProcessServicesClient.class);
    }

    private ProcessInfo getProcessInfo(String kieServerId, String containerId, String processId) throws InvalidKieServerException {
        ProcessInfo processInfo = new ProcessInfo();

        //get SVG file
        String svgFile = getUIServicesClient(kieServerId).getProcessImage(containerId, processId);

        //Add this replacement here because in react-svgmt, ? and = are not allowed.
        svgFile = svgFile.replaceAll("\\?shapeType=BACKGROUND", "_shapeType_BACKGROUND");
        processInfo.setSvgFile(svgFile);

        ProcessDefinition pd = getProcessServicesClient(kieServerId).getProcessDefinition(containerId, processId);
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        if(pd.getNodes() != null) {
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
        KieServerConfig kieConfig = new KieServerConfig();
        kieConfig.setHost(config.get(HOST));
        CredentialsProvider credentialsProvider = new EnteredCredentialsProvider(config.get(USERNAME), config.get(PASSWORD));
        kieConfig.setCredentialsProvider(credentialsProvider);
        try {
            KieServicesClient kieServicesClient = createKieServicesClient(kieConfig);
            kieConfig.setId(kieServicesClient.getServerInfo().getResult().getServerId());
            configs.put(kieConfig.getId(), kieConfig);
            kieServices.put(kieConfig.getId(), kieServicesClient);
            logger.info("Loaded kie server configuration: {}", kieConfig.getId());
        } catch (Exception e) {
            logger.warn("Unable to add KieServer: " + config.toString(), e);
        }
    }

    private KieServicesClient createKieServicesClient(KieServerConfig config) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(config.getHost(), config.getCredentialsProvider());
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

}

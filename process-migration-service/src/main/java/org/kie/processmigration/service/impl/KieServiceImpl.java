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

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.processmigration.model.KieServerConfig;
import org.kie.processmigration.service.KieService;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.admin.impl.ProcessAdminServicesClientImpl;
import org.kie.server.client.credentials.EnteredCredentialsProvider;
import org.kie.server.client.helper.JBPMServicesClientBuilder;
import org.kie.server.client.impl.KieServicesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.api.config.ConfigView;

@ApplicationScoped
public class KieServiceImpl implements KieService {

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PROTOCOL = "protocol";
    private static final String CONTEXT_ROOT = "contextRoot";
    private static final String PATH = "path";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final Logger logger = LoggerFactory.getLogger(KieServiceImpl.class);

    private Map<String, KieServerConfig> configs;

    @Inject
    private ConfigView configView;

    @SuppressWarnings("unchecked")
    @PostConstruct
    private void loadConfigs() {
        List<Map<String, String>> value = configView.resolve("kieservers").as(List.class).getValue();
        value.stream().forEach(this::loadConfig);
    }

    public Map<String, KieServerConfig> getConfigs() {
        return configs;
    }

    public ProcessAdminServicesClient createProcessAdminServicesClient(String kieServerId, CredentialsProvider credentialsProvider) {
        Map<Class<?>, Object> services = createServices(configs.get(kieServerId), credentialsProvider);
        ProcessAdminServicesClientImpl processAdminServicesClient = (ProcessAdminServicesClientImpl) services.get(ProcessAdminServicesClient.class);
        processAdminServicesClient.setOwner((KieServicesClientImpl) createKieServicesClient(kieServerId, credentialsProvider));
        return processAdminServicesClient;
    }

    public KieServicesClient createKieServicesClient(String kieServerId, CredentialsProvider credentialsProvider) {
        return KieServicesFactory.newKieServicesRestClient(configs.get(kieServerId).getUrl(), credentialsProvider);
    }

    private String getServerId(KieServerConfig config) {
        KieServicesClient restClient = KieServicesFactory.newKieServicesRestClient(config.getUrl(), config.getCredentialsProvider());
        return restClient.getServerInfo().getResult().getServerId();
    }

    private Map<Class<?>, Object> createServices(KieServerConfig config, CredentialsProvider credentialsProvider) {
        return new JBPMServicesClientBuilder().build(KieServicesFactory.newRestConfiguration(config.getUrl(), credentialsProvider), this.getClass().getClassLoader());
    }

    private void loadConfig(Map<String, String> config) {
        KieServerConfig kieConfig = new KieServerConfig();
        kieConfig.setHost(config.get(HOST));
        kieConfig.setPort(Integer.valueOf(config.get(PORT)));
        kieConfig.setContextRoot(config.get(CONTEXT_ROOT));
        kieConfig.setProtocol(config.get(PROTOCOL));
        kieConfig.setPath(config.get(PATH));
        CredentialsProvider credentialsProvider = new EnteredCredentialsProvider(config.get(USERNAME), config.get(PASSWORD));
        kieConfig.setCredentialsProvider(credentialsProvider);
        kieConfig.setId(getServerId(kieConfig));
        configs.put(kieConfig.getId(), kieConfig);
        logger.error("Loaded kie server configuration: {}", kieConfig.getId());
    }

}

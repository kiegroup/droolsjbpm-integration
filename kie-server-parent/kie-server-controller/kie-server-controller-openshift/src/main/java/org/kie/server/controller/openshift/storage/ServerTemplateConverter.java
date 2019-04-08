/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.openshift.storage;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.CAPABILITY_BPM;
import static org.kie.server.api.KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED;
import static org.kie.server.api.KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED;
import static org.kie.server.api.KieServerConstants.KIE_OPTAPLANNER_SERVER_EXT_DISABLED;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_LOCATION;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_MODE;
import static org.kie.server.api.KieServerConstants.PCFG_KIE_BASE;
import static org.kie.server.api.KieServerConstants.PCFG_KIE_SESSION;
import static org.kie.server.api.KieServerConstants.PCFG_MERGE_MODE;
import static org.kie.server.api.KieServerConstants.PCFG_RUNTIME_STRATEGY;
import static org.kie.server.controller.api.KieServerControllerConstants.KIE_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.UNKNOWN;

public class ServerTemplateConverter {

    private static final Logger logger = LoggerFactory.getLogger(ServerTemplateConverter.class);
    private static final String SERVICE_PORT_ENV_SUFFIX = "_SERVICE_PORT";
    private static final String SERVICE_HOST_ENV_SUFFIX = "_SERVICE_HOST";
    private static final String SERVICE_PATH = "/services/rest/server";
    protected static final boolean PREFER_KIESERVER_SERVICE =
            Boolean.parseBoolean(System.getProperty(KIE_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE, "true"));

    private ServerTemplateConverter() {}

    public static ServerTemplate fromState(KieServerState state) {
        if (state == null) {
            return null;
        }
        if (state.getConfiguration() == null) {
            throw new IllegalArgumentException("Kie server configuration can not be empty.");
        }
        ServerTemplate template = new ServerTemplate();
        String id = state.getConfiguration().getConfigItemValue(KIE_SERVER_ID);
        String name = state.getConfiguration().getConfigItemValue(KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME, id);
        String mode = state.getConfiguration().getConfigItemValue(KIE_SERVER_MODE, KieServerMode.DEVELOPMENT.name());
        Optional<String> urlOpt = resolveServerUrl(state);

        template.setId(id);
        template.setName(name);
        urlOpt.ifPresent(url -> template.addServerInstance(new ServerInstanceKey(id, id, id, url)));

        for (KieContainerResource conRes : state.getContainers()) {
            Map<Capability, ContainerConfig> configs = new EnumMap<>(Capability.class);

            KieScannerResource scanner = conRes.getScanner();
            if (scanner != null) {
                configs.put(Capability.RULE, new RuleConfig(scanner.getPollInterval(), scanner.getStatus()));
            }
            ProcessConfig pcfg = null;
            for (KieServerConfigItem item : conRes.getConfigItems()) {
                if (CAPABILITY_BPM.equals(item.getType())) {
                    pcfg = pcfg == null ? new ProcessConfig() : pcfg;
                    if (PCFG_KIE_BASE.equals(item.getName())) {
                        pcfg.setKBase(item.getValue());
                    }
                    if (PCFG_KIE_SESSION.equals(item.getName())) {
                        pcfg.setKSession(item.getValue());
                    }
                    if (PCFG_MERGE_MODE.equals(item.getName())) {
                        pcfg.setMergeMode(item.getValue());
                    }
                    if (PCFG_RUNTIME_STRATEGY.equals(item.getName())) {
                        pcfg.setRuntimeStrategy(item.getValue());
                    }
                }
            }
            if (pcfg != null) {
                configs.put(Capability.PROCESS, pcfg);
            }

            template.addContainerSpec(new ContainerSpec(conRes.getContainerId(),
                                                        conRes.getContainerAlias(),
                                                        new ServerTemplateKey(id, id),
                                                        conRes.getReleaseId(),
                                                        conRes.getStatus(), configs));
        }

        List<String> capabilities = Stream.of(Capability.values()).map(Capability::name).collect(Collectors.toList());
        KieServerConfig kcfg = state.getConfiguration();
        if (kcfg.getConfigItem(KIE_JBPM_SERVER_EXT_DISABLED) != null) {
            capabilities.remove(Capability.PROCESS.name());
        }
        if (kcfg.getConfigItem(KIE_DROOLS_SERVER_EXT_DISABLED) != null) {
            capabilities.remove(Capability.RULE.name());
        }
        if (kcfg.getConfigItem(KIE_OPTAPLANNER_SERVER_EXT_DISABLED) != null) {
            capabilities.remove(Capability.PLANNING.name());
        }

        template.setCapabilities(capabilities);
        //https://issues.jboss.org/projects/RHPAM/issues/RHPAM-1975
        template.setMode(KieServerMode.valueOf(mode.toUpperCase()));

        return template;
    }

    public static KieServerState toState(ServerTemplate template) {
        String id = template.getId();
        String name = template.getName();
        KieServerMode mode = template.getMode() == null ? KieServerMode.DEVELOPMENT : template.getMode();
        KieServerState state = new KieServerState();
        KieServerConfig config = new KieServerConfig();
        Set<KieContainerResource> containers = new HashSet<>();

        config.addConfigItem(new KieServerConfigItem(KIE_SERVER_ID, id, String.class.getName()));
        config.addConfigItem(new KieServerConfigItem(KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME, name, String.class.getName()));
        config.addConfigItem(new KieServerConfigItem(KIE_SERVER_MODE, mode.name(), String.class.getName()));

        if (template.getServerInstance(id) != null) {
            config.addConfigItem(new KieServerConfigItem(KIE_SERVER_LOCATION,
                                                         template.getServerInstance(id).getUrl(), String.class.getName()));
        }
        List<String> capabilities = template.getCapabilities();
        for (Capability cap : Arrays.asList(Capability.values())) {
            switch (cap) {
                case PROCESS:
                    if (!capabilities.contains(cap.name()))
                        config.addConfigItem(
                                             new KieServerConfigItem(KIE_JBPM_SERVER_EXT_DISABLED,
                                                                     "true", String.class.getName()));
                    break;
                case RULE:
                    if (!capabilities.contains(cap.name()))
                        config.addConfigItem(
                                             new KieServerConfigItem(KIE_DROOLS_SERVER_EXT_DISABLED,
                                                                     "true", String.class.getName()));
                    break;
                case PLANNING:
                    if (!capabilities.contains(cap.name()))
                        config.addConfigItem(
                                             new KieServerConfigItem(KIE_OPTAPLANNER_SERVER_EXT_DISABLED,
                                                                     "true", String.class.getName()));
                    break;

                default:
                    break;
            }
        }

        for (ContainerSpec conSpec : template.getContainersSpec()) {
            KieContainerResource conRes = new KieContainerResource(conSpec.getId(), conSpec.getReleasedId());
            conRes.setContainerAlias(conSpec.getContainerName());
            conRes.setStatus(conSpec.getStatus());

            for (Entry<Capability, ContainerConfig> conCfgEntry : conSpec.getConfigs().entrySet()) {
                switch (conCfgEntry.getKey()) {
                    case PROCESS:
                        ProcessConfig pcfg = (ProcessConfig) conCfgEntry.getValue();
                        conRes.addConfigItem(new KieServerConfigItem(PCFG_KIE_BASE, pcfg.getKBase(), CAPABILITY_BPM));
                        conRes.addConfigItem(new KieServerConfigItem(PCFG_KIE_SESSION, pcfg.getKSession(), CAPABILITY_BPM));
                        conRes.addConfigItem(new KieServerConfigItem(PCFG_MERGE_MODE, pcfg.getMergeMode(), CAPABILITY_BPM));
                        conRes.addConfigItem(new KieServerConfigItem(PCFG_RUNTIME_STRATEGY, pcfg.getRuntimeStrategy(), CAPABILITY_BPM));
                        break;
                    case RULE:
                        RuleConfig rcfg = (RuleConfig) conCfgEntry.getValue();
                        conRes.setScanner(new KieScannerResource(rcfg.getScannerStatus(), rcfg.getPollInterval()));
                        break;
                    default:
                        break;
                }
            }

            containers.add(conRes);
        }

        state.setConfiguration(config);
        state.setContainers(containers);

        return state;
    }

    protected static Optional<String> resolveServerUrl(KieServerState state) {
        String serverId = state.getConfiguration().getConfigItemValue(KIE_SERVER_ID, UNKNOWN);
        String serviceName = state.getConfiguration().getConfigItemValue(KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME, UNKNOWN);
        Optional<String> resolvedUrl = Optional.ofNullable(state.getConfiguration().getConfigItemValue(KIE_SERVER_LOCATION));

        if (PREFER_KIESERVER_SERVICE && resolvedUrl.isPresent()) {
            String envPrefix = serviceName.replace('-', '_').toUpperCase();
            String servicePortEnv = System.getenv(envPrefix.concat(SERVICE_PORT_ENV_SUFFIX));
            String serviceHostEnv = System.getenv(envPrefix.concat(SERVICE_HOST_ENV_SUFFIX));
            servicePortEnv = servicePortEnv == null ? "8080" : servicePortEnv;
            if (serviceHostEnv != null) {
                resolvedUrl = Optional.of(new StringBuffer("http://")
                                             .append(serviceHostEnv)
                                             .append(":")
                                             .append(servicePortEnv)
                                             .append(SERVICE_PATH)
                                             .toString());

            } else {
                logger.warn("Environment variable: [{}] defined by OpenShift Cluster Service for KIE server: [{}] not found.", 
                            envPrefix.concat(SERVICE_HOST_ENV_SUFFIX), serverId);
                if (resolvedUrl.isPresent()) { 
                    logger.warn("Use kie server location [{}]", resolvedUrl.get());
                } else {
                    logger.warn("Undefined kie server location");
                }
             }
        }
        return resolvedUrl;
    }

}

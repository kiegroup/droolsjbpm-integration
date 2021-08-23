/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.router;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class KieServerRouterEnvironment {

    private String routerId;
    private String routerName;
    private String routerHost;
    private Integer port;
    private Integer sslPort;
    private String routerExternalUrl;

    private Boolean tlsEnabled;
    private String keystorePath;
    private String keystorePassword;
    private String keystoreKey;

    private String repositoryDir;
    private String identityProvider;

    private String kieControllerUrl;
    private String kieControllerUser;
    private String kieControllerPwd;
    private String kieControllerToken;

    private Boolean configFileWatcherEnabled;
    private Long configFileWatcherInterval;

    private Long kieControllerAttemptInterval;
    private Integer kieControllerRecoveryAttemptLimit;

    private Boolean managementPassword;

    public KieServerRouterEnvironment() {
        reload();
    }
    public void reload() {
        loadFromSystemEnv();
        loadFromProperties();
        tlsEnabled = keystorePath != null && !keystorePath.isEmpty();
    }

    public void loadFromSystemEnv() {
        routerId = System.getProperty(KieServerRouterConstants.ROUTER_ID, "kie-server-router");
        routerName = System.getProperty(KieServerRouterConstants.ROUTER_NAME, "KIE Server Router");
        routerHost = System.getProperty(KieServerRouterConstants.ROUTER_HOST, "localhost");
        port = Integer.getInteger(KieServerRouterConstants.ROUTER_PORT, KieServerRouterConstants.DEFAULT_PORT_NUM);
        sslPort = Integer.getInteger(KieServerRouterConstants.ROUTER_PORT_TLS, KieServerRouterConstants.DEFAULT_PORT_TLS_NUM);
        routerExternalUrl = System.getProperty(KieServerRouterConstants.ROUTER_EXTERNAL_URL);

        keystorePath = System.getProperty(KieServerRouterConstants.ROUTER_KEYSTORE);
        keystorePassword = System.getProperty(KieServerRouterConstants.ROUTER_KEYSTORE_PASSWORD);
        keystoreKey = System.getProperty(KieServerRouterConstants.ROUTER_KEYSTORE_KEYALIAS);

        repositoryDir = System.getProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, ".");
        identityProvider = System.getProperty(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER, "default");

        kieControllerUrl = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER);
        kieControllerUser = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_USER, "kieserver");
        kieControllerPwd = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_PASSWORD, "kieserver1!");
        kieControllerToken = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_TOKEN);

        configFileWatcherEnabled = Boolean.getBoolean(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED);
        configFileWatcherInterval = Long.getLong(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL, 30000L);

        kieControllerAttemptInterval = Long.getLong(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL, 10L);
        kieControllerRecoveryAttemptLimit = Integer.getInteger(KieServerRouterConstants.KIE_SERVER_RECOVERY_ATTEMPT_LIMIT, -1);

        managementPassword = Boolean.getBoolean(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED);
    }

    public void loadFromProperties() {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(new File(System.getProperty(KieServerRouterConstants.ROUTER_CONFIG_FILE)))) {
            props.load(is);

            routerId = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_ID, routerId);
            routerName = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_NAME, routerName);
            routerHost = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_HOST, routerHost);
            port =  props.containsKey(KieServerRouterConstants.ROUTER_PORT) ? Integer.parseInt((String) props.get(KieServerRouterConstants.ROUTER_PORT)) : port;
            sslPort =  props.containsKey(KieServerRouterConstants.ROUTER_PORT_TLS) ? Integer.parseInt((String) props.get(KieServerRouterConstants.ROUTER_PORT_TLS)) : sslPort;

            routerExternalUrl = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_EXTERNAL_URL, routerExternalUrl);

            keystorePath = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_KEYSTORE, keystorePath);
            keystorePassword = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_KEYSTORE_PASSWORD, keystorePassword);
            keystoreKey = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_KEYSTORE_KEYALIAS, keystoreKey);

            repositoryDir = (String) props.getOrDefault(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repositoryDir);
            identityProvider = (String) props.getOrDefault(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER, identityProvider);

            kieControllerUrl = (String) props.getOrDefault(KieServerRouterConstants.KIE_CONTROLLER, kieControllerUrl);
            kieControllerUser = (String) props.getOrDefault(KieServerRouterConstants.KIE_CONTROLLER_USER, kieControllerUser);
            kieControllerPwd = (String) props.getOrDefault(KieServerRouterConstants.KIE_CONTROLLER_PASSWORD, kieControllerPwd);
            kieControllerToken = (String) props.getOrDefault(KieServerRouterConstants.KIE_CONTROLLER_TOKEN, kieControllerToken);

            configFileWatcherEnabled =  props.containsKey(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED) ? Boolean.parseBoolean((String) props.get(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED)) : configFileWatcherEnabled;
            configFileWatcherInterval =  props.containsKey(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL) ? Long.parseLong((String) props.get(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL)) : configFileWatcherInterval;

            kieControllerAttemptInterval =  props.containsKey(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL) ? Long.parseLong((String) props.get(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL)) : kieControllerAttemptInterval;
            kieControllerRecoveryAttemptLimit =  props.containsKey(KieServerRouterConstants.KIE_SERVER_RECOVERY_ATTEMPT_LIMIT) ? Integer.parseInt((String) props.get(KieServerRouterConstants.KIE_SERVER_RECOVERY_ATTEMPT_LIMIT)) : kieControllerRecoveryAttemptLimit;

            managementPassword = props.containsKey(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED) ? Boolean.parseBoolean((String) props.get(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED)) : managementPassword;
        } catch(Exception e) {
            // do nothing
        }

    }
    
    public String getRouterId() {
        return routerId;
    }

    public String getRouterName() {
        return routerName;
    }

    public String getRouterHost() {
        return routerHost;
    }

    public int getPort() {
        return port;
    }

    public int getSslPort() {
        return sslPort;
    }

    public String getRouterExternalUrl() {

        if (routerExternalUrl == null) {
            StringBuilder sb = new StringBuilder();
            boolean httpEnabled = isHttpEnabled();
            if (httpEnabled) {
                sb.append("http://");
            } else {
                sb.append("https://");
            }
            sb.append(routerHost);
            sb.append(":");
            if (httpEnabled) {
                sb.append(port);
            } else {
                sb.append(sslPort);
            }
            routerExternalUrl = sb.toString();
        }
        return routerExternalUrl;
    }

    public boolean isHttpEnabled() {
        return isValidPort(port);
    }

    private boolean isValidPort (int port) {
        return port > 0;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeystoreKey() {
        return keystoreKey;
    }

    public String getRepositoryDir() {
        return repositoryDir;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public boolean hasKieControllerUrl() {
        return kieControllerUrl != null;
    }

    public String getKieControllerUrl() {
        return kieControllerUrl;
    }

    public String getKieControllerUser() {
        return kieControllerUser;
    }

    public String getKieControllerPwd() {
        return kieControllerPwd;
    }

    public boolean hasKieControllerToken() {
        return kieControllerToken != null;
    }
    
    public String getKieControllerToken() {
        return kieControllerToken;
    }

    public boolean isConfigFileWatcherEnabled() {
        return configFileWatcherEnabled;
    }

    public long getConfigFileWatcherInterval() {
        return configFileWatcherInterval;
    }

    public long getKieControllerAttemptInterval() {
        return kieControllerAttemptInterval;
    }

    public int getKieControllerRecoveryAttemptLimit() {
        return kieControllerRecoveryAttemptLimit;
    }

    public boolean isManagementSecured() {
        return managementPassword;
    }



}

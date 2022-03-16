/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.server.router.handlers.AdminHttpHandler;
import org.kie.server.router.handlers.ContainersHttpHandler;
import org.kie.server.router.handlers.DocumentsHttpHandler;
import org.kie.server.router.handlers.JobsHttpHandler;
import org.kie.server.router.handlers.KieServerInfoHandler;
import org.kie.server.router.handlers.OptionsHttpHandler;
import org.kie.server.router.handlers.QueriesDataHttpHandler;
import org.kie.server.router.handlers.QueriesHttpHandler;
import org.kie.server.router.identity.IdentityService;
import org.kie.server.router.proxy.KieServerProxyClient;
import org.kie.server.router.repository.FileRepository;
import org.kie.server.router.spi.ConfigRepository;
import org.kie.server.router.utils.HttpUtils;
import org.kie.server.router.utils.SSLContextBuilder;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;

import static org.kie.server.router.KieServerRouterConstants.DEFAULT_PORT_TLS_NUM;
import static org.kie.server.router.KieServerRouterConstants.ROUTER_HOST;
import static org.kie.server.router.KieServerRouterConstants.ROUTER_PORT;
import static org.kie.server.router.KieServerRouterConstants.ROUTER_PORT_TLS;
import static org.kie.server.router.KieServerRouterResponsesUtil.buildServerInfo;

public class KieServerRouter {

    public static final String CMD_ADD_USER = "addUser";
    public static final String CMD_REMOVE_USER = "removeUser";

    private static final Logger log = Logger.getLogger(KieServerRouter.class);

    private ServiceLoader<ConfigRepository> configRepositoryServiceLoader = ServiceLoader.load(ConfigRepository.class);

    private Undertow server;
    private ConfigRepository repository;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);
    private ScheduledFuture<?> controllerConnectionAttempts;
    private KieServerRouterEnvironment env;

    private ConfigurationManager configurationManager;

    public KieServerRouter() {
        this(new KieServerRouterEnvironment());
    }

    public KieServerRouter(KieServerRouterEnvironment env) {
        configRepositoryServiceLoader.forEach(repo -> repository = repo);
        this.env = env;
        this.repository = new FileRepository(env);
        log.info("KIE Server router repository implementation is " + repository);
    }

    private KieServerRouterEnvironment environment() {
        return env;
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        Option addInstanceOption = Option.builder(CMD_ADD_USER)
                                         .desc(CMD_ADD_USER + " <user> <password>")
                                         .hasArg(true)
                                         .numberOfArgs(2)
                                         .build();
        options.addOption(addInstanceOption);
        Option removeInstanceOption = Option.builder(CMD_REMOVE_USER)
                                            .desc(CMD_REMOVE_USER + " <user>")
                                            .hasArg(true)
                                            .build();
        options.addOption(removeInstanceOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        KieServerRouter router = new KieServerRouter();
        if (cmd.hasOption(CMD_ADD_USER)) {
            String[] values = cmd.getOptionValues(CMD_ADD_USER);
            router.getIdentityService().addKieServerInstance(values[0], values[1]);
            log.infof("User <%1$s> added", values[0]);
            return;
        }
        if (cmd.hasOption(CMD_REMOVE_USER)) {
            String value = cmd.getOptionValue(CMD_REMOVE_USER);
            router.getIdentityService().removeKieServerInstance(value);
            log.infof("User <%1$s> removed", value);
            return;
        }

        // default behavior
        router.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                router.stop();
            }
        });
    }

    public void start(String host,
                      Integer port,
                      ConfigurationListener... listeners) {
        start(host,
              port,
              DEFAULT_PORT_TLS_NUM,
              listeners);
    }

    public void start(ConfigurationListener... listeners) {
        start(environment().getRouterHost(), environment().getPort(), environment().getSslPort(), listeners);
    }

    public void start(String host,
                      Integer port,
                      Integer portTls,
                      ConfigurationListener... listeners) {
        System.setProperty(ROUTER_HOST, host);
        System.setProperty(ROUTER_PORT, port.toString());
        System.setProperty(ROUTER_PORT_TLS, portTls.toString());
        
        boolean isHttpEnabled = environment().isHttpEnabled();

        configurationManager = new ConfigurationManager(environment(), repository, executorService);
        Configuration configuration = configurationManager.getConfiguration();

        for (ConfigurationListener listener : listeners) {
            configurationManager.getConfiguration().addListener(listener);
        }

        // setup config file watcher to be updated when changes are discovered
        if (environment().isConfigFileWatcherEnabled()) {
            configurationManager.startWatcher();
        }

        AdminHttpHandler adminHandler = new AdminHttpHandler(configurationManager);
        final KieServerProxyClient proxyClient = new KieServerProxyClient(configurationManager);
        Map<String, List<String>> perContainer = configuration.getHostsPerContainer();

        for (Map.Entry<String, List<String>> entry : perContainer.entrySet()) {
            Set<String> uniqueUrls = new LinkedHashSet<>(entry.getValue());
            uniqueUrls.forEach(url -> {
                proxyClient.addContainer(entry.getKey(),
                                         URI.create(url));
            });
        }

        HttpHandler notFoundHandler = ResponseCodeHandler.HANDLE_404;
        ProxyHandler proxyHandler = ProxyHandler
        .builder()
        .setProxyClient(proxyClient)
        .setMaxRequestTime(-1)
        .setRewriteHostHeader(true)
        .setReuseXForwarded(false)        
        .setNext(new OptionsHttpHandler(notFoundHandler, configurationManager))        
        .build();
        
        PathHandler pathHandler = Handlers.path(proxyHandler);
        pathHandler.addPrefixPath("/queries/definitions",
                                  new QueriesDataHttpHandler(notFoundHandler, configurationManager));
        pathHandler.addPrefixPath("/queries",
                                  new QueriesHttpHandler(notFoundHandler, configurationManager));
        pathHandler.addPrefixPath("/jobs",
                                  new JobsHttpHandler(proxyHandler, configurationManager));
        pathHandler.addPrefixPath("/documents",
                                  new DocumentsHttpHandler(notFoundHandler, configurationManager));
        pathHandler.addExactPath("/containers",
                                 new ContainersHttpHandler(notFoundHandler, configurationManager));

        if (environment().isManagementSecured()) {
            IdentityManager idm = getIdentityService();
            HttpHandler authenticationCallHandler = new AuthenticationCallHandler(adminHandler);
            HttpHandler authenticationConstraintHandler = new AuthenticationConstraintHandler(authenticationCallHandler);
            List<AuthenticationMechanism> mechanisms = Collections.singletonList(new BasicAuthenticationMechanism("KieServerRouterRealm"));
            AuthenticationMechanismsHandler authenticationMechanismsHandler = new AuthenticationMechanismsHandler(authenticationConstraintHandler, mechanisms);
            SecurityInitialHandler securityInitialHandler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, idm, authenticationMechanismsHandler);
            pathHandler.addPrefixPath("/mgmt", securityInitialHandler);
        } else {
            pathHandler.addPrefixPath("/mgmt", adminHandler);
        }


        
        pathHandler.addExactPath("/", new KieServerInfoHandler(environment()));

        HttpHandler blockingHandler = new BlockingHandler(pathHandler);

        // main server configuration
        Undertow.Builder undertowBuilder = Undertow.builder();
        
        
        
        if (isHttpEnabled) {
            undertowBuilder.addHttpListener(port, host);
        }

        if (environment().isTlsEnabled()) {
            SSLContext sslContext = SSLContextBuilder.builder()
                    .setKeyStorePath(environment().getKeystorePath())
                    .setKeyStorePassword(environment().getKeystorePassword())
                    .setKeyAlias(environment().getKeystoreKey()).build();
            undertowBuilder = undertowBuilder.addHttpsListener(portTls,
                                                               host,
                                                               sslContext);
        }
        
        if (!isHttpEnabled && !environment().isTlsEnabled()) {
            throw new IllegalStateException(
                    "HTTP listener was disabled (by setting HTTP port to 0 or lower ) and TLS wasn't configured, no listener is available to handle requests");
        }
        
        server = undertowBuilder
                .setHandler(blockingHandler)
                .build();
        server.start();
        
        
        if (log.isInfoEnabled()) {
            logServerInfo("KieServerRouter started on: ", host, port, portTls);
        }
        
        connectToController(configurationManager);
    }
    
    public void stop() {
        stop(false);
        
    }

    public void stop(boolean clean) {
        configurationManager.close();

        executorService.shutdownNow();
        disconnectToController();
        if (server != null) {
            server.stop();
            repository.close();
            if (clean) {
                repository.clean();
            }
            if (log.isInfoEnabled()) {
                logServerInfo("KieServerRouter stopped on: ", System.getProperty(ROUTER_HOST),
                        Integer.getInteger(ROUTER_PORT), Integer.getInteger(ROUTER_PORT_TLS));
            }
            
            
        } else {
            log.error("KieServerRouter was not started");
        }
    }
    
    private void logServerInfo(String prefix, String host, int port, int portTls) {
        StringBuilder sb = new StringBuilder(prefix);
        if (environment().isHttpEnabled()) {
            sb.append(host).append(':').append(port);
        }
        if (environment().isTlsEnabled()) {
            sb.append(" (TLS) ").append(host).append(':').append(portTls);
        }
        log.info(sb);
    }


    protected void connectToController(ConfigurationManager configurationManager) {
        if (!environment().hasKieControllerUrl()) {
            return;
        }
        try {
            String jsonResponse = HttpUtils.putHttpCall(environment(), environment().getKieControllerUrl() + "/server/" + environment().getRouterId(),  buildServerInfo(environment()));
            log.debugf("Controller response :: %s", jsonResponse);
            boostrapFromControllerResponse(configurationManager, jsonResponse);
            log.infof("KieServerRouter connected to controller at " + environment().getKieControllerUrl());
        } catch (Exception e) {
            log.error("Error when connecting to controller at " + environment().getKieControllerUrl() + " due to " + e.getMessage());
            log.debug(e);
            controllerConnectionAttempts = executorService.scheduleAtFixedRate(() -> tryToConnectToController(configurationManager),
                    environment().getKieControllerAttemptInterval(),
                    environment().getKieControllerAttemptInterval(),
                    TimeUnit.SECONDS);
        }
    }

    private void tryToConnectToController(ConfigurationManager configurationManager) {
        try {
            String jsonResponse = HttpUtils.putHttpCall(environment(), environment().getKieControllerUrl() + "/server/" + environment().getRouterId(), buildServerInfo(environment()));
            log.debugf("Controller response :: %s", jsonResponse);
            boostrapFromControllerResponse(configurationManager, jsonResponse);

            controllerConnectionAttempts.cancel(false);
            log.infof("KieServerRouter connected to controller at " + environment().getKieControllerUrl());
        } catch (Exception ex) {
            log.error("Error when connecting to controller at " + environment().getKieControllerUrl() +
                              " next attempt in " + environment().getKieControllerAttemptInterval() + " " + TimeUnit.SECONDS.toString());
            log.debug(ex);
        }
    }

    protected void disconnectToController() {
        if (!environment().hasKieControllerUrl()) {
            return;
        }
        try {
            HttpUtils.deleteHttpCall(environment(), environment().getKieControllerUrl() + "/server/" + environment().getRouterId() + "/?location=" + URLEncoder.encode(environment().getRouterExternalUrl(), "UTF-8"));
            log.infof("KieServerRouter disconnected from controller at " + environment().getKieControllerUrl());
        } catch (Exception e) {
            log.error("Error when disconnecting from controller at " + environment().getKieControllerUrl(),
                      e);
        }
    }

    protected void boostrapFromControllerResponse(ConfigurationManager configurationManager, String jsonResponse) throws JSONException {
        List<String> containers = new ArrayList<>();

        JSONObject serverConfig = new JSONObject(jsonResponse);
        try {
            JSONArray sourceList = serverConfig.getJSONArray("containers");

            for (int i = 0; i < sourceList.length(); i++) {
                JSONObject container = sourceList.getJSONObject(i);
                containers.add(container.getString("container-id"));
            }
        } catch (JSONException e) {
            // if the server template did not exist the containers can be null, meaning not JSONArray
            log.debug("Error when getting list of containers:: " + e.getMessage(), e);
        }

        configurationManager.addControllerContainers(containers);
    }

    private static class IdentityServiceNotFound extends RuntimeException {

        private static final long serialVersionUID = 7156962325493936307L;

        public IdentityServiceNotFound(String msg) {
            super(msg);
        }
    }

    public IdentityService getIdentityService() {
        ServiceLoader<IdentityService> services = ServiceLoader.load(IdentityService.class, Thread.currentThread().getContextClassLoader());
        Iterator<IdentityService> iterator = services.iterator();
        while (iterator.hasNext()) {
            IdentityService identityService = iterator.next();
            if (env.getIdentityProvider().contentEquals(identityService.id())) {
                return identityService;
            }
        }
        throw new IdentityServiceNotFound("Identity Provider " + env.getIdentityProvider() + " not found !");
    }

}

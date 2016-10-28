/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.shared;

import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.kie.api.KieServices;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;

public class KieServerExecutor {

    protected TJWSEmbeddedJaxrsServer server;

    // Need to hold kie server instance because we need to manually handle startup/shutdown behavior defined in
    // context listener org.kie.server.services.Bootstrap. Embedded server doesn't support ServletContextListeners.
    private KieServerImpl kieServer;

    private static SimpleDateFormat serverIdSuffixDateFormat = new SimpleDateFormat("yyyy-MM-DD-HHmmss_SSS");

    public void startKieServer() {
        if (server != null) {
            throw new RuntimeException("Kie execution server is already created!");
        }

        setKieServerProperties();
        registerKieServerId();

        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(TestConfig.getKieServerAllocatedPort());
        server.start();

        addServerSingletonResources();
    }
    private void setKieServerProperties() {
        System.setProperty(KieServerConstants.CFG_BYPASS_AUTH_USER, "true");
        System.setProperty(KieServerConstants.CFG_HT_CALLBACK, "custom");
        System.setProperty(KieServerConstants.CFG_HT_CALLBACK_CLASS, "org.kie.server.integrationtests.jbpm.util.FixedUserGroupCallbackImpl");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_DS, "jdbc/jbpm-ds");
        System.setProperty(KieServerConstants.CFG_PERSISTANCE_TM, "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");
        System.setProperty(KieServerConstants.KIE_SERVER_CONTROLLER, TestConfig.getControllerHttpUrl());
        System.setProperty(KieServerConstants.CFG_KIE_CONTROLLER_USER, TestConfig.getUsername());
        System.setProperty(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, TestConfig.getPassword());
        System.setProperty(KieServerConstants.KIE_SERVER_LOCATION, TestConfig.getEmbeddedKieServerHttpUrl());
        System.setProperty(KieServerConstants.KIE_SERVER_STATE_REPO, "./target");

        // kie server policy settings
        System.setProperty(KieServerConstants.KIE_SERVER_ACTIVATE_POLICIES, "KeepLatestOnly");
        System.setProperty("policy.klo.interval", "5000");
    }
    private void registerKieServerId() {
        if (KieServerEnvironment.getServerId() == null) {
            KieServerEnvironment.setServerId(KieServerBaseIntegrationTest.class.getSimpleName() + "@" + serverIdSuffixDateFormat.format(new Date()));
            KieServerEnvironment.setServerName("KieServer");
        }
    }
    private void addServerSingletonResources() {
        kieServer = new KieServerImpl();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerRestImpl(kieServer));

        List<KieServerExtension> extensions = kieServer.getServerExtensions();

        for (KieServerExtension extension : extensions) {
            List<Object> components = extension.getAppComponents(SupportedTransports.REST);
            for (Object component : components) {
                server.getDeployment().getRegistry().addSingletonResource(component);
            }
        }
    }

    public void stopKieServer() {
        if (server == null) {
            throw new RuntimeException("Kie execution server is already stopped!");
        }
        kieServer.destroy();
        // The KieServices instance that was seen by the kieserver, will never be seen again at this point
        ((KieServicesImpl) KieServices.Factory.get()).nullAllContainerIds();
        server.stop();
        server = null;
    }
}

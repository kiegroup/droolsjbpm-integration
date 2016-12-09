/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.shared.basetests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieControllerExecutor;
import org.kie.server.integrationtests.shared.KieServerExecutor;
import org.kie.server.integrationtests.shared.KieServerRouterExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieServerBaseIntegrationTest {

    protected static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    protected KieServicesClient client;
    protected static KieControllerExecutor controller;
    protected static KieServerExecutor server;
    protected static KieServerRouterExecutor router;

    protected static final long DEFAULT_TIMEOUT = 60000;

    @BeforeClass
    public static void setupClass() throws Exception {
        router = new KieServerRouterExecutor();
        router.startKieRouter();
        if (TestConfig.isLocalServer()) {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
            controller = new KieControllerExecutor();
            controller.startKieController();
            server = new KieServerExecutor();
            server.startKieServer();
        }
        setupCustomSettingsXml();
        disposeAllContainers();
    }

    private static void setupCustomSettingsXml() {
        if (!TestConfig.isLocalServer()) {
            String deploymentSettings = TestConfig.getKieClientDeploymentSettings();

            if (deploymentSettings == null) {
                deploymentSettings = ClassLoader.class.getResource("/kie-server-testing-client-deployment-settings.xml").getFile();
            }

            System.setProperty(KieServerConstants.CFG_KIE_MVN_SETTINGS, deploymentSettings);
        }
    }

    @Before
    public void setup() throws Exception {
        client = createDefaultClient();
    }

    @AfterClass
    public static void tearDown() {
        if (TestConfig.isLocalServer()) {
            server.stopKieServer();
            controller.stopKieController();
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        }
        router.stopKieRouter();
    }

    protected static void disposeAllContainers() {
        KieServicesClient client = createDefaultStaticClient();
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, response.getType());
        List<KieContainerResource> containers = response.getResult().getContainers();
        if (containers != null) {
            for (KieContainerResource container : containers) {
                client.disposeContainer(container.getContainerId());
            }
        }
    }

    protected static void createContainer(String containerId, ReleaseId releaseId) {
        createContainer(containerId, releaseId, null);
    }

    protected static void createContainer(String containerId, ReleaseId releaseId, KieServerConfigItem... configItems) {
        createContainer(containerId, releaseId, null, configItems);
    }
    protected static void createContainer(String containerId, ReleaseId releaseId, String alias, KieServerConfigItem... configItems) {
        KieServicesClient client = createDefaultStaticClient();
        KieContainerResource containerResource = new KieContainerResource(containerId, releaseId);
        containerResource.setContainerAlias(alias);
        if (configItems != null && configItems.length > 0) {
            containerResource.setConfigItems(Arrays.asList(configItems));
        }
        ServiceResponse<KieContainerResource> reply = client.createContainer(containerId, containerResource);
        Assume.assumeTrue(reply.getType().equals(ServiceResponse.ResponseType.SUCCESS));
    }

    protected void disposeAllServerInstances() {
        // Is done just if we run local server (controller always on) or controller is deployed.
        if (TestConfig.isLocalServer() || TestConfig.isControllerProvided()) {
            disposeServerTemplates();
        }
    }

    private void disposeServerTemplates() {
        KieServerMgmtControllerClient mgmtControllerClient;
        if (TestConfig.isLocalServer()) {
            mgmtControllerClient = new KieServerMgmtControllerClient(TestConfig.getControllerHttpUrl(), null, null);
        } else {
            mgmtControllerClient = new KieServerMgmtControllerClient(TestConfig.getControllerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
        }
        mgmtControllerClient.setMarshallingFormat(MarshallingFormat.JAXB);
        Collection<ServerTemplate> serverTemplates = mgmtControllerClient.listServerTemplates();
        for (ServerTemplate serverTemplate : serverTemplates) {
            mgmtControllerClient.deleteServerTemplate(serverTemplate.getId());
        }
        mgmtControllerClient.close();
    }

    protected abstract KieServicesClient createDefaultClient() throws Exception;

    public static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {

            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {

                new File(tempDir, file).delete();
            }
        }
    }

    protected Map<String, Class<?>> extraClasses = new ConcurrentHashMap<String, Class<?>>();

    protected KieServicesClient createDefaultClient(KieServicesConfiguration configuration, MarshallingFormat marshallingFormat) throws Exception {
        KieServicesClient kieServicesClient = null;

        configuration.setTimeout(DEFAULT_TIMEOUT);
        configuration.setMarshallingFormat(marshallingFormat);

        configuration.addExtraClasses(new HashSet<Class<?>>(extraClasses.values()));
        additionalConfiguration(configuration);

        if (extraClasses.size() > 0) {
            // Use classloader of extra classes as client classloader
            ClassLoader classLoader = extraClasses.values().iterator().next().getClassLoader();
            kieServicesClient = KieServicesFactory.newKieServicesClient(configuration, classLoader);
        } else {
            kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        }

        setupClients(kieServicesClient);
        return kieServicesClient;
    }

    /**
     * Create client with default REST configuration - usable for helper purposes(creating containers...).
     * @return Kie server client.
     */
    protected static KieServicesClient createDefaultStaticClient() {
        return createDefaultStaticClient(DEFAULT_TIMEOUT);
    }

    /**
     * Create client with default REST configuration - usable for helper purposes(creating containers...).
     * @param timeout
     * @return Kie server client.
     */
    protected static KieServicesClient createDefaultStaticClient(long timeout) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(),TestConfig.getUsername(), TestConfig.getPassword(), timeout);
        configuration.setMarshallingFormat(MarshallingFormat.JAXB);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        return kieServicesClient;
    }

    /**
     * Add custom classes needed by marshallers.
     *
     * @param extraClasses Map with classname keys and respective Class instances.
     */
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {}

    /**
     * Additional configuration of KieServicesConfiguration like timeout and such.
     *
     * @param configuration Kie server configuration to be configured.
     */
    protected void additionalConfiguration(KieServicesConfiguration configuration) throws Exception {}

    /**
     * Initialize Execution server clients.
     * Override to initialize specific clients.
     *
     * @param kieServicesClient Kie services client.
     */
    protected void setupClients(KieServicesClient kieServicesClient){}

}

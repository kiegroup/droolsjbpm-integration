/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.integrationtests.shared;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.cli.MavenCli;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.rest.RestKieServerControllerAdminImpl;
import org.kie.server.controller.rest.RestKieServerControllerImpl;
import org.kie.server.integrationtests.config.JacksonRestEasyTestConfig;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieServerBaseIntegrationTest {

    protected static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    protected static TJWSEmbeddedJaxrsServer server;
    protected static TJWSEmbeddedJaxrsServer controller;
    protected static MavenRepository repository;

    // Need to hold kie server instance because we need to manually handle startup/shutdown behavior defined in
    // context listener org.kie.server.services.Bootstrap. Embedded server doesn't support ServletContextListeners.
    private static KieServerImpl kieServer;

    protected KieServicesClient client;
    /*
       Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
       kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
       the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
       already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;



    @BeforeClass
    public static void setupClass() throws Exception {
        if (TestConfig.isLocalServer()) {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
            startKieController();
            startKieServer();
        }
        setupCustomSettingsXml();
        warmUpServer();
    }

    private static void setupCustomSettingsXml() {
        if (!TestConfig.isLocalServer()) {
            String clientDeploymentSettingsXml = ClassLoader.class.getResource(
                    "/kie-server-testing-client-deployment-settings.xml").getFile();
            System.setProperty(KieServerConstants.CFG_KIE_MVN_SETTINGS, clientDeploymentSettingsXml);
        }
    }

    /*
    * The first call to the server takes usually much longer because the JVM needs to load all the classes, JAXRS subsystem gets
    * initialized, etc. The first test sometimes fails, more frequently on slow machines.
    *
    * This method creates dummy container and then immediately destroys it. This should warm-up the server enough
    * so that the subsequent calls are faster.
    */
    private static void warmUpServer() throws Exception {
        logger.info("Warming-up the server by creating dummy container and then immediately destroying it...");
        KieServicesConfiguration config = createKieServicesRestConfiguration();
        // specify higher timeout, the default is too small
        config.setTimeout(30000);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ReleaseId warmUpReleaseId = new ReleaseId("org.kie.server.testing", "server-warm-up", "42");
        createAndDeployKJar(warmUpReleaseId);
        assertSuccess(client.createContainer("warm-up-kjar", new KieContainerResource("warm-up-kjar", warmUpReleaseId)));
        assertSuccess(client.disposeContainer("warm-up-kjar"));
        logger.info("Server warm-up done.");
    }


    @Before
    public void setup() throws Exception {
        startClient();
        disposeAllContainers();
        disposeAllServerInstances();
    }

    @AfterClass
    public static void tearDown() {
        if (TestConfig.isLocalServer()) {
            stopKieServer();
            stopKieController();
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        }
    }

    protected void disposeAllContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, response.getType());
        List<KieContainerResource> containers = response.getResult().getContainers();
        if (containers != null) {
            for (KieContainerResource container : containers) {
                client.disposeContainer(container.getContainerId());
            }
        }
    }

    protected void disposeAllServerInstances() throws Exception {
        // Is done just if we run local server (controller always on) or controller is deployed.
        if (TestConfig.isLocalServer() || TestConfig.isControllerProvided()) {
            ClientRequest clientRequest = newRequest(TestConfig.getControllerHttpUrl() + "/admin/servers");
            ClientResponse<KieServerInstanceList> responseList = clientRequest.accept(MediaType.APPLICATION_XML_TYPE).get(KieServerInstanceList.class);

            assertEquals(Response.Status.OK.getStatusCode(), responseList.getStatus());
            KieServerInstance[] instanceList = responseList.getEntity().getKieServerInstances();

            if (instanceList != null && instanceList.length > 0) {
                for (KieServerInstance kieServerInstance : instanceList) {
                    clientRequest = newRequest(TestConfig.getControllerHttpUrl() + "/admin/server/" + kieServerInstance.getIdentifier());
                    ClientResponse<KieServerInstanceList> responseDelete = clientRequest.accept(MediaType.APPLICATION_XML_TYPE).delete(KieServerInstanceList.class);
                    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), responseDelete.getStatus());
                    responseDelete.releaseConnection();
                }
            }
        }
    }

    private void startClient() throws Exception {
        client = createDefaultClient();
    }

    protected abstract KieServicesClient createDefaultClient() throws Exception;

    private static SimpleDateFormat serverIdSuffixDateFormat = new SimpleDateFormat("yyyy-MM-DD-HHmmss_SSS");

    protected static void startKieController() {
        if (controller != null) {
            throw new RuntimeException("Kie execution controller is already created!");
        }

        controller = new TJWSEmbeddedJaxrsServer();
        controller.setPort(TestConfig.getControllerAllocatedPort());
        controller.start();
        controller.getDeployment().getRegistry().addSingletonResource(new RestKieServerControllerImpl());
        controller.getDeployment().getRegistry().addSingletonResource(new RestKieServerControllerAdminImpl());
        controller.getDeployment().setProviderFactory(JacksonRestEasyTestConfig.createRestEasyProviderFactory());
    }

    protected static void stopKieController() {
        if (controller == null) {
            throw new RuntimeException("Kie execution controller is already stopped!");
        }
        controller.stop();
        controller = null;
    }

    protected static void startKieServer() {
        if (server != null) {
            throw new RuntimeException("Kie execution server is already created!");
        }

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

        // Register server id if wasn't done yet
        if (KieServerEnvironment.getServerId() == null) {
            KieServerEnvironment.setServerId(KieServerBaseIntegrationTest.class.getSimpleName() + "@" + serverIdSuffixDateFormat.format(new Date()));
            KieServerEnvironment.setServerName("KieServer");
        }

        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(TestConfig.getKieServerAllocatedPort());
        server.start();

        kieServer = new KieServerImpl();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerRestImpl(kieServer));

        List<KieServerExtension> extensions = kieServer.getServerExtensions();

        for (KieServerExtension extension : extensions) {
            List<Object> components = extension.getAppComponents(SupportedTransports.REST);
            for (Object component : components) {
                server.getDeployment().getRegistry().addSingletonResource(component);
            }
        }

        server.getDeployment().setProviderFactory(JacksonRestEasyTestConfig.createRestEasyProviderFactory());
    }

    protected static void stopKieServer() {
        if (server == null) {
            throw new RuntimeException("Kie execution server is already stopped!");
        }
        kieServer.destroy();
        server.stop();
        server = null;
    }

    protected static void buildAndDeployMavenProject(String basedir) {
        // need to backup (and later restore) the current class loader, because the Maven/Plexus does some classloader
        // magic which then results in CNFE in RestEasy client
        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
        // remote repo
        logger.debug("Building and deploying Maven project from basedir '{}'.", basedir);
        ClassLoader classLoaderBak = Thread.currentThread().getContextClassLoader();
        MavenCli cli = new MavenCli();
        List<String> mvnArgs;
        if (TestConfig.isLocalServer()) {
            // just install into local repository when running the local server. Deploying to remote repo will fail
            // if the repo does not exist.

            // wrapping explicitly in ArrayList as we may need to update the list later (and Arrays.toList() returns
            // just read-only list)
            mvnArgs = new ArrayList<String>(Arrays.asList("-B", "clean", "install"));
        } else {
            mvnArgs = new ArrayList<String>(Arrays.asList("-B", "-e", "clean", "deploy"));
        }
        // use custom settings.xml file, if one specified
        String kjarsBuildSettingsXml = TestConfig.getKjarsBuildSettingsXml();
        if (kjarsBuildSettingsXml != null && !kjarsBuildSettingsXml.isEmpty()) {
            mvnArgs.add("-s");
            mvnArgs.add(kjarsBuildSettingsXml);
        }
        int mvnRunResult = cli.doMain(mvnArgs.toArray(new String[mvnArgs.size()]), basedir, System.out, System.out);
        if (mvnRunResult != 0) {
            throw new RuntimeException("Error while building Maven project from basedir " + basedir +
                    ". Return code=" + mvnRunResult);
        }
        Thread.currentThread().setContextClassLoader(classLoaderBak);
        logger.debug("Maven project successfully built and deployed!");
    }

    protected static void buildAndDeployCommonMavenParent() {
        // deploy only once as it is not needed to do that with every request
        if (!commonParentDeployed) {
            buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/common-parent").getFile());
        } else {
            logger.debug("Common parent project already deployed, nothing to do here.");
        }
    }

    protected static void createAndDeployKJar(ReleaseId releaseId) {
        String drl = "package org.pkg1\n"
                + "global java.util.List list;"
                + "declare Message\n"
                + "    text : String\n"
                + "end\n"
                + "rule echo dialect \"mvel\"\n"
                + "when\n"
                + "    $m : Message()\n"
                + "then\n"
                + "    $m.text = \"echo:\" + $m.text;\n"
                + "end\n"
                + "rule X when\n"
                + "    msg : String()\n"
                + "then\n"
                + "    list.add(msg);\n"
                + "end\n";
        KieServices ks = KieServices.Factory.get();
        createAndDeployJar(ks, releaseId, drl);

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule(releaseId);
    }

    private static void createAndDeployJar(KieServices ks,
            ReleaseId releaseId,
            String... drls) {
        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(
                releaseId);
        for (int i = 0; i < drls.length; i++) {
            if (drls[i] != null) {
                kfs.write("src/main/resources/org/pkg1/r" + i + ".drl", drls[i]);
            }
        }
        byte[] pom = kfs.read("pom.xml");
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        Assert.assertFalse(kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR).toString(),
                kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR));
        InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule(releaseId);
        byte[] jar = kieModule.getBytes();

        repository = MavenRepository.getMavenRepository();
        repository.deployArtifact(releaseId, jar, pom);
    }

    protected static void assertSuccess(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals("Expected SUCCESS, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.SUCCESS,
                type);
    }

    protected static void assertResultContainsString(String result, String expectedString) {
        assertTrue("Expecting string '" + expectedString + "' in result, but got: " + result, result.contains(expectedString));
    }

    protected static void assertResultContainsStringRegex(String result, String regex) {
        assertTrue("Regex '" + regex + "' does not matches result string '" + result + "'!",
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

    protected static void assertResultNotContainingStringRegex(String result, String regex) {
        assertFalse("Regex '" + regex + "' matches result string '" + result + "'!",
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

    protected static void assertNullOrEmpty(String errorMessage, Collection<?> result ) {
        if (result != null) {
            assertTrue(errorMessage, result.size() == 0);
        }
    }

    protected static void assertNullOrEmpty(String errorMessage, Object[] result ) {
        if (result != null) {
            assertTrue(errorMessage, result.length == 0);
        }
    }

    protected static KieServicesConfiguration createKieServicesJmsConfiguration() {
        try {
            InitialContext context = TestConfig.getInitialRemoteContext();

            Queue requestQueue = (Queue) context.lookup(TestConfig.getRequestQueueJndi());
            Queue responseQueue = (Queue) context.lookup(TestConfig.getResponseQueueJndi());
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(TestConfig.getConnectionFactory());

            KieServicesConfiguration jmsConfiguration = KieServicesFactory.newJMSConfiguration(
                    connectionFactory, requestQueue, responseQueue, TestConfig.getUsername(),
                    TestConfig.getPassword());

            return jmsConfiguration;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JMS client configuration!", e);
        }
    }

    protected static KieServicesConfiguration createKieServicesRestConfiguration() {
        return KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
    }

    private static HttpClient httpClient;
    
    protected ClientRequest newRequest(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed request URI was specified: '" + uriString + "'!", e);
        }
        if (httpClient == null) {
            if (TestConfig.isLocalServer()) {
                RequestConfig requestConfig = RequestConfig.custom()
                                                .setConnectionRequestTimeout(1000)
                                                .setConnectTimeout(1000)
                                                .setSocketTimeout(1000)
                                                .build();
                httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            } else {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(uri.getHost(), uri.getPort()),
                        new UsernamePasswordCredentials(TestConfig.getUsername(), TestConfig.getPassword())
                );
                httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
            }
        }
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient);
        return new ClientRequest(uriString, executor);
    }

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
}

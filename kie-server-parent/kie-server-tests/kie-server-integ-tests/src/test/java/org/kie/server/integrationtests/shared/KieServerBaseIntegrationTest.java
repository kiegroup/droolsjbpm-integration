package org.kie.server.integrationtests.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.core.MediaType;

import org.apache.maven.cli.MavenCli;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.JacksonRestEasyTestConfig;
import org.kie.server.services.rest.KieServerRestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public abstract class KieServerBaseIntegrationTest {

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() throws Exception {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MediaType.APPLICATION_XML_TYPE, restConfiguration},
                                {MediaType.APPLICATION_JSON_TYPE, restConfiguration},
                        }
        ));
                

        if (PROVIDER_URL != null) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]
                            {
                                    {MediaType.APPLICATION_XML_TYPE, jmsConfiguration},
                                    {MediaType.APPLICATION_JSON_TYPE, jmsConfiguration}
                            })
            );
        }

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MediaType MEDIA_TYPE;

    private static final Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    protected static final String DEFAULT_USERNAME = "yoda";
    protected static final String DEFAULT_PASSWORD = "usetheforce123@";

    // REST
    protected static String BASE_HTTP_URL = System.getProperty("kie.server.base.http.url");
    // JMS
    private static final String INITIAL_CONTEXT_FACTORY = System.getProperty( "context.factory", "org.jboss.naming.remote.client.InitialContextFactory");
    private static final String CONNECTION_FACTORY = System.getProperty( "connection.factory", "jms/RemoteConnectionFactory");
    private static final String PROVIDER_URL = System.getProperty( "remoting.uri" );
    private static final String REQUEST_QUEUE_JNDI = System.getProperty( "jndi.kie.request.queue", "jms/queue/KIE.SERVER.REQUEST" );
    private static final String RESPONSE_QUEUE_JNDI = System.getProperty( "jndi.kie.response.queue", "jms/queue/KIE.SERVER.RESPONSE" );

    protected static boolean LOCAL_SERVER = false;

    protected static int PORT;

    private static MavenRepository repository;

    protected static TJWSEmbeddedJaxrsServer server;

    @Parameterized.Parameter(1)
    public KieServicesConfiguration configuration;

    protected KieServicesClient client;

    /*
       Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
       kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
       the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
       already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;

    static {
        if (BASE_HTTP_URL == null && PROVIDER_URL == null) {
            // falls back to local, in memory, server -> serving just for REST
            LOCAL_SERVER = true;
            PORT = findFreePort();
            BASE_HTTP_URL = "http://localhost:" + PORT + "/server";
        }
    }


    @BeforeClass
    public static void setupClass() throws Exception {
        if (LOCAL_SERVER) {
            startServer();
        }
        setupCustomSettingsXml();
        logSettings();
        warmUpServer();
    }

    private static void setupCustomSettingsXml() {
        if (!LOCAL_SERVER) {
            String clientDeploymentSettingsXml = ClassLoader.class.getResource("/kie-server-testing-client-deployment-settings.xml").getFile();
            System.setProperty("kie.maven.settings.custom", clientDeploymentSettingsXml);
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
        KieServicesConfiguration config  = createKieServicesRestConfiguration();
        // specify higher timeout, the default is too small
        config.setTimeout(30000);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ReleaseId warmUpReleaseId = new ReleaseId("org.kie.server.testing", "server-warm-up", "42");
        createAndDeployKJar(warmUpReleaseId);
        assertSuccess(client.createContainer("warm-up-kjar", new KieContainerResource("warm-up-kjar", warmUpReleaseId)));
        assertSuccess(client.disposeContainer("warm-up-kjar"));
        logger.info("Server warm-up done.");
    }


    private static void logSettings() {
        logger.debug("KIE Server base URI: " + BASE_HTTP_URL);
    }

    @Before
    public void setup() throws Exception {
        startClient();
        disposeAllContainers();
    }

    @AfterClass
    public static void tearDown() {
        if (LOCAL_SERVER) {
            server.stop();
        }
    }

    protected void disposeAllContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, response.getType());
        List<KieContainerResource> containers = response.getResult().getContainers();
        for (KieContainerResource container : containers) {
            client.disposeContainer(container.getContainerId());
        }
    }

    private void startClient() throws Exception {
        client = createDefaultClient();
    }

    protected KieServicesClient createDefaultClient() throws Exception {
        if (LOCAL_SERVER) {
            return KieServicesFactory.newKieServicesRestClient(BASE_HTTP_URL, null, null);
        } else {
            return KieServicesFactory.newKieServicesClient(configuration);
        }
    }

    private static void startServer() throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerRestImpl());
        server.getDeployment().setProviderFactory(JacksonRestEasyTestConfig.createRestEasyProviderFactory());
    }

    protected static void buildAndDeployMavenProject(String basedir) {
        // need to backup (and later restore) the current class loader, because the Maven/Plexus does some classloader
        // magic which then results in CNFE in RestEasy client
        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
        // remote repo
        logger.debug("Building and deploying Maven project from basedir '{}'.", basedir);
        ClassLoader classLoaderBak = Thread.currentThread().getContextClassLoader();
        MavenCli cli = new MavenCli();
        String[] mvnArgs;
        if (LOCAL_SERVER) {
            // just install into local repository when running the local server. Deploying to remote repo will fail
            // if the repo does not exists.
            mvnArgs = new String[]{"-B", "clean", "install"};
        } else {
            mvnArgs = new String[]{"-B", "clean", "deploy"};
        }
        int mvnRunResult = cli.doMain(mvnArgs, basedir, System.out, System.out);
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
            logger.info("Common parent project already deployed!");
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

    public static int findFreePort() {
        int port = 0;
        try {
            ServerSocket server =
                    new ServerSocket(0);
            port = server.getLocalPort();
            server.close();
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            port = 9789;
        }
        logger.debug("Allocating port {}.", +port);
        return port;
    }

    protected static void assertSuccess(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals("Expected SUCCESS, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.SUCCESS, type);
    }

    protected static void assertResultContainsString(String result, String expectedString) {
        assertTrue("Expecting string '" + expectedString + "' in result, but got: " + result, result.contains(expectedString));
    }

    protected static void assertResultContainsStringRegex(String result, String regex) {
        assertTrue("Regex '" + regex + "' does not matches result string '" + result + "'!" ,
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

//    protected ClientRequest newRequest(String uriString) {
//        URI uri;
//        try {
//            uri = new URI(uriString);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Malformed URI was specified: '" + uriString + "'!", e);
//        }
//        if (LOCAL_SERVER) {
//            return new ClientRequest(uriString);
//        } else {
//            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//            credentialsProvider.setCredentials(
//                    new AuthScope(uri.getHost(), uri.getPort()),
//                    new UsernamePasswordCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD)
//            );
//            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
//            ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
//            return new ClientRequest(uriString, executor);
//        }
//    }

    private static KieServicesConfiguration createKieServicesJmsConfiguration() throws Exception {
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
        env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", DEFAULT_USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", DEFAULT_PASSWORD));
        InitialContext context = new InitialContext(env);

        logger.debug("JMS provider URL: {}", PROVIDER_URL);
        logger.debug("Initial context factory: {}", INITIAL_CONTEXT_FACTORY);
        logger.debug("Connection factory: {}", CONNECTION_FACTORY);
        logger.debug("JMS request queue JNDI: {}", REQUEST_QUEUE_JNDI);
        logger.debug("JMS response queue JNDI: {}", RESPONSE_QUEUE_JNDI);

        Queue requestQueue = (Queue) context.lookup(REQUEST_QUEUE_JNDI);
        Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_JNDI);
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY);

        KieServicesConfiguration jmsConfiguration = KieServicesFactory.newJMSConfiguration(
                connectionFactory, requestQueue, responseQueue, System.getProperty("username", DEFAULT_USERNAME),
                System.getProperty("password", DEFAULT_PASSWORD));

        return jmsConfiguration;
    }

    private static KieServicesConfiguration createKieServicesRestConfiguration() throws Exception {
        return KieServicesFactory.newRestConfiguration(BASE_HTTP_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
    
}

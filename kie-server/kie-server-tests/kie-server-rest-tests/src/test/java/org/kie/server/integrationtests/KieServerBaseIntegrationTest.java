package org.kie.server.integrationtests;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.cli.MavenCli;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.After;
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
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.JacksonRestEasyTestConfig;
import org.kie.server.services.rest.KieServerRestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public abstract class KieServerBaseIntegrationTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] { {MediaType.APPLICATION_XML_TYPE}, {MediaType.APPLICATION_JSON_TYPE} } );
    }

    @Parameterized.Parameter
    public MediaType MEDIA_TYPE;

    private static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    protected static final String DEFAULT_USERNAME = "yoda";
    protected static final String DEFAULT_PASSWORD = "usetheforce123@";

    protected static String BASE_URI = System.getProperty("kie.server.base.uri");

    protected static boolean LOCAL_SERVER = false;

    protected static int PORT;

    private static MavenRepository repository;

    protected TJWSEmbeddedJaxrsServer server;

    protected KieServicesClient client;

    /*
       Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
       kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
       the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
       already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;

    static {
        if (BASE_URI == null) {
            // falls back to local, in memory, server
            LOCAL_SERVER = true;
            PORT = findFreePort();
            BASE_URI = "http://localhost:" + PORT + "/server";
        }
    }

    @BeforeClass
    public static void logSettings() {
        logger.debug("Kie Server base URI: " + BASE_URI);
    }

    @BeforeClass
    public static void setupCustomSettingsXml() {
        if (!LOCAL_SERVER) {
            String clientDeploymentSettingsXml = ClassLoader.class.getResource("/kie-server-testing-client-deployment-settings.xml").getFile();
            System.setProperty("kie.maven.settings.custom", clientDeploymentSettingsXml);
        }
    }

    @Before
    public void setup() throws Exception {
        if (LOCAL_SERVER) {
            startServer();
        }
        startClient();
        disposeAllContainers();
    }

    @After
    public void tearDown() {
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

    protected KieServicesClient createDefaultClient() {
        if (LOCAL_SERVER) {
            return KieServicesFactory.newKieServicesRestClient( BASE_URI, null, null );
        } else {
            return KieServicesFactory.newKieServicesRestClient( BASE_URI, DEFAULT_USERNAME, DEFAULT_PASSWORD );
        }
    }

    private void startServer() throws Exception {
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
        System.out.println("Allocating port: "+port);
        return port;
    }

    protected void assertSuccess(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals("Expected SUCCESS, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.SUCCESS, type);
    }

    protected void assertResultContainsString(String result, String expectedString) {
        assertTrue("Expecting string '" + expectedString + "' in result, but got: " + result, result.contains(expectedString));
    }

    protected void assertResultContainsStringRegex(String result, String regex) {
        assertTrue("Regex '" + regex + "' does not matches result string '" + result + "'!" ,
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

    protected ClientRequest newRequest(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed URI was specified: '" + uriString + "'!", e);
        }
        if (LOCAL_SERVER) {
            return new ClientRequest(uriString);
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD)
            );
            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
            ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
            return new ClientRequest(uriString, executor);
        }
    }

}

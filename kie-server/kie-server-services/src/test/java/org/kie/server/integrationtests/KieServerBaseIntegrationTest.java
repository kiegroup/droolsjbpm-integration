package org.kie.server.integrationtests;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.services.rest.KieServerRestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public abstract class KieServerBaseIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    protected static String BASE_URI = System.getProperty("kie.server.base.uri");

    protected static boolean LOCAL_SERVER = false;

    protected static int PORT;

    private static MavenRepository repository;

    protected TJWSEmbeddedJaxrsServer server;

    protected KieServicesClient client;

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
    public static void configureCustomSettingsXml() {
        if( !LOCAL_SERVER ) {
            System.setProperty("kie.maven.settings.custom",
                               ClassLoader.class.getResource("/kie-server-testing-custom-settings.xml").getFile());
            logger.debug(
                    "Value of 'kie.maven.settings.custom' property:" + System.getProperty("kie.maven.settings.custom"));
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

    private void disposeAllContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, response.getType());
        List<KieContainerResource> containers = response.getResult().getContainers();
        for (KieContainerResource container : containers) {
            client.disposeContainer(container.getContainerId());
        }
    }

    private void startClient() throws Exception {
        client = new KieServicesClient(BASE_URI);
    }

    private void startServer() throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerRestImpl());
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

}

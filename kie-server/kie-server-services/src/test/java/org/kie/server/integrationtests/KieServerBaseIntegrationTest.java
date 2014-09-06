package org.kie.server.integrationtests;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class KieServerBaseIntegrationTest {
    private static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    public static final String BASE_URI = System.getProperty("kie.server.base.uri",
            "http://localhost:8080/kie-server-services/services/rest/server");

    private static MavenRepository repository;

    protected KieServicesClient client;

    @BeforeClass
    public static void logSettings() {
        logger.debug("Kie Server base URI: " + BASE_URI);
    }

    @BeforeClass
    public static void configureCustomSettingsXml() {
        System.setProperty("kie.maven.settings.custom",
                ClassLoader.class.getResource("/kie-server-testing-custom-settings.xml").getFile());
        logger.debug("Value of 'kie.maven.settings.custom' property:" + System.getProperty("kie.maven.settings.custom"));
    }

    @Before
    public void setup() throws Exception {
        startClient();
        disposeAllContainers();
    }

    private void disposeAllContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, response.getType());
        List<KieContainerResource> containers = response.getResult().getContainers();
        // TODO server should return empty list instead of null when there are no containers!
        if (containers != null) {
            for (KieContainerResource container : containers) {
                client.disposeContainer(container.getContainerId());
            }
        }
    }

    private void startClient() throws Exception {
        client = new KieServicesClient(BASE_URI);
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

        try {
            FileOutputStream fos = new FileOutputStream("target/baz-2.1.0.GA.jar");
            fos.write(jar);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        repository = MavenRepository.getMavenRepository();
        repository.deployArtifact(releaseId, jar, pom);
    }
}

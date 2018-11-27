package org.kie.camel.container.integration.tests;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.FileManager;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.scanner.KieMavenRepository;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;

import static org.junit.Assert.assertTrue;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class RemoteIntegrationTest extends AbstractKieCamelIntegrationTest {

    @Test
    public void listContainersTest() throws Exception {
        final String containerId = "test-container";
        final ReleaseId releaseId = new ReleaseId("org.drools", "camel-container-tests-kjar", "1.0");

        createKieJar( releaseId );

        final KieContainerResource kieContainerResource = new KieContainerResource(releaseId);
        kieContainerResource.setContainerId(containerId);

        KieContainerResource resource = kieCamelTestService.esCreateContainer(containerId, kieContainerResource);
        Assertions.assertThat(resource).isNotNull();

        KieContainerResourceList kieContainerResourceList = kieCamelTestService.esListContainers();

        Assertions.assertThat(kieContainerResourceList.getContainers().size()).isGreaterThan(0);
    }

    private void createKieJar( ReleaseId releaseId ) throws IOException {
        KieServices ks = KieServices.get();
        KieFileSystem kfs = createKieFileSystemWithKProject(ks, false);
        kfs.writePomXML(getPom(releaseId));

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        assertTrue("", kieBuilder.buildAll().getResults().getMessages().isEmpty());
        InternalKieModule kJar1 = (InternalKieModule) kieBuilder.getKieModule();

        KieMavenRepository repository = getKieMavenRepository();
        repository.installArtifact(releaseId, kJar1, createKPom(releaseId));
    }

    private KieFileSystem createKieFileSystemWithKProject(KieServices ks, boolean isdefault) {
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }

    private File createKPom( org.kie.api.builder.ReleaseId releaseId) throws IOException {
        FileManager fileManager = new FileManager();
        fileManager.setUp();
        File pomFile = fileManager.newFile("pom.xml");
        fileManager.write(pomFile, getPom(releaseId));
        return pomFile;
    }

    private String getPom( org.kie.api.builder.ReleaseId releaseId ) {
        return
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                "  <version>" + releaseId.getVersion() + "</version>\n" +
                "\n" +
                "</project>";
    }
}

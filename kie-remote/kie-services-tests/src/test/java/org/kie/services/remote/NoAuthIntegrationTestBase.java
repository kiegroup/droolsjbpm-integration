package org.kie.services.remote;

import static org.kie.services.remote.setup.TestConstants.projectVersion;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;

public class NoAuthIntegrationTestBase {

    static WebArchive createWebArchive() { 
        List<MavenResolvedArtifact> artifacts = new ArrayList<MavenResolvedArtifact>();
        
        String [] warDeps = { 
                // kie-services
                "org.kie.remote:kie-services-remote",
                // cdi
                "org.jboss.solder:solder-impl",
                // persistence
                "org.jbpm:jbpm-audit",
                "org.jbpm:jbpm-persistence-jpa",
                "org.jbpm:jbpm-runtime-manager",
                // cdi impls (includes core jbpm libs)
                "org.jbpm:jbpm-kie-services",
                // services
                "org.kie.commons:kie-nio2-fs",
                // test
                "org.jbpm:jbpm-shared-services:test-jar:" + projectVersion,
        };
        
        MavenResolvedArtifact [] warArtifacts = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(warDeps)
                .withTransitivity()
                .asResolvedArtifact();
        artifacts.addAll(Arrays.asList(warArtifacts));
        
        List<File> libList = new ArrayList<File>();
        HashSet<String> depSet = new HashSet<String>();
        for( MavenResolvedArtifact artifact : artifacts ) { 
            MavenCoordinate depCoord = artifact.getCoordinate();
            if( depCoord.getGroupId().contains("dom4j") ) { 
                continue;
            }
            if( depCoord.getArtifactId().equals("resteasy-jaxrs") ) {
                continue;
            }
            String artifactId = depCoord.getArtifactId();
            if( depSet.add(artifactId) ) {
                libList.add(artifact.asFile());
            }
        }
        File [] libs = libList.toArray(new File[libList.size()]);
        
        WebArchive war =  ShrinkWrap.create(WebArchive.class, "kie-services-remote-test.war")
                .addPackages(true, "org/kie/services/remote/war")
                .addAsResource("META-INF/persistence.xml")
                .addAsServiceProvider(FileSystemProvider.class, SimpleFileSystemProvider.class)
                .addAsWebInfResource("WEB-INF/test-beans.xml", "beans.xml")
                .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
                .setWebXML("WEB-INF/web.xml")
                .addAsLibraries(libs);
        
        // export in order to inspect it
        war.as(ZipExporter.class).exportTo(new File("target/" + war.getName()), true);
        
        return war;
    }
    
    protected ClientRequestFactory createNoAuthRequestFactory(URL deploymentUrl) throws URISyntaxException { 
        return new ClientRequestFactory(deploymentUrl.toURI());
    }
}

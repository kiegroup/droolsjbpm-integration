package org.kie.services.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;

public class IntegrationTestBase {

    protected final static String USER="test";
    protected final static String PASSWORD="12341234";
    
    protected final static String projectVersion;
    static { 
        Properties testProps = new Properties();
        try {
            testProps.load(IntegrationTestBase.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize DroolsVersion property: " + e.getMessage(), e);
        }
        projectVersion = testProps.getProperty("project.version");
    }

    /**
     * Initializes a (remote) IntialContext instance.
     * 
     * @return a remote {@link InitialContext} instance
     */
    protected static InitialContext getRemoteInitialContext() {
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://localhost:4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, USER );
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, PASSWORD );
        
        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
    
    static WebArchive createWebArchive() { 

        List<MavenResolvedArtifact> artifacts = new ArrayList<MavenResolvedArtifact>();
        
        MavenResolvedArtifact[] runtimeArtifacts = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .asResolvedArtifact();
        artifacts.addAll(Arrays.asList(runtimeArtifacts));
        
        String [] warDeps = { 
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
            String artifactId = depCoord.getArtifactId();
            if( depSet.add(artifactId) ) {
                libList.add(artifact.asFile());
            }
        }
        File [] libs = libList.toArray(new File[libList.size()]);
        
        WebArchive war =  ShrinkWrap.create(WebArchive.class, "arquillian-test.war")
                .addPackages(true, "org/kie/services/remote/cdi", "org/kie/services/remote/jms", "org/kie/services/remote/rest", "org/kie/services/remote/util", "org/kie/services/remote/exception")
                .addPackages(true, "org/kie/services/remote/war")
                .addClass(KieRemoteServicesInternalError.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsServiceProvider(FileSystemProvider.class, SimpleFileSystemProvider.class)
                .addAsResource("users.properties")
                .addAsWebInfResource("WEB-INF/test-beans.xml", "beans.xml")
                .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
                .setWebXML("WEB-INF/web.xml")
                .addAsLibraries(libs);
        
        // export in order to inspect it
        war.as(ZipExporter.class).exportTo(new File("target/" + war.getName()), true);
        
        return war;
    }
}

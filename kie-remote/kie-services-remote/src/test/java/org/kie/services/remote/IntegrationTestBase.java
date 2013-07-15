package org.kie.services.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;

public class IntegrationTestBase {

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
    
    static WebArchive createWebArchive() { 

        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .asFile();
        
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
                "org.jbpm:jbpm-shared-services:test-jar:" + projectVersion
        };
        
        File[] warLibs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(warDeps)
                .withTransitivity()
                .asFile();
        
        List<File> libList = new ArrayList<File>(Arrays.asList(warLibs));
        libList.addAll(Arrays.asList(libs));
        Iterator<File> iter = libList.iterator();
        HashSet<String> depSet = new HashSet<String>();
        while( iter.hasNext() ) {
            File depFile = iter.next();
            if( depFile.getAbsolutePath().contains("dom4j") ) { 
                iter.remove();
                continue;
            }
            String parent = depFile.getParent();
            if( depSet.contains(parent) && ! "/tmp".equals(parent) && ! parent.contains("jre") ) { 
                iter.remove();
                continue;
            }
            depSet.add(depFile.getParent());
        }
        libs = libList.toArray(new File[libList.size()]);
        
        WebArchive war =  ShrinkWrap.create(WebArchive.class, "arquillian-test.war")
                .addPackages(true, "org/kie/services/remote/cdi", "org/kie/services/remote/jms", "org/kie/services/remote/rest", "org/kie/services/remote/util")
                .addPackages(true, "org/kie/services/remote/war")
                .addClass(KieRemoteServicesInternalError.class)
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
}

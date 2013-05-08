package org.kie.services.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;

public class IntegrationBase {

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
                // cdi impls
                "org.jbpm:jbpm-kie-services",
                // services
                "org.kie.commons:kie-nio2-fs",
                // test
                "org.jbpm:jbpm-shared-services:test-jar:6.0.0-SNAPSHOT"
        };
        
        File[] warLibs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(warDeps)
                .withTransitivity()
                .asFile();
        
        List<File> libList = new ArrayList<File>(Arrays.asList(warLibs));
        libList.addAll(Arrays.asList(libs));
        Iterator<File> iter = libList.iterator();
        while( iter.hasNext() ) {
            if( iter.next().getAbsolutePath().contains("dom4j") ) { 
                iter.remove();
            }
        }
        libs = libList.toArray(new File[libList.size()]);
        
        WebArchive war =  ShrinkWrap.create(WebArchive.class, "arquillian-test.war")
                .addPackages(true, "org/kie/services/remote/cdi", "org/kie/services/remote/ejb", "org/kie/services/remote/jms", "org/kie/services/remote/rest")
                .addPackages(true, "org/kie/services/remote/war")
                .addClass(UnfinishedError.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsServiceProvider(FileSystemProvider.class, SimpleFileSystemProvider.class)
                .addAsWebInfResource("WEB-INF/beans.xml", "beans.xml")
                .setWebXML("WEB-INF/web.xml")
                .addAsLibraries(libs);
        
        // export in order to inspect it
        war.as(ZipExporter.class).exportTo(new File("target/" + war.getName()), true);
        
        return war;
    }
}

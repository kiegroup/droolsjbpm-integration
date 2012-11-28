package org.drools.fluent.simulation;

import org.drools.builder.impl.KnowledgeContainerImpl;
import org.drools.core.util.FileManager;
import org.kie.builder.KieBaseModel;
import org.kie.builder.KieProject;
import org.drools.kproject.KieProjectImpl;
import org.kie.builder.KieSessionModel;
import org.junit.After;
import org.junit.Before;
import org.kie.builder.KnowledgeContainer;
import org.kie.builder.KnowledgeContainerFactory;
import org.kie.builder.ResourceType;
import org.kie.conf.AssertBehaviorOption;
import org.kie.conf.EventProcessingOption;
import org.kie.runtime.conf.ClockTypeOption;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static java.util.Arrays.asList;

public class SimulateTestBase {

    protected FileManager fileManager;
    protected ClassLoader contextClassLoader;

    public SimulateTestBase() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        KnowledgeContainerImpl.clearCache();
        this.fileManager = new FileManager().setUp();
        contextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() throws Exception {
        this.fileManager.tearDown();
        Thread.currentThread().setContextClassLoader( contextClassLoader );
    }

    protected void createKJar(String... pairs) throws IOException {
        KieProject kproj = new KieProjectImpl();
        for ( int i = 0; i < pairs.length; i += 2 ) {
            String id = pairs[i];
            String rule = pairs[i + 1];

            fileManager.write( fileManager.newFile( "src/kbases/" + id + "/org/test/rule" + i + ".drl" ), rule );

            KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                    .setEqualsBehavior( AssertBehaviorOption.EQUALITY )
                    .setEventProcessingMode( EventProcessingOption.STREAM );

            KieSessionModel ksession1 = kBase1.newKieSessionModel( id+".KSession1" )
                    .setType( "stateful" )
                    .setAnnotations( asList( "@ApplicationScoped; @Inject" ) )
                    .setClockType( ClockTypeOption.get( "pseudo" ) );
        }

        fileManager.write( fileManager.newFile( KnowledgeContainerImpl.KPROJECT_RELATIVE_PATH ), kproj.toXML() );

        KnowledgeContainer kcontainer = KnowledgeContainerFactory.newKnowledgeContainer();

        // input and output folder are the same
        File kJar = kcontainer.buildKJar( fileManager.getRootDirectory(), fileManager.getRootDirectory(), "test.jar" );

        // we now have a KJAR
        // you now have two choices, write the KJAR to disk and use URLClassLoader
        // or use a custom class loader which the FS is added to. you can eithre use
        // the ServiceRegistrImpl, which the factory will access, or set CurrentContextClasslader

        // ServiceRegistryImpl.put(ClassLoader.class.getName());

        URLClassLoader urlClassLoader = new URLClassLoader( new URL[]{kJar.toURI().toURL()} );
        Thread.currentThread().setContextClassLoader( urlClassLoader );
    }

    protected void createKJarWithMultipleResources(String id,
                                                   String[] resources,
                                                   ResourceType[] types) throws IOException {
        KieProject kproj = new KieProjectImpl();
        for ( int i = 0; i < resources.length; i++ ) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            fileManager.write( fileManager.newFile( "src/kbases/" + id + "/org/test/res" + i + "." + type ), res );
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                .setEqualsBehavior( AssertBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM );

        KieSessionModel ksession1 = kBase1.newKieSessionModel( id + ".KSession1" )
                .setType( "stateful" )
                .setAnnotations( asList( "@ApplicationScoped; @Inject" ) )
                .setClockType( ClockTypeOption.get( "pseudo" ) );

        fileManager.write( fileManager.newFile( KnowledgeContainerImpl.KPROJECT_RELATIVE_PATH ), kproj.toXML() );

        KnowledgeContainer kcontainer = KnowledgeContainerFactory.newKnowledgeContainer();

        // input and output folder are the same
        File kJar = kcontainer.buildKJar( fileManager.getRootDirectory(), fileManager.getRootDirectory(), "test.jar" );

        // we now have a KJAR
        // you now have two choices, write the KJAR to disk and use URLClassLoader
        // or use a custom class loader which the FS is added to. you can eithre use
        // the ServiceRegistrImpl, which the factory will access, or set CurrentContextClasslader

        // ServiceRegistryImpl.put(ClassLoader.class.getName());

        URLClassLoader urlClassLoader = new URLClassLoader( new URL[]{kJar.toURI().toURL()} );
        Thread.currentThread().setContextClassLoader( urlClassLoader );
    }
}
package org.kie.server.integrationtests.jms;

//import org.apache.maven.cli.MavenCli;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public abstract class KieServerBaseIntegrationTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][]{{MarshallingFormat.JAXB}, {MarshallingFormat.JSON}} );
    }

    @Parameterized.Parameter
    public MarshallingFormat FORMAT;

    private static Logger logger = LoggerFactory.getLogger( KieServerBaseIntegrationTest.class );

    private static final String DEFAULT_USERNAME        = "yoda";
    private static final String DEFAULT_PASSWORD        = "usetheforce123@";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL            = System.getProperty( "remoting.uri", "remote://localhost:4447" );

    protected static String BASE_URI = System.getProperty( "kie.server.base.uri" );

    private static MavenRepository repository;

    protected KieServicesClient client;

    /*
       Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
       kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
       the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
       already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;

    @BeforeClass
    public static void logSettings() {
        logger.debug( "Kie Server base URI: " + BASE_URI );
    }

//    @BeforeClass
//    public static void setupCustomSettingsXml() {
//        String clientDeploymentSettingsXml = ClassLoader.class.getResource( "/kie-server-testing-client-deployment-settings.xml" ).getFile();
//        System.setProperty( "kie.maven.settings.custom", clientDeploymentSettingsXml );
//    }

    @Before
    public void setup()
            throws Exception {
        startClient();
        disposeAllContainers();
    }

    @After
    public void tearDown() {
    }

    protected void disposeAllContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assert.assertEquals( ServiceResponse.ResponseType.SUCCESS, response.getType() );
        List<KieContainerResource> containers = response.getResult().getContainers();
        for ( KieContainerResource container : containers ) {
            client.disposeContainer( container.getContainerId() );
        }
    }

    private void startClient()
            throws Exception {
        final Properties env = new Properties();
        env.put( Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY );
        env.put( Context.PROVIDER_URL, System.getProperty( Context.PROVIDER_URL, PROVIDER_URL ) );
        env.put( Context.SECURITY_PRINCIPAL, System.getProperty( "username", DEFAULT_USERNAME ) );
        env.put( Context.SECURITY_CREDENTIALS, System.getProperty( "password", DEFAULT_PASSWORD ) );
        InitialContext context = new InitialContext( env );

        KieServicesConfiguration conf = KieServicesFactory.newJMSConfiguration( context, DEFAULT_USERNAME, DEFAULT_PASSWORD );
        conf.setMarshallingFormat( FORMAT );
        client = KieServicesFactory.newKieServicesClient( conf );
    }

//    protected static void buildAndDeployMavenProject(String basedir) {
//        // need to backup (and later restore) the current class loader, because the Maven/Plexus does some classloader
//        // magic which then results in CNFE in RestEasy client
//        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
//        // remote repo
//        ClassLoader classLoaderBak = Thread.currentThread().getContextClassLoader();
//        MavenCli cli = new MavenCli();
//        String[] mvnArgs = new String[]{"-B", "clean", "deploy"};
//        int mvnRunResult = cli.doMain( mvnArgs, basedir, System.out, System.out );
//        if ( mvnRunResult != 0 ) {
//            throw new RuntimeException(
//                    "Error while building Maven project from basedir " + basedir +
//                    ". Return code=" + mvnRunResult );
//        }
//        Thread.currentThread().setContextClassLoader( classLoaderBak );
//    }
//
//    protected static void buildAndDeployCommonMavenParent() {
//        // deploy only once as it is not needed to do that with every request
//        if ( !commonParentDeployed ) {
//            buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/common-parent" ).getFile() );
//        } else {
//            logger.info( "Common parent project already deployed!" );
//        }
//    }

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
        org.kie.api.KieServices ks = org.kie.api.KieServices.Factory.get();
        createAndDeployJar( ks, releaseId, drl );

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule( releaseId );
    }

    private static void createAndDeployJar(
            org.kie.api.KieServices ks,
            ReleaseId releaseId,
            String... drls) {
        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(
                releaseId );
        for ( int i = 0; i < drls.length; i++ ) {
            if ( drls[i] != null ) {
                kfs.write( "src/main/resources/org/pkg1/r" + i + ".drl", drls[i] );
            }
        }
        byte[] pom = kfs.read( "pom.xml" );
        KieBuilder kb = ks.newKieBuilder( kfs ).buildAll();
        Assert.assertFalse(
                kb.getResults().getMessages( org.kie.api.builder.Message.Level.ERROR ).toString(),
                kb.getResults().hasMessages( org.kie.api.builder.Message.Level.ERROR ) );
        InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule( releaseId );
        byte[] jar = kieModule.getBytes();

        repository = MavenRepository.getMavenRepository();
        repository.deployArtifact( releaseId, jar, pom );
    }

    public static int findFreePort() {
        int port = 0;
        try {
            ServerSocket server =
                    new ServerSocket( 0 );
            port = server.getLocalPort();
            server.close();
        } catch ( IOException e ) {
            // failed to dynamically allocate port, try to use hard coded one
            port = 9789;
        }
        System.out.println( "Allocating port: " + port );
        return port;
    }

    protected void assertSuccess(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals( "Expected SUCCESS, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.SUCCESS, type );
    }

    protected void assertResultContainsString(String result, String expectedString) {
        assertTrue( "Expecting string '" + expectedString + "' in result, but got: " + result, result.contains( expectedString ) );
    }

    protected void assertResultContainsStringRegex(String result, String regex) {
        assertTrue(
                "Regex '" + regex + "' does not matches result string '" + result + "'!",
                Pattern.compile( regex, Pattern.DOTALL ).matcher( result ).matches() );
    }

}

package org.kie.server.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GenericType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.client.KieServicesClient;
import org.kie.server.services.rest.KieServerRestImpl;

import com.thoughtworks.xstream.XStream;

public class KieServerTest {

    private static final int PORT = findFreePort();
    public static final String BASE_URI = "http://localhost:" + PORT + "/server";
    private static MavenRepository repository;
    private static ReleaseId releaseId1 = new ReleaseId("foo.bar", "baz", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("foo.bar", "baz", "2.1.1.GA");

    private TJWSEmbeddedJaxrsServer server;
    private KieServicesClient       client;

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId1);
        createAndDeployKJar(releaseId2);
        // this initialization only needs to be done once per VM
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }

    @AfterClass
    public static void destroy() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        startServer();
        startClient();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testGetServerInfo() throws Exception {
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        KieServerInfo info = reply.getResult();
        Assert.assertEquals(KieServerEnvironment.getVersion().toString(), info.getVersion());
        System.out.println(reply.getResult());
    }

    @Test
    public void testCreateContainer() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("kie1", new KieContainerResource("kie1",
                                                                                                              releaseId1));
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCreateContainerNonExistingGAV() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("kie1", new KieContainerResource("kie1",
                                                                                                              new ReleaseId(
                                                                                                                      "foo",
                                                                                                                      "bar",
                                                                                                                      "0.0.0")));
        Assert.assertEquals(ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testCreateContainerNonExistingGAV2() throws Exception {
        KieContainerResource resource = new KieContainerResource("kie1", new ReleaseId("foo", "bar", "0.0.0"));

        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(BASE_URI + "/containers/" + resource.getContainerId());
            response = clientRequest.body(
                    MediaType.APPLICATION_XML_TYPE, resource).put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            Assert.assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
            Assert.assertEquals( ResponseType.FAILURE, response.getEntity().getType());
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception creating container: "+resource.getContainerId()+" with release-id "+resource.getReleaseId(), e, response );
        }
    }

    @Test
    public void testCreateContainerEmptyBody() throws Exception {
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(BASE_URI + "/containers/kie1");
            response = clientRequest.body(
                    MediaType.APPLICATION_XML_TYPE, "").put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            Assert.assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception on empty body", e, response );
        }
    }

    @Test
    public void testGetContainerInfo() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        KieContainerResource info = reply.getResult();
        Assert.assertEquals( KieContainerStatus.STARTED, info.getStatus() );
    }

    @Test
    public void testGetContainerInfoNonExisting() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testListContainers() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        client.createContainer("kie2", new KieContainerResource("kie2", releaseId1));
        ServiceResponse<KieContainerResourceList> reply = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(2, reply.getResult().getContainers().size());
    }

    @Test
    public void testDisposeContainer() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        ServiceResponse<Void> reply = client.disposeContainer("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCallContainer() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));

        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <insert out-identifier=\"message\">\n" +
                "    <org.pkg1.Message>\n" +
                "      <text>Hello World</text>\n" +
                "    </org.pkg1.Message>\n" +
                "  </insert>\n" +
                "  <fire-all-rules/>\n" +
                "</batch-execution>";

        ServiceResponse<String> reply = client.executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testUpdateVersion() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        
        ServiceResponse<ReleaseId> v = client.updateReleaseId("kie1", releaseId2);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, v.getType());
        Assert.assertEquals(releaseId2, v.getResult());
        
        ServiceResponse<Void> reply = client.disposeContainer("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCallContainerMarshallCommands() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));

        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId1).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        Method getter = messageClass.getMethod("getText");
        setter.invoke( message, "HelloWorld");

        KieCommands kcmd = ks.getCommands();
        Command<?> insert = kcmd.newInsert(message, "message");
        Command<?> fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");

        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        ServiceResponse<String> reply = client.executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        XStream xs = BatchExecutionHelper.newXStreamMarshaller();
        xs.setClassLoader(cl);
        ExecutionResults results = (ExecutionResults) xs.fromXML(reply.getResult());
        Object value = results.getValue("message");
        Assert.assertEquals("echo:HelloWorld", getter.invoke(value));
    }

    @Test
    public void testCommandScript() throws Exception {
        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId1).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        setter.invoke(message, "HelloWorld");

        KieCommands kcmd = ks.getCommands();
        Command<?> insert = kcmd.newInsert(message, "message");
        Command<?> fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");

        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        KieServerCommand create = new CreateContainerCommand(new KieContainerResource( "kie1", releaseId1, null));
        KieServerCommand call = new CallContainerCommand("kie1", payload);
        KieServerCommand dispose = new DisposeContainerCommand("kie1");

        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScript(cmds);

        List<ServiceResponse<? extends Object>> reply = client.executeScript(script);

        for (ServiceResponse<? extends Object> r : reply) {
            Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, r.getType());
        }
    }

    @Test
    public void testCallContainerLookupError() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));

        String payload = "<batch-execution lookup=\"xyz\">\n" +
                "  <insert out-identifier=\"message\">\n" +
                "    <org.pkg1.Message>\n" +
                "      <text>Hello World</text>\n" +
                "    </org.pkg1.Message>\n" +
                "  </insert>\n" +
                "</batch-execution>";

        ServiceResponse<String> reply = client.executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testScanner() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        ServiceResponse<KieScannerResource> si = client.getScannerInfo("kie1");
        Assert.assertEquals( ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
        
        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.STARTED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );
        
        si = client.getScannerInfo("kie1");
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );
        
        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.STOPPED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );
        
        si = client.getScannerInfo("kie1");
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );
        
        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.DISPOSED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
        
        si = client.getScannerInfo("kie1");
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
    }

    public static byte[] createAndDeployJar(KieServices ks,
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
        return jar;
    }

    private void startServer() throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerRestImpl());
    }

    private void startClient() throws Exception {
        client = new KieServicesClient(BASE_URI);
    }

    private static void createAndDeployKJar(ReleaseId releaseId) {
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

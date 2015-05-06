package org.kie.server.integrationtests.drools;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
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
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public class KieServerIntegrationTest extends RestJmsSharedBaseIntegrationTest {
    private static ReleaseId releaseId1 = new ReleaseId("foo.bar", "baz", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("foo.bar", "baz", "2.1.1.GA");

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId1);
        createAndDeployKJar(releaseId2);
    }

    @Test
    public void testGetServerInfo() throws Exception {
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        KieServerInfo info = reply.getResult();
        Assert.assertEquals(getServerVersion(), info.getVersion());
    }

    private String getServerVersion() {
        // use the property if specified and fallback to KieServerEnvironment if no property set
        return System.getProperty("kie.server.version", KieServerEnvironment.getVersion().toString());
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

        String containerId = "command-script-container";
        KieServerCommand create = new CreateContainerCommand(new KieContainerResource( containerId, releaseId1, null));
        KieServerCommand call = new CallContainerCommand(containerId, payload);
        KieServerCommand dispose = new DisposeContainerCommand(containerId);

        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScript(cmds);

        ServiceResponsesList reply = client.executeScript(script);

        for (ServiceResponse<? extends Object> r : reply.getResponses()) {
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

    @Test
    public void testScannerScanNow() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ServiceResponse<KieScannerResource> si = client.getScannerInfo("kie1");
        Assert.assertEquals( ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );

        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.SCANNING, 0l));
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

    @Test
    public void testScannerStatusOnContainerInfo() throws Exception {
        client.createContainer("kie1", new KieContainerResource("kie1", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("kie1");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        KieContainerResource kci = reply.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, kci.getScanner().getStatus() );

        ServiceResponse<KieScannerResource> si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.STARTED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );

        kci = client.getContainerInfo( "kie1" ).getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, kci.getScanner().getStatus() );
        Assert.assertEquals( 10000, kci.getScanner().getPollInterval().longValue() );

        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.STOPPED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );

        kci = client.getContainerInfo( "kie1" ).getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, kci.getScanner().getStatus() );

        si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.DISPOSED, 10000l));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );

        kci = client.getContainerInfo( "kie1" ).getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, kci.getScanner().getStatus() );
    }
}

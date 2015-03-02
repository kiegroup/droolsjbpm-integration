package org.kie.services.client.builder;

import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InitialContext.class, NamingManager.class})
public class RemoteWebservicesClientBuilderTest extends org.kie.services.client.api.RemoteJmsRuntimeEngineFactory {

    protected static Logger logger = LoggerFactory.getLogger(RemoteWebservicesClientBuilderTest.class);
    
    public RemoteWebservicesClientBuilderTest() { 
        super();
    }
    
    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() throws Exception {  // Create initial context
        System.out.println( ">>> " + testName.getMethodName());
    }
    
    @Test
    public void commandWebServiceClientInterfaceInheritanceTest() { 
        try { 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .buildBasicAuthClient();
        } catch( Exception e ) { 
            // the above just needs to compile..
        }
    }
}

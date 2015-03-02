package org.kie.services.client.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;

import org.jbpm.bpmn2.objects.Person;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.exception.MissingRequiredInfoException;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;
import org.kie.services.client.builder.objects.MyType;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InitialContext.class, NamingManager.class})
public class RemoteRestClientBuilderTest extends org.kie.services.client.api.RemoteJmsRuntimeEngineFactory {

    protected static Logger logger = LoggerFactory.getLogger(RemoteRestClientBuilderTest.class);
    
    private InitialContext remoteInitialContext = null;
    
    private ConnectionFactory connectionFactory = null;
    private Queue ksessionQueue = null;
    private Queue taskQueue = null;
    private Queue responseQueue = null;
  
    public RemoteRestClientBuilderTest() { 
        super();
    }
    
    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() throws Exception {  // Create initial context
        System.out.println( ">>> " + testName.getMethodName());
    }
    
    @Test
    public void restRuntimeFactoryBuilderTest() throws MalformedURLException, InsufficientInfoToBuildException { 
       org.kie.remote.client.api.RemoteRestRuntimeEngineFactory restRuntimeFactory = 
               RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addProcessInstanceId(23l)
               .addUserName("S")
               .addPassword("koek")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .addExtraJaxbClasses(MyType.class, Person.class)
               .buildFactory();
       assertNotNull( restRuntimeFactory );
       
       try { 
           RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addPassword("poffertje")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .buildFactory();
           fail( "A user name should always be required!");
       } catch(InsufficientInfoToBuildException e) { 
          // expected
       }
       
       try { 
           RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addUserName("A")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .buildFactory();
           fail( "A password should always be required!");
       } catch(InsufficientInfoToBuildException e) { 
          // expected 
       }
       
       try { 
           RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addUserName("E")
               .addPassword("suiker")
               .addTimeout(3)
               .buildFactory();
           fail( "A URL should always be required!");
       } catch(InsufficientInfoToBuildException e) { 
          // expected 
       }
      
       // minimum
       RemoteRestRuntimeEngineFactory.newBuilder()
               .addUserName("joke")
               .addPassword("stroop")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .buildFactory();
    }

    private RemoteConfiguration getConfig(org.kie.services.client.api.RemoteJmsRuntimeEngineFactory factory) throws Exception { 
        Field configField = org.kie.services.client.api.RemoteJmsRuntimeEngineFactory.class.getDeclaredField("config");
        configField.setAccessible(true);
        Object configObj = configField.get(factory);
        assertNotNull("No config found.", configObj);
        return (RemoteConfiguration) configObj;
    }
    
    @Test
    public void missingDeploymentIdTest() throws Exception { 
        RuntimeEngine runtimeEngine = 
                RemoteRestRuntimeEngineFactory.newBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();
        
        try { 
            runtimeEngine.getTaskService().claim(23l, "user");
            fail( "This should have failed because there's no server running... ");
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        try { 
            runtimeEngine.getAuditService().clear();
            fail( "This should have failed because there's no server running... ");
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        // This will throw a MissingRequiredInfoException because the deployment id is required here
        try { 
            runtimeEngine.getKieSession().startProcess("org.test.process"); 
            fail( "This should have failed because no deployment id has been provided. ");
        } catch( MissingRequiredInfoException mrie ) { 
            // expected
        }
   
    }
    
    @Test
    public void orderedRestBuilderTest() throws Exception {
        RemoteRuntimeEngineFactory.newOrderedRestBuilder()
        .addUserName("user")
        .addPassword("pass")
        .addUrl("http://localhost:8080/kie-wb/")
        .build();
    }
    
    
}

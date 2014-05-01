package org.kie.services.client.builder;

import static org.junit.Assert.*;
import static org.kie.services.client.api.command.RemoteConfiguration.*;
import static org.powermock.api.mockito.PowerMockito.*;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.jbpm.bpmn2.objects.Person;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactoryBuilderImpl;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineFactoryBuilder;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.builder.exception.MissingRequiredInfoException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;
import org.kie.services.client.builder.objects.MyType;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InitialContext.class, NamingManager.class})
public class RemoteRuntimeEngineBuilderTest extends RemoteJmsRuntimeEngineFactory {

    protected static Logger logger = LoggerFactory.getLogger(RemoteRuntimeEngineBuilderTest.class);
    
    private InitialContext remoteInitialContext = null;
    
    private ConnectionFactory connectionFactory = null;
    private Queue ksessionQueue = null;
    private Queue taskQueue = null;
    private Queue responseQueue = null;
  
    public RemoteRuntimeEngineBuilderTest() { 
        super();
    }
    
    @BeforeClass
    public static void beforeClass() { 
    }
   
    @Before
    public void before() throws Exception {  // Create initial context
        mockStatic( NamingManager.class );
        
        this.remoteInitialContext = mock(InitialContext.class);
        try {
            Mockito.when(NamingManager.getInitialContext(Mockito.any(Properties.class))).thenReturn(remoteInitialContext);
        } catch (NamingException e) {
            // do nothing..
        }
        String prop = CONNECTION_FACTORY_NAME;
        this.connectionFactory = mock(ConnectionFactory.class);
        doReturn(this.connectionFactory).when(remoteInitialContext).lookup(prop);
        prop = SSL_CONNECTION_FACTORY_NAME;
        doReturn(this.connectionFactory).when(remoteInitialContext).lookup(prop);
        prop = SESSION_QUEUE_NAME;
        this.ksessionQueue = mock(Queue.class);
        doReturn(this.ksessionQueue).when(remoteInitialContext).lookup(prop);
        prop = TASK_QUEUE_NAME;
        this.taskQueue = mock(Queue.class);
        doReturn(this.taskQueue).when(remoteInitialContext).lookup(prop);
        prop = RESPONSE_QUEUE_NAME;
        this.responseQueue = mock(Queue.class);
        doReturn(responseQueue).when(remoteInitialContext).lookup(prop);
    }
    
    @Test
    public void restRuntimeFactoryBuilderTest() throws MalformedURLException, InsufficientInfoToBuildException { 
       RemoteRestRuntimeEngineFactory restRuntimeFactory = 
               RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addProcessInstanceId(23l)
               .addUserName("S")
               .addPassword("koek")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .useFormBasedAuth(true)
               .addExtraJaxbClasses(MyType.class, Person.class)
               .build();
       assertNotNull( restRuntimeFactory );
       
       try { 
           RemoteRestRuntimeEngineFactory.newBuilder()
               .addDeploymentId("deployment")
               .addPassword("poffertje")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .useFormBasedAuth(true)
               .build();
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
               .useFormBasedAuth(true)
               .build();
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
               .useFormBasedAuth(true)
               .build();
           fail( "A URL should always be required!");
       } catch(InsufficientInfoToBuildException e) { 
          // expected 
       }
      
       // minimum
       RemoteRestRuntimeEngineFactory.newBuilder()
               .addUserName("joke")
               .addPassword("stroop")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .build();
    }

    @Test
    public void jmsRuntimeFactoryBuilderTest() throws InsufficientInfoToBuildException { 
        // url + all options
        RemoteJmsRuntimeEngineFactory jmsRuntimeFactory = 
                RemoteJmsRuntimeEngineFactory.newBuilder()
                .addDeploymentId("deployment")
                .addProcessInstanceId(46l)
                .addUserName("C")
                .addPassword("cake")
                .addRemoteInitialContext(remoteInitialContext)
                .addTimeout(3)
                .addExtraJaxbClasses(MyType.class)
                .useSsl(false)
                .build();
       assertNotNull( jmsRuntimeFactory );
        
        // context, minimum
        jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                .addRemoteInitialContext(remoteInitialContext)
                .addUserName("E*")
                .addPassword("koffie")
                .build();
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addRemoteInitialContext(remoteInitialContext)
                    .addPassword("koffie")
                    .build();
            fail( "A user name should always be required!");
        } catch(InsufficientInfoToBuildException e) { 
            // expected 
        }
                
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addRemoteInitialContext(remoteInitialContext)
                    .addUserName("E*")
                    .build();
            fail( "A password should always be required!");
        } catch(InsufficientInfoToBuildException e) { 
            // expected 
        }
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addUserName("E*")
                    .addPassword("koffie")
                    .build();
            fail( "An inital context or server url should always be required!");
        } catch(InsufficientInfoToBuildException e) { 
            // expected 
        }
                
        // queue collection, minimum
        jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("M")
                .addPassword("koekje")
                .addKieSessionQueue(mock(Queue.class))
                .addTaskServiceQueue(mock(Queue.class))
                .addResponseQueue(mock(Queue.class))
                .addConnectionFactory(mock(ConnectionFactory.class))
                .build();
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addTaskServiceQueue(mock(Queue.class))
                    .addResponseQueue(mock(Queue.class))
                    .addConnectionFactory(mock(ConnectionFactory.class))
                    .build();
            
            jmsRuntimeFactory.newRuntimeEngine().getKieSession();
            fail( "A ksession queue is required for a ksession!");
        } catch( MissingRequiredInfoException e) { 
            // expected
        }
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addKieSessionQueue(mock(Queue.class))
                    .addResponseQueue(mock(Queue.class))
                    .addConnectionFactory(mock(ConnectionFactory.class))
                    .build();
            
            jmsRuntimeFactory.newRuntimeEngine().getTaskService();
            fail( "A task service queue is always required!");
        } catch( MissingRequiredInfoException e) { 
            // expected
        }
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addKieSessionQueue(mock(Queue.class))
                    .addTaskServiceQueue(mock(Queue.class))
                    .addConnectionFactory(mock(ConnectionFactory.class))
                    .build();
            fail( "A response queue is always required!");
        } catch( InsufficientInfoToBuildException e) { 
            // expected
        }
        
        try { 
            jmsRuntimeFactory = RemoteJmsRuntimeEngineFactory.newBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addKieSessionQueue(mock(Queue.class))
                    .addTaskServiceQueue(mock(Queue.class))
                    .addResponseQueue(mock(Queue.class))
                    .build();
            fail( "A connection factory is always required!");
        } catch( InsufficientInfoToBuildException e) { 
            // expected
        }
        
    }
    
    @Test
    public void jmsSslRuntimeFactoryAndBuilderTest() throws Exception { 

        String hostName = "host-local";
        int port = 12345;
        RemoteJmsRuntimeEngineFactoryBuilder builder = RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
        .addUserName("H")
        .addPassword("gummy bears")
        .addHostName(hostName)
        .addJmsConnectorPort(port)
        .addKieSessionQueue(ksessionQueue)
        .addResponseQueue(responseQueue)
        .useSsl(true);
    
        // this doesn't really test what I want.. but it's better than nothing? Maybe? 
        {
        Field hostNameField = RemoteJmsRuntimeEngineFactoryBuilderImpl.class.getDeclaredField("hostName");
        hostNameField.setAccessible(true);
        assertEquals( hostName, hostNameField.get(builder) );
        }
        {
        Field portField = RemoteJmsRuntimeEngineFactoryBuilderImpl.class.getDeclaredField("jmsConnectorPort");
        portField.setAccessible(true);
        assertEquals( port, portField.get(builder) );
        }
       
        try { 
            RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            // .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue)
            .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) {
            assertTrue( iitbe.getMessage().contains( "JMS ConnectionFactory"));
        }
        
        try { 
            RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
             // .addJmsConnectorPort(5446)
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue)
            .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) {
            assertTrue( iitbe.getMessage().contains( "JMS ConnectionFactory"));
        }
        
        RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue)
            .build();
          
        // SSL
        try { 
            RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
            .useSsl(true)
            .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) { 
            // expected
        }
        
        try { 
            RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder()
            .useSsl(true)
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) {
            assertTrue( iitbe.getMessage().contains( "SSL"));
        }
        
        RemoteJmsRuntimeEngineFactory.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKeystorePassword("R")
            .addKeystoreLocation("ssl/client_keystore.jks")
            .addTruststorePassword("D")
            .addTruststoreLocation("ssl/truststore.jts")
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue)
            .build();
            
        try { 
            RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("H")
                .addPassword("gummy bears")
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystorePassword("R")
                .addKeystoreLocation("ssl/DOES_NOT_EXIST.jks")
                .addTruststorePassword("D")
                .addTruststoreLocation("ssl/truststore.jts")
                .addKieSessionQueue(ksessionQueue)
                .addResponseQueue(responseQueue)
                .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) { 
            assertTrue( iitbe.getMessage().contains( "could be found on the classpath"));
        }
     
        try { 
            RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("H")
                .addPassword("gummy bears")
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystorePassword("R")
                .addKeystoreLocation("ssl/client_keystore.jks")
                .addTruststorePassword("D")
                .addTruststoreLocation("/ssl/truststore.jts")
                .addKieSessionQueue(ksessionQueue)
                .addResponseQueue(responseQueue)
                .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) { 
            assertTrue( iitbe.getMessage().contains( "could be found at"));
        }
        
        try { 
            RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("H")
                .addPassword("gummy bears")
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                // .addKeystorePassword("R")
                .addKeystoreLocation("ssl/client_keystore.jks")
                .addTruststorePassword("D")
                .addTruststoreLocation("/ssl/truststore.jts")
                .addKieSessionQueue(ksessionQueue)
                .addResponseQueue(responseQueue)
                .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) { 
            assertTrue( iitbe.getMessage().contains( "password is required"));
        }
        
        try { 
            RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("H")
                .addPassword("gummy bears")
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystorePassword("R")
                .addKeystoreLocation("ssl/client_keystore.jks")
                // .addTruststorePassword("D")
                .addTruststoreLocation("/ssl/truststore.jts")
                .addKieSessionQueue(ksessionQueue)
                .addResponseQueue(responseQueue)
                .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) { 
            assertTrue( iitbe.getMessage().contains( "password is required"));
        }
       
        // remote initial context
        RemoteJmsRuntimeEngineFactory.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKeystorePassword("R")
            .addKeystoreLocation("ssl/client_keystore.jks")
            .addTruststorePassword("D")
            .addTruststoreLocation("ssl/truststore.jts")
            .addRemoteInitialContext(remoteInitialContext)
            .build();
        
        // jboss server url
        RemoteJmsRuntimeEngineFactory.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKeystorePassword("R")
            .addKeystoreLocation("ssl/client_keystore.jks")
            .addTruststorePassword("D")
            .addTruststoreLocation("ssl/truststore.jts")
            .addJbossServerUrl(new URL("http://localhost:8080/kie-wb"))
            .build();
       
        // useKeystoreAsTruststore
        builder = RemoteJmsRuntimeEngineFactory.newBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKeystorePassword("R")
            .addKeystoreLocation("ssl/client_keystore.jks")
            .useKeystoreAsTruststore()
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue);
           
        builder.build();
    }
    
    @Test
    public void jmsRuntimeFactoryBuilderReuseTest() throws Exception { 
        RemoteJmsRuntimeEngineFactoryBuilder jreFactoryBuilder = RemoteJmsRuntimeEngineFactory.newBuilder()
                .addDeploymentId("deploymentId")
                .useSsl(true)
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystoreLocation("ssl/client_keystore.jks")
                .addKeystorePassword("CLIENT_KEYSTORE_PASSWORD")
                .useKeystoreAsTruststore();
               
        try { 
            jreFactoryBuilder
            .addTaskServiceQueue((Queue) remoteInitialContext.lookup(TASK_QUEUE_NAME))
            .addKieSessionQueue((Queue) remoteInitialContext.lookup(SESSION_QUEUE_NAME))
            .addResponseQueue((Queue) remoteInitialContext.lookup(RESPONSE_QUEUE_NAME));
        } catch( Exception e ) { 
            String msg = "Unable to lookup queue instances: " + e.getMessage();
            logger.error(msg, e);
            fail(msg);
        }

        String krisUser = "kris";
        String krisPassword = "kris123@";
        RemoteRuntimeEngineFactory krisRemoteEngineFactory = jreFactoryBuilder
                .addUserName(krisUser)
                .addPassword(krisPassword)
                .build();

        String maryUser = "mary";
        String maryPass = "mary123@";
        RemoteRuntimeEngineFactory maryRemoteEngineFactory = jreFactoryBuilder
                .addUserName(maryUser)
                .addPassword(maryPass)
                .build();

        String johnUser = "john";
        String johnPassword = "john123@";
        RemoteRuntimeEngineFactory johnRemoteEngineFactory = jreFactoryBuilder
                .addUserName(johnUser)
                .addPassword(johnPassword)
                .build();
        
        RemoteConfiguration maryConfig = getConfig((RemoteJmsRuntimeEngineFactory)maryRemoteEngineFactory);
        assertEquals( maryUser, maryConfig.getUserName());
        assertEquals( maryPass, maryConfig.getPassword());
        
        RemoteConfiguration krisConfig = getConfig((RemoteJmsRuntimeEngineFactory)krisRemoteEngineFactory);
        assertEquals( krisUser, krisConfig.getUserName());
        assertEquals( krisPassword, krisConfig.getPassword());
        
        RemoteConfiguration johnConfig = getConfig((RemoteJmsRuntimeEngineFactory)johnRemoteEngineFactory);
        assertEquals( johnUser, johnConfig.getUserName());
        assertEquals( johnPassword, johnConfig.getPassword());
        
    }
    
    private RemoteConfiguration getConfig(RemoteJmsRuntimeEngineFactory factory) throws Exception { 
        Field configField = RemoteJmsRuntimeEngineFactory.class.getDeclaredField("config");
        configField.setAccessible(true);
        Object configObj = configField.get(factory);
        assertNotNull("No config found.", configObj);
        return (RemoteConfiguration) configObj;
    }
    
    @Test
    public void remoteConfigurationCloneTest() throws Exception { 
       RemoteConfiguration orig = new RemoteConfiguration("deploy", 
               mock(ConnectionFactory.class), 
               mock(Queue.class), 
               mock(Queue.class), 
               mock(Queue.class), 
               "user", "pass");
       orig.setExtraJaxbClasses(new HashSet<Class<?>>());
       orig.setProcessInstanceId(123l);
       orig.setRemoteInitialContext(remoteInitialContext);
       orig.setTimeout(23l);
       orig.setUseSsl(false);
       
       setField(orig, "jmsSerializationType", 4);
       setField(orig, "requestFactory", mock(ClientRequestFactory.class));
       
       RemoteConfiguration copy = orig.clone();
       
       Field[] fields = RemoteConfiguration.class.getDeclaredFields();
       for( Field field : fields ) { 
           field.setAccessible(true);
           Object origVal = field.get(orig);
           assertNotNull( field.getName() + " should be set to a non-null value to be compared.");
           assertEquals( field.getName() + " not equal in RemoteConfiguration clone.", origVal, field.get(copy));
       }
    }
    
    private void setField(Object obj, String fieldName, Object val) throws Exception { 
       Field field = obj.getClass().getDeclaredField(fieldName);
       field.setAccessible(true);
       field.set(obj, val);
    }
    
    @Test
    public void missingDeploymentIdTest() throws Exception { 
        RemoteRuntimeEngineFactory factory = 
                RemoteRestRuntimeEngineFactory.newBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();
        
        RemoteRuntimeEngine runtimeEngine = factory.newRuntimeEngine();
        try { 
            runtimeEngine.getTaskService().claim(23l, "user");
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        try { 
            runtimeEngine.getAuditLogService().clear();
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        // This will throw a MissingRequiredInfoException because the deployment id is required here
        try { 
            runtimeEngine.getKieSession().startProcess("org.test.process"); 
        } catch( MissingRequiredInfoException mrie ) { 
            // expected
        }
       
        factory = 
                RemoteJmsRuntimeEngineFactory.newBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addRemoteInitialContext(remoteInitialContext)
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystorePassword("R")
                .addKeystoreLocation("ssl/client_keystore.jks")
                .useKeystoreAsTruststore()
                .useSsl(true)
                .build();
        
        runtimeEngine = factory.newRuntimeEngine();
        try { 
            runtimeEngine.getTaskService().claim(23l, "user");
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        try { 
            runtimeEngine.getAuditLogService().clear();
        } catch( RemoteCommunicationException rce ) { 
            // expected
        }
        
        // This will throw a MissingRequiredInfoException because the deployment id is required here
        try { 
            runtimeEngine.getKieSession().startProcess("org.test.process"); 
        } catch( MissingRequiredInfoException mrie ) { 
            // expected
        }
    }
}

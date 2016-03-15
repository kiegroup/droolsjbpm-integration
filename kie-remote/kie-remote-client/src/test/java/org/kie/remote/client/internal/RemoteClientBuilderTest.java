/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.client.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.remote.client.internal.command.RemoteConfiguration.CONNECTION_FACTORY_NAME;
import static org.kie.remote.client.internal.command.RemoteConfiguration.RESPONSE_QUEUE_NAME;
import static org.kie.remote.client.internal.command.RemoteConfiguration.SESSION_QUEUE_NAME;
import static org.kie.remote.client.internal.command.RemoteConfiguration.SSL_CONNECTION_FACTORY_NAME;
import static org.kie.remote.client.internal.command.RemoteConfiguration.TASK_QUEUE_NAME;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.jbpm.bpmn2.objects.Person;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.remote.client.api.exception.MissingRequiredInfoException;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.internal.command.RemoteConfiguration;
import org.kie.remote.client.internal.command.RemoteRuntimeEngine;
import org.kie.test.objects.MyType;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InitialContext.class, NamingManager.class})
public class RemoteClientBuilderTest extends RemoteJmsRuntimeEngineBuilderImpl {

    protected static Logger logger = LoggerFactory.getLogger(RemoteClientBuilderTest.class);

    private InitialContext remoteInitialContext = null;

    private ConnectionFactory connectionFactory = null;
    private Queue ksessionQueue = null;
    private Queue taskQueue = null;
    private Queue responseQueue = null;

    public RemoteClientBuilderTest() {
        super();
    }

    @Rule
    public TestName testName = new TestName();

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

        System.out.println( ">>> " + testName.getMethodName());
    }

    @Test
    public void restRuntimeFactoryBuilderTest() throws MalformedURLException, InsufficientInfoToBuildException {
       RuntimeEngine restRuntimeEngine = RemoteRuntimeEngineFactory.newRestBuilder()
               .addDeploymentId("deployment")
               .addProcessInstanceId(23l)
               .addUserName("S")
               .addPassword("koek")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .addExtraJaxbClasses(MyType.class, Person.class)
               .build();
       assertNotNull( restRuntimeEngine );

       try {
           RemoteRuntimeEngineFactory.newRestBuilder()
               .addDeploymentId("deployment")
               .addPassword("poffertje")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .build();
           fail( "A user name should always be required!");
       } catch(InsufficientInfoToBuildException e) {
          // expected
       }

       try {
           RemoteRuntimeEngineFactory.newRestBuilder()
               .addDeploymentId("deployment")
               .addUserName("A")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .addTimeout(3)
               .build();
           fail( "A password should always be required!");
       } catch(InsufficientInfoToBuildException e) {
          // expected
       }

       try {
           RemoteRuntimeEngineFactory.newRestBuilder()
               .addDeploymentId("deployment")
               .addUserName("E")
               .addPassword("suiker")
               .addTimeout(3)
               .build();
           fail( "A URL should always be required!");
       } catch(InsufficientInfoToBuildException e) {
          // expected
       }

       // minimum
       RemoteRuntimeEngineFactory.newRestBuilder()
               .addUserName("joke")
               .addPassword("stroop")
               .addUrl(new URL("http://localhost:8080/kie-wb"))
               .build();
    }

    @Test
    public void jmsRuntimeFactoryBuilderTest() throws InsufficientInfoToBuildException {
        // url + all options
        RuntimeEngine jmsRuntimeEngine =
                RemoteRuntimeEngineFactory.newJmsBuilder()
                .addDeploymentId("deployment")
                .addProcessInstanceId(46l)
                .addUserName("C")
                .addPassword("cake")
                .addRemoteInitialContext(remoteInitialContext)
                .addTimeout(3)
                .addExtraJaxbClasses(MyType.class)
                .useSsl(false)
                .build();
       assertNotNull( jmsRuntimeEngine );

        // context, minimum
        jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                .addRemoteInitialContext(remoteInitialContext)
                .addUserName("E*")
                .addPassword("koffie")
                .build();

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                    .addRemoteInitialContext(remoteInitialContext)
                    .addPassword("koffie")
                    .build();
            fail( "A user name should always be required!");
        } catch(InsufficientInfoToBuildException e) {
            // expected
        }

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                    .addRemoteInitialContext(remoteInitialContext)
                    .addUserName("E*")
                    .build();
            fail( "A password should always be required!");
        } catch(InsufficientInfoToBuildException e) {
            // expected
        }

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                    .addUserName("E*")
                    .addPassword("koffie")
                    .build();
            fail( "An inital context or server url should always be required!");
        } catch(InsufficientInfoToBuildException e) {
            // expected
        }

        // queue collection, minimum
        jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                .addUserName("M")
                .addPassword("koekje")
                .addKieSessionQueue(mock(Queue.class))
                .addTaskServiceQueue(mock(Queue.class))
                .addResponseQueue(mock(Queue.class))
                .addConnectionFactory(mock(ConnectionFactory.class))
                .build();

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addTaskServiceQueue(mock(Queue.class))
                    .addResponseQueue(mock(Queue.class))
                    .addConnectionFactory(mock(ConnectionFactory.class))
                    .build();

            jmsRuntimeEngine.getKieSession();
            fail( "A ksession queue is required for a ksession!");
        } catch( MissingRequiredInfoException e) {
            // expected
        }

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
                    .addUserName("1")
                    .addPassword("ijs")
                    .addKieSessionQueue(mock(Queue.class))
                    .addResponseQueue(mock(Queue.class))
                    .addConnectionFactory(mock(ConnectionFactory.class))
                    .build();

            jmsRuntimeEngine.getTaskService();
            fail( "A task service queue is always required!");
        } catch( MissingRequiredInfoException e) {
            // expected
        }

        try {
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
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
            jmsRuntimeEngine = RemoteRuntimeEngineFactory.newJmsBuilder()
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
    public void jmsSslRuntimeEngineAndBuilderTest() throws Exception {

        String hostName = "host-local";
        int port = 12345;
        RemoteJmsRuntimeEngineBuilder builder = RemoteRuntimeEngineFactory.newJmsBuilder()
        .addUserName("H")
        .addPassword("gummy bears")
        .addHostName(hostName)
        .addJmsConnectorPort(port)
        .addKieSessionQueue(ksessionQueue)
        .addResponseQueue(responseQueue)
        .useSsl(true);

        // this doesn't really test what I want.. but it's better than nothing? Maybe?
        {
            Field hostNameField =
            Class.forName("org.kie.remote.client.internal.RemoteJmsRuntimeEngineBuilderImpl")
            .getDeclaredField("hostName");
            hostNameField.setAccessible(true);
            assertEquals( hostName, hostNameField.get(builder) );
        }
        {
            Field portField =
            Class.forName("org.kie.remote.client.internal.RemoteJmsRuntimeEngineBuilderImpl")
            .getDeclaredField("jmsConnectorPort");
            portField.setAccessible(true);
            assertEquals( port, portField.get(builder) );
        }

        try {
            RemoteRuntimeEngineFactory.newJmsBuilder()
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
            RemoteRuntimeEngineFactory.newJmsBuilder()
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

        RemoteRuntimeEngineFactory.newJmsBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKieSessionQueue(ksessionQueue)
            .addResponseQueue(responseQueue)
            .build();

        // SSL
        try {
            RemoteRuntimeEngineFactory.newJmsBuilder()
            .useSsl(true)
            .build();
            fail( "Should have thrown exception");
        } catch( InsufficientInfoToBuildException iitbe ) {
            // expected
        }

        try {
            RemoteRuntimeEngineFactory.newJmsBuilder()
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

        RemoteRuntimeEngineFactory.newJmsBuilder()
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
            RemoteRuntimeEngineFactory.newJmsBuilder()
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
            RemoteRuntimeEngineFactory.newJmsBuilder()
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
            RemoteRuntimeEngineFactory.newJmsBuilder()
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
            RemoteRuntimeEngineFactory.newJmsBuilder()
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
        RemoteRuntimeEngineFactory.newJmsBuilder()
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
        RemoteRuntimeEngineFactory.newJmsBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addKeystorePassword("R")
            .addKeystoreLocation("ssl/client_keystore.jks")
            .addTruststorePassword("D")
            .addTruststoreLocation("ssl/truststore.jts")
            .addJbossServerHostName("localhost")
            .build();

        // useKeystoreAsTruststore
        builder = RemoteRuntimeEngineFactory.newJmsBuilder()
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
    public void jmsRuntimeEngineNoSslTest() {
        // disableTaskSecurity
        RemoteRuntimeEngineFactory.newJmsBuilder()
            .addUserName("H")
            .addPassword("gummy bears")
            .addHostName("localhost")
            .addJmsConnectorPort(5446)
            .addRemoteInitialContext(remoteInitialContext)
            .disableTaskSecurity()
            .build();
    }

    @Test
    public void jmsRuntimeFactoryBuilderReuseTest() throws Exception {
        RemoteJmsRuntimeEngineBuilder runtimeEngineBuilder = RemoteRuntimeEngineFactory.newJmsBuilder()
                .addDeploymentId("deploymentId")
                .useSsl(true)
                .addHostName("localhost")
                .addJmsConnectorPort(5446)
                .addKeystoreLocation("ssl/client_keystore.jks")
                .addKeystorePassword("CLIENT_KEYSTORE_PASSWORD")
                .useKeystoreAsTruststore();

        try {
            runtimeEngineBuilder
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
        RuntimeEngine krisRemoteEngine = runtimeEngineBuilder
                .addUserName(krisUser)
                .addPassword(krisPassword)
                .build();

        String maryUser = "mary";
        String maryPass = "mary123@";
        RuntimeEngine maryRemoteEngine = runtimeEngineBuilder
                .addUserName(maryUser)
                .addPassword(maryPass)
                .build();

        String johnUser = "john";
        String johnPassword = "john123@";
        RuntimeEngine johnRemoteEngine = runtimeEngineBuilder
                .addUserName(johnUser)
                .addPassword(johnPassword)
                .build();

        RemoteConfiguration maryConfig = getConfig(maryRemoteEngine);
        assertEquals( maryUser, maryConfig.getUserName());
        assertEquals( maryPass, maryConfig.getPassword());

        RemoteConfiguration krisConfig = getConfig(krisRemoteEngine);
        assertEquals( krisUser, krisConfig.getUserName());
        assertEquals( krisPassword, krisConfig.getPassword());

        RemoteConfiguration johnConfig = getConfig(johnRemoteEngine);
        assertEquals( johnUser, johnConfig.getUserName());
        assertEquals( johnPassword, johnConfig.getPassword());

    }

    private RemoteConfiguration getConfig(RuntimeEngine runtimeEngine) throws Exception {
        Field configField = RemoteRuntimeEngine.class.getDeclaredField("config");
        configField.setAccessible(true);
        Object configObj = configField.get(runtimeEngine);
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
        RuntimeEngine runtimeEngine =
                RemoteRuntimeEngineFactory.newRestBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();

        try {
            runtimeEngine.getTaskService().claim(23l, "user");
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

        runtimeEngine =
                RemoteRuntimeEngineFactory.newJmsBuilder()
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

        try {
            runtimeEngine.getTaskService().claim(23l, "user");
            fail( "This should have failed because there's no server running... ");
        } catch( RemoteCommunicationException rce ) {
            logger.info("The " + NoSuchAlgorithmException.class.getSimpleName() + " above is expected, nothing is wrong.");
            // expected
        }

        try {
            runtimeEngine.getAuditService().clear();
            fail( "This should have failed because there's no server running... ");
        } catch( RemoteCommunicationException rce ) {
            logger.info("The " + NoSuchAlgorithmException.class.getSimpleName() + " above is expected, nothing is wrong.");
            // expected
        }

        // This will throw a MissingRequiredInfoException because the deployment id is required here
        try {
            runtimeEngine.getKieSession().startProcess("org.test.process");
        } catch( MissingRequiredInfoException mrie ) {
            // expected
        }
    }

    @Test
    public void commandWebServiceClientInterfaceInheritanceTest() {
        try {
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .setWsdlLocationRelativePath("random/path/to/CommandWebservice.wsdl")
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .setWsdlLocationRelativePath("random/path/to/CommandWebservice.wsdl")
            .useHttpRedirect()
            .addPassword("test")
            .addUserName("tester")
            .addServerUrl("http://test.server.com/test-app/")
            .addServerUrl(new URL("http://test.server.com/test-app/"))
            .buildBasicAuthClient();
        } catch( Exception e ) {
            logger.info("The " + ConnectException.class.getSimpleName() + " above is expected, nothing is wrong.");
            // the above just needs to compile..
        }
    }


    @Test
    public void execptionWhenUserIdDoesNotMatchAuthUserId() throws Exception {
        String authUser = "user";
        RuntimeEngine runtimeEngine =
                RemoteRuntimeEngineFactory.newRestBuilder()
                .addUserName(authUser)
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();

        String otherUser = "notTheSameUser";
        try {
            runtimeEngine.getTaskService().getTasksOwned(otherUser, "en-Uk");
        } catch( RemoteApiException rae ) {
            assertTrue( "Exception should reference incorrect user", rae.getMessage().contains(otherUser));
            assertTrue( "Exception should reference correct/auth user", rae.getMessage().contains(authUser));
            assertTrue( "Exception should explain problem", rae.getMessage().contains("must match the authenticating user"));

        }

        runtimeEngine = RemoteRuntimeEngineFactory.newRestBuilder()
                .addUserName(authUser)
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .disableTaskSecurity()
                .build();

        try {
            runtimeEngine.getTaskService().getTasksOwned(otherUser, "en-Uk");
        } catch( RemoteCommunicationException rce ) {
            assertTrue( "Incorrect exception received", rce.getMessage().contains("Unable to send HTTP POST"));
        }

        System.setProperty("org.kie.task.insecure", "true");
        runtimeEngine = RemoteRuntimeEngineFactory.newRestBuilder()
                .addUserName(authUser)
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();

        try {
            runtimeEngine.getTaskService().getTasksOwned(otherUser, "en-Uk");
        } catch( RemoteCommunicationException rce ) {
            assertTrue( "Incorrect exception received", rce.getMessage().contains("Unable to send HTTP POST"));
        }
    }

}

package org.kie.services.remote;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple test that tests whether the war can be succesfully deployed on WildFly (AS 7).
 */

@RunWith(Arquillian.class)
@ServerSetup(ArquillianJbossServerSetupTask.class)
public class JmsIntegrationTest extends IntegrationBase {

    private static Logger logger = LoggerFactory.getLogger(JmsIntegrationTest.class);

    private static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    private static final String DOMAIN_TASK_QUEUE_NAME = "jms/queue/KIE.TASK.DOMAIN.TEST";
    private static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    private static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";

    /**
     * Initializes a (remote) IntialContext instance.
     * 
     * @return a remote {@link InitialContext} instance
     */
    private static InitialContext getRemoteInitialContext() {
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://localhost:4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, "guest");
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, "1234");
        
        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }

    @Deployment(testable = false)
    public static Archive<?> createWar() {
       return createWebArchive();
    }

    private static final long QUALITY_OF_SERVICE_THRESHOLD_MS = 5 * 1000;

    @After
    public void waitForTxOnServer() throws InterruptedException { 
        Thread.sleep(1000);
    }
    
    @Test
    public void shouldBeAbleToGetMessageBack() throws Exception {
        Message response = sendStartProcessMessage(TASK_QUEUE_NAME);
    }
    
    private Message sendStartProcessMessage(String sendQueueName) throws Exception { 
        InitialContext context = getRemoteInitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);
        Queue jbpmQueue = (Queue) context.lookup(sendQueueName);
        Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_NAME);

        Connection connection = null;
        Session session = null;
        Message response = null;
        try {
            // setup
            connection = factory.createConnection("guest", "1234");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer(jbpmQueue);
            MessageConsumer consumer = session.createConsumer(responseQueue);

            connection.start();

            // Create msg
            BytesMessage msg = session.createBytesMessage();
            msg.setIntProperty("serialization", 1);
            Command<?> cmd = new StartProcessCommand("org.jbpm.scripttask");
            JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
            String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(req);
            msg.writeUTF(xmlStr);
            
            // send
            producer.send(msg);
            
            // receive
            response = consumer.receive(QUALITY_OF_SERVICE_THRESHOLD_MS);
            
            // check
            assertNotNull("Response from MDB was null!", response);
            xmlStr = ((BytesMessage) response).readUTF();
            JaxbCommandsResponse cmdResponse = (JaxbCommandsResponse) JaxbSerializationProvider.convertStringToJaxbObject(xmlStr);
            assertNotNull("Jaxb Cmd Response was null!", cmdResponse);
        } finally {
            if (connection != null) {
                connection.close();
                session.close();
            }
        }
        return response;
    }

}
package org.kie.server.integrationtests.jms;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.*;
import org.slf4j.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

@Ignore
public class JMSCRUDTest
        extends BaseJMSIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId( "org.kie.server.testing", "container-crud-tests1", "2.1.0.GA" );
    private static ReleaseId releaseId2 = new ReleaseId( "org.kie.server.testing", "container-crud-tests1", "2.1.1.GA" );

    private static final Logger log = org.slf4j.LoggerFactory.getLogger( JMSCRUDTest.class );

    @BeforeClass
    public static void initialize()
            throws Exception {
        createAndDeployKJar( releaseId1 );
        createAndDeployKJar( releaseId2 );
    }

    // Set up all the default values
    private static final String DEFAULT_MESSAGE            = "Hello, World!";
    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String DEFAULT_DESTINATION = "java:/jms/queue/KieServerRequest";
    private static final String DEFAULT_MESSAGE_COUNT = "1";
    private static final String DEFAULT_USERNAME = "kieserver";
    private static final String DEFAULT_PASSWORD = "kieserver";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL = "http-remoting://localhost:8080";


    @Test
    public void testCreateContainer() throws Exception {
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        Destination destination = null;
        TextMessage message = null;
        Context context = null;

        try {

            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
            env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", DEFAULT_USERNAME));
            env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", DEFAULT_PASSWORD));
            context = new InitialContext(env);

            String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
            log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
            connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);
            log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

            String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
            log.info("Attempting to acquire destination \"" + destinationString + "\"");
            destination = (Destination) context.lookup(destinationString);
            log.info("Found destination \"" + destinationString + "\" in JNDI");

            // Create the JMS connection, session, producer, and consumer
            connection = connectionFactory.createConnection(System.getProperty("username", DEFAULT_USERNAME), System.getProperty("password", DEFAULT_PASSWORD));
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(destination);
            // consumer = session.createConsumer(destination);
            connection.start();

            int count = Integer.parseInt(System.getProperty("message.count", DEFAULT_MESSAGE_COUNT));
            String content = System.getProperty("message.content", DEFAULT_MESSAGE);

            log.info("Sending " + count + " messages with content: " + content);

            // Send the specified number of messages
            for (int i = 0; i < count; i++) {
                message = session.createTextMessage(content);
                producer.send(message);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            if (context != null) {
                context.close();
            }

            // closing the connection takes care of the session, producer, and consumer
            if (connection != null) {
                connection.close();
            }
        }
    }

}

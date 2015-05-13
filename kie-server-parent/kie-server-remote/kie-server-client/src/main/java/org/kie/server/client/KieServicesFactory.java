package org.kie.server.client;

import org.kie.server.client.impl.KieServicesClientImpl;
import org.kie.server.client.impl.KieServicesConfigurationImpl;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

public class KieServicesFactory {
    private KieServicesFactory() {}

    /**
     * Creates a new configuration object for REST based service
     * @param serverUrl the URL to the server (e.g.: "http://localhost:8080")
     * @param login user login
     * @param password user password
     * @return configuration instance
     */
    public static KieServicesConfiguration newRestConfiguration( String serverUrl, String login, String password ) {
        return new KieServicesConfigurationImpl( serverUrl, login, password );
    }

    /**
     * Creates a new configuration object for REST based service
     * @param serverUrl the URL to the server (e.g.: "http://localhost:8080")
     * @param login user login
     * @param password user password
     * @param timeout the maximum timeout in seconds
     * @return configuration instance
     */
    public static KieServicesConfiguration newRestConfiguration( String serverUrl, String login, String password, int timeout ) {
        return new KieServicesConfigurationImpl( serverUrl, login, password, timeout );
    }

    /**
     * Creates a new configuration object for JMS based service
     * @param connectionFactory a JMS connection factory
     * @param requestQueue a reference to the requests queue
     * @param responseQueue a reference to the responses queue
     * @return configuration instance
     */
    public static KieServicesConfiguration newJMSConfiguration( ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue) {
        return new KieServicesConfigurationImpl( connectionFactory, requestQueue, responseQueue );
    }

    /**
     * Creates a new configuration object for JMS based service
     * @param connectionFactory a JMS connection factory
     * @param requestQueue a reference to the requests queue
     * @param responseQueue a reference to the responses queue
     * @param username user name
     * @param password password
     * @return configuration instance
     */
    public static KieServicesConfiguration newJMSConfiguration( ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue,
                                                                String username, String password ) {
        return new KieServicesConfigurationImpl( connectionFactory, requestQueue, responseQueue, username, password );
    }

    /**
     * Creates a new configuration object for JMS based service
     * @param context a context to look up for the JMS request and response queues
     * @param username user name
     * @param password user password
     * @return configuration instance
     */
    public static KieServicesConfiguration newJMSConfiguration( InitialContext context, String username, String password ) {
        return new KieServicesConfigurationImpl( context, username, password );
    }

    /**
     * Instantiates and return a KieServicesClient instance based on the provided configuration
     * @param conf client configuration
     * @return the KieServicesClient instance
     */
    public static KieServicesClient newKieServicesClient( KieServicesConfiguration conf ) {
        return new KieServicesClientImpl( conf );
    }

    public static KieServicesClient newKieServicesClient( KieServicesConfiguration conf, ClassLoader classLoader ) {
        return new KieServicesClientImpl( conf, classLoader );
    }

    public static KieServicesClient newKieServicesRestClient( String serverUrl, String login, String password ) {
        return new KieServicesClientImpl( newRestConfiguration( serverUrl, login, password ) );
    }

    public static KieServicesClient newKieServicesJMSClient( ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue ) {
        return new KieServicesClientImpl( newJMSConfiguration( connectionFactory, requestQueue, responseQueue ) );
    }

    public static KieServicesClient newKieServicesJMSClient( ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue,
                                                             String username, String password ) {
        return new KieServicesClientImpl( newJMSConfiguration( connectionFactory, requestQueue, responseQueue, username, password ) );
    }

    public static KieServicesClient newKieServicesJMSClient( InitialContext context, String username, String password ) {
        return new KieServicesClientImpl( newJMSConfiguration( context, username, password ) );
    }

}

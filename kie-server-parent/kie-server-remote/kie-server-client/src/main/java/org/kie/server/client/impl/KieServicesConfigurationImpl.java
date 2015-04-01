package org.kie.server.client.impl;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * In order to protect the Remote (Java) API, this class may not be extended nor may its constructor be made public.
 */
public final class KieServicesConfigurationImpl
        implements KieServicesConfiguration {

    public static final String SSL_CONNECTION_FACTORY_NAME = "jms/SslRemoteConnectionFactory";
    public static final String CONNECTION_FACTORY_NAME     = "jms/RemoteConnectionFactory";
    public static final String REQUEST_QUEUE_NAME          = "jms/queue/KIE.SERVER.REQUEST";
    public static final String RESPONSE_QUEUE_NAME         = "jms/queue/KIE.SERVER.RESPONSE";

    private             long timeoutInMillisecs      = 5000; // in milliseconds

    // REST or JMS
    private final Transport transport;

    // General
    private String userName;
    private String password;
    private String serverUrl;

    // JMS
    private boolean useSsl = false;
    private ConnectionFactory connectionFactory;
    private Queue             requestQueue;
    private Queue             responseQueue;

    private MarshallingFormat format           = MarshallingFormat.JAXB;
    private Set<Class<?>>     extraJaxbClasses = new HashSet<Class<?>>();

    /*
     * Public constructors and setters
     */

    /**
     * REST based constructor
     * @param url
     * @param username
     * @param password
     */
    public KieServicesConfigurationImpl(String url, String username, String password) {
        this( url, username, password, 5000 );
    }

    /**
     * REST based constructor
     * @param url
     * @param username
     * @param password
     * @param timeoutInSecs
     */
    public KieServicesConfigurationImpl(String url, String username, String password, long timeout) {
        this.transport = Transport.REST;

        this.serverUrl = url;
        this.userName = username;
        this.password = password;
        this.timeoutInMillisecs = timeout;
    }

    @Override
    public void dispose() {
        if ( extraJaxbClasses != null ) {
            extraJaxbClasses.clear();
            extraJaxbClasses = null;
        }
        if ( connectionFactory != null ) {
            connectionFactory = null;
        }
        if ( requestQueue != null ) {
            requestQueue = null;
        }
        if ( responseQueue != null ) {
            responseQueue = null;
        }
    }

    // REST ----------------------------------------------------------------------------------------------------------------------

    // JMS ----------------------------------------------------------------------------------------------------------------------

    public KieServicesConfigurationImpl(ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue) {
        this.transport = Transport.JMS;
        this.connectionFactory = connectionFactory;
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        checkValidValues( this.connectionFactory, this.requestQueue, this.responseQueue );
    }

    public KieServicesConfigurationImpl(
            ConnectionFactory connectionFactory, Queue requestQueue,
            Queue responseQueue, String username, String password) {
        this( connectionFactory, requestQueue, responseQueue );
        setAndCheckUserNameAndPassword( username, password );
    }

    public KieServicesConfigurationImpl(InitialContext context, String username, String password) {
        this.transport = Transport.JMS;
        setAndCheckUserNameAndPassword( username, password );
        setRemoteInitialContext( context );
    }

    public void checkValidJmsValues() {
        checkValidValues( connectionFactory, requestQueue, responseQueue );
    }

    private static void checkValidValues(ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue)
            throws IllegalStateException {
        if ( connectionFactory == null ) {
            throw new IllegalStateException( "The connection factory argument may not be null." );
        }
        if ( requestQueue == null ) {
            throw new IllegalStateException( "At least a ksession queue or task queue is required." );
        }
        if ( responseQueue == null ) {
            throw new IllegalStateException( "The response queue argument may not be null." );
        }
    }

    @Override
    public KieServicesConfiguration setRemoteInitialContext(InitialContext context) {
        String prop = CONNECTION_FACTORY_NAME;
        try {
            if ( this.connectionFactory == null ) {
                this.connectionFactory = (ConnectionFactory) context.lookup( prop );
            }
            prop = REQUEST_QUEUE_NAME;
            this.requestQueue = (Queue) context.lookup( prop );
            prop = RESPONSE_QUEUE_NAME;
            this.responseQueue = (Queue) context.lookup( prop );
        } catch ( NamingException ne ) {
            throw new KieServicesException( "Unable to retrieve object for " + prop, ne );
        }
        checkValidValues( connectionFactory, requestQueue, responseQueue );
        return this;
    }

    private KieServicesConfiguration setAndCheckUserNameAndPassword(String username, String password) {
        if ( username == null || username.trim().isEmpty() ) {
            throw new IllegalArgumentException( "The user name may not be empty or null." );
        }
        this.userName = username;
        if ( password == null ) {
            throw new IllegalArgumentException( "The password may not be null." );
        }
        this.password = password;
        return this;
    }

    /**
     * (Package-scoped) Getters
     */
    @Override
    public MarshallingFormat getMarshallingFormat() {
        return format;
    }

    @Override
    public KieServicesConfiguration setMarshallingFormat(MarshallingFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public boolean isJms() {
        return (this.transport == Transport.JMS);
    }

    @Override
    public boolean isRest() {
        return (this.transport == Transport.REST);
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    @Override
    public Queue getRequestQueue() {
        return requestQueue;
    }

    @Override
    public Queue getResponseQueue() {
        return responseQueue;
    }

    @Override
    public boolean addJaxbClasses(Set<Class<?>> extraJaxbClassList) {
        return this.extraJaxbClasses.addAll( extraJaxbClassList );
    }

    @Override
    public KieServicesConfiguration clearJaxbClasses() {
        this.extraJaxbClasses.clear();
        return this;
    }

    @Override
    public Set<Class<?>> getExtraJaxbClasses() {
        return this.extraJaxbClasses;
    }

    @Override
    public Transport getTransport() {
        return this.transport;
    }

    @Override
    public long getTimeout() {
        return timeoutInMillisecs;
    }

    @Override
    public boolean getUseUssl() {
        return useSsl;
    }

    // Setters -------------------------------------------------------------------------------------------------------------------

    @Override
    public KieServicesConfiguration setTimeout(long timeout) {
        this.timeoutInMillisecs = timeout;
        return this;
    }

    @Override
    public KieServicesConfiguration setServerUrl(String url) {
        this.serverUrl = url;
        return this;
    }

    @Override
    public KieServicesConfiguration setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public KieServicesConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public KieServicesConfiguration setExtraJaxbClasses(Set<Class<?>> extraJaxbClasses) {
        this.extraJaxbClasses = extraJaxbClasses;
        return this;
    }

    @Override
    public KieServicesConfiguration setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    @Override
    public KieServicesConfiguration setRequestQueue(Queue requestQueue) {
        this.requestQueue = requestQueue;
        return this;
    }

    @Override
    public KieServicesConfiguration setResponseQueue(Queue responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }

    @Override
    public KieServicesConfiguration setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }

    // Clone ---
    private KieServicesConfigurationImpl(KieServicesConfigurationImpl config) {
        this.connectionFactory = config.connectionFactory;

        this.extraJaxbClasses = config.extraJaxbClasses;
        this.format = config.format;
        this.requestQueue = config.requestQueue;
        this.password = config.password;
        this.responseQueue = config.responseQueue;
        this.serverUrl = config.serverUrl;
        this.timeoutInMillisecs = config.timeoutInMillisecs;
        this.transport = config.transport;
        this.userName = config.userName;
        this.useSsl = config.useSsl;
    }

    @Override
    public KieServicesConfiguration clone() {
        return new KieServicesConfigurationImpl( this );
    }

}

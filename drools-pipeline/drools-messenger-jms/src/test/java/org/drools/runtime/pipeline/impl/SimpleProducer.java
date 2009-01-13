package org.drools.runtime.pipeline.impl;

import java.io.Serializable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

public class SimpleProducer {

    private ConnectionFactory connectionFactory;
    private Destination       destination;
    private MessageProducer   producer;
    private Connection        connection;
    private Session           session;

    public SimpleProducer(Properties properties,
                          String destinationName) {
        try {
            InitialContext jndiContext = new InitialContext( properties );
            this.connectionFactory = (ConnectionFactory) jndiContext.lookup( "ConnectionFactory" );
            this.destination = (Destination) jndiContext.lookup( destinationName );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to instantiate JmsFeeder",
                                        e );
        }
    }

    public void start() {
        try {
            this.connection = this.connectionFactory.createConnection();
            this.session = this.connection.createSession( false,
                                                          Session.AUTO_ACKNOWLEDGE );
            this.producer = this.session.createProducer( destination );
            this.connection.start();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public void setText(String text) {
        ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
        try {
            textMessage.setText( text );
            this.producer.send( textMessage );
        } catch ( JMSException e ) {
            throw new RuntimeException( e );
        }
    }

    public void sendObject(Serializable object) {
        ActiveMQObjectMessage objectMessage = new ActiveMQObjectMessage();
        try {
            objectMessage.setObject( object );
            this.producer.send( objectMessage );
        } catch ( JMSException e ) {
            throw new RuntimeException( e );
        }
    }

    public void stop() {
        try {
            //            this.run = false;
            // this will interrupt the receive()
            this.producer.close();
            this.connection.stop();
        } catch ( JMSException e ) {
            throw new RuntimeException( e );
        }
    }

    public void simpleProducer2() {
        Context jndiContext = null;
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageProducer producer = null;
        String destinationName = "dynamicQueues/FOO.BAR";
        final int numMsgs = 50;

        System.out.println( "Destination name is " + destinationName );
        /*
         * Create a JNDI API InitialContext object
         */
        try {
            Properties props = new Properties();
            props.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                               "org.apache.activemq.jndi.ActiveMQInitialContextFactory" );
            props.setProperty( Context.PROVIDER_URL,
                               "vm://localhost:61616" );
            jndiContext = new InitialContext( props );
        } catch ( NamingException e ) {
            e.printStackTrace();
            System.out.println( "Could not create JNDI API context: " + e.toString() );
            System.exit( 1 );
        }

        /*
         * Look up connection factory and destination.
         */
        try {
            connectionFactory = (ConnectionFactory) jndiContext.lookup( "ConnectionFactory" );
            destination = (Destination) jndiContext.lookup( destinationName );
        } catch ( NamingException e ) {
            e.printStackTrace();
            //LOG.info("JNDI API lookup failed: " + e);
            System.exit( 1 );
        }

        /*
         * Create connection. Create session from connection; false means
         * session is not transacted. Create sender and text message. Send
         * messages, varying text slightly. Send end-of-messages message.
         * Finally, close connection.
         */
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession( false,
                                                Session.AUTO_ACKNOWLEDGE );
            //session.createTopic( "" )
            producer = session.createProducer( destination );
            TextMessage message = session.createTextMessage();
            for ( int i = 0; i < numMsgs; i++ ) {
                message.setText( "This is message " + (i + 1) );
                System.out.println( "Sending message: " + message.getText() );
                producer.send( message );
            }

            /*
             * Send a non-text control message indicating end of messages.
             */
            producer.send( session.createMessage() );
        } catch ( JMSException e ) {
            e.printStackTrace();
            System.out.println( "Exception occurred: " + e );
        } finally {
            if ( connection != null ) {
                try {
                    connection.close();
                } catch ( JMSException e ) {
                }
            }
        }
    }
}

package org.drools.runtime.pipeline.impl;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.broker.BrokerService;

public class SimpleConsumer {

    public SimpleConsumer() {
        super();
    }

    public static void main(String[] args) throws Exception {
        BrokerService broker = new BrokerService();
        // configure the broker
        broker.setBrokerName("consumer");
        broker.addConnector("tcp://localhost:61616");
        broker.start();
        
        Context jndiContext = null;
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageConsumer consumer = null;
        TextMessage message = null;
        String destinationName = "dynamicQueues/FOO.BAR";
        final int NUM_MSGS = 50;

        try {
            Properties props = new Properties();
            props.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                               "org.apache.activemq.jndi.ActiveMQInitialContextFactory" );
            props.setProperty( Context.PROVIDER_URL,
                               "tcp://localhost:61616" );
            jndiContext = new InitialContext( props );
        }

        catch ( NamingException e ) {
            System.out.println( "Could not create JNDI API context: " + e.toString() );
            System.exit( 1 );
        }

        try {
            connectionFactory = (ConnectionFactory) jndiContext.lookup( "ConnectionFactory" );
            destination = (Destination) jndiContext.lookup( destinationName );
        }

        catch ( NamingException e ) {
            e.printStackTrace();
            System.out.println( "JNDI API lookup failed: " + e );
            System.exit( 1 );
        }

        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession( false,
                                                Session.AUTO_ACKNOWLEDGE );

            //producer = session.createProducer(destination);
            consumer = session.createConsumer( destination );
            connection.start();
            //message = session.createTextMessage();

            for ( int i = 0; i < NUM_MSGS; i++ ) {
                Message msg = consumer.receive();
                System.out.println( "received : " + msg );
                if ( msg instanceof TextMessage ) {
                    message = (TextMessage) msg;
                    System.out.println( message.getText() );
                }
            }
        } catch ( JMSException e ) {
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

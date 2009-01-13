package org.drools.runtime.pipeline.impl;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.pipeline.Service;

public class JmsMessenger extends BaseService
    implements
    Service {
    private ConnectionFactory    connectionFactory;
    private Destination          destination;
    private MessageConsumer      consumer;
    private Connection           connection;
    private Session              session;

    private ResultHandlerFactory resultHandlerFactory;
    private Pipeline             pipeline;

    private Thread               thread;

    private JmsMessengerRunner   jmsFeederRunner;

    public JmsMessenger(Pipeline pipeline,
                        Properties properties,
                        String destinationName,
                        ResultHandlerFactory resultHandlerFactory) {
        super();
        this.pipeline = pipeline;
        this.resultHandlerFactory = resultHandlerFactory;

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
            this.consumer = this.session.createConsumer( destination );

            this.connection.start();
        } catch ( Exception e ) {
            handleException( this,
                             null,
                             e );
        }
        this.jmsFeederRunner = new JmsMessengerRunner( this,
                                                       this.consumer,
                                                       this.pipeline,
                                                       this.resultHandlerFactory );
        this.jmsFeederRunner.setRun( true );
        this.thread = new Thread( this.jmsFeederRunner );
        this.thread.start();
    }

    public void stop() {
        try {
            this.jmsFeederRunner.setRun( false );
            // this will interrupt the receive()
            this.consumer.close();
            this.connection.stop();
        } catch ( JMSException e ) {
            handleException( this,
                             null,
                             e );
        }
    }

    //    public void run() {
    //        while ( this.run ) {
    //            Message msg = null;
    //            try {
    //                msg = this.consumer.receive();
    //                System.out.println( "msg received : " + msg );
    //                //                emit( msg,
    //                //                      new EntryPointPipelineContext( this.entryPoint ) );
    //            } catch ( JMSException e ) {
    //                handleException( this,
    //                                 msg,
    //                                 e );
    //            }
    //        }
    //    }

    public static class JmsMessengerRunner
        implements
        Runnable {
        private JmsMessenger         feeder;
        private MessageConsumer      consumer;
        private Pipeline             pipeline;
        private ResultHandlerFactory resultHandlerFactory;
        private volatile boolean     run;

        public JmsMessengerRunner(JmsMessenger feeder,
                                  MessageConsumer consumer,
                                  Pipeline pipeline,
                                  ResultHandlerFactory resultHandlerFactory) {
            super();
            this.feeder = feeder;
            this.consumer = consumer;
            this.pipeline = pipeline;
            this.resultHandlerFactory = resultHandlerFactory;
        }

        public void run() {
            while ( this.run ) {
                Message msg = null;
                try {
                    msg = this.consumer.receive();
                    if ( this.resultHandlerFactory != null ) {
                        pipeline.insert( msg,
                                         this.resultHandlerFactory.newResultHandler() );
                    } else {
                        pipeline.insert( msg,
                                         null );
                    }
                    System.out.println( "msg received : " + msg );
                } catch ( JMSException e ) {
                    this.feeder.handleException( this.feeder,
                                                 msg,
                                                 e );
                }
            }
        }

        public void setRun(boolean run) {
            this.run = run;
        }

    }
}

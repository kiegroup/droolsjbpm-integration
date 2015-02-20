package org.kie.server.jms;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.kie.server.api.jms.JMSConstants.*;

@MessageDriven(name = "KieServerMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "queue/KIE.SERVER.REQUEST"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/KIE.SERVER.REQUEST"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class KieServerMDB
        implements MessageListener {

    private final static Logger logger = LoggerFactory.getLogger( KieServerMDB.class );

    // Constants / properties
    private              String RESPONSE_QUEUE_NAME          = null;
    private static final String RESPONSE_QUEUE_NAME_PROPERTY = "kie.server.jms.queues.response";
    private static final String DEFAULT_RESPONSE_QUEUE_NAME  = "queue/KIE.SERVER.RESPONSE";

    private static final String ID_NECESSARY = "This id is needed to be able to match a request to a response message.";

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory factory;

    // Initialized in @PostConstruct
    private Session    session;
    private Connection connection;

    //    @Resource(lookup = "java:app/RetryTrackerSingleton")
    //    private RetryTrackerSingleton retryTracker;

    private KieServerImpl kieServer;

    private Map<MarshallingFormat, Marshaller> marshallers;

    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty( RESPONSE_QUEUE_NAME_PROPERTY, DEFAULT_RESPONSE_QUEUE_NAME );

        try {
            connection = factory.createConnection();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            connection.start();
        } catch ( JMSException jmse ) {
            // Unable to create connection/session, so no need to try send the message (4.) either
            String errMsg = "Unable to open new session to send response messages";
            logger.error( errMsg, jmse );
            throw new JMSRuntimeException( errMsg, jmse );
        }

        kieServer = KieServerLocator.getInstance();

        marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>(  );
        // the commands classloader -- not sure if there is a better way to get a reference to it
        ClassLoader classLoader = CommandScript.class.getClassLoader();
        marshallers.put( MarshallingFormat.XSTREAM, MarshallerFactory.getMarshaller( MarshallingFormat.XSTREAM, classLoader ) );
        marshallers.put( MarshallingFormat.JAXB, MarshallerFactory.getMarshaller( MarshallingFormat.JAXB, classLoader ) );
        marshallers.put( MarshallingFormat.JSON, MarshallerFactory.getMarshaller( MarshallingFormat.JSON, classLoader ) );
    }

    @PreDestroy
    public void cleanup() {
        try {
            if ( connection != null ) {
                connection.close();
                connection = null;
            }
            if ( session != null ) {
                session.close();
                session = null;
            }
        } catch ( JMSException jmse ) {
            String errMsg = "Unable to close " + (connection == null ? "session" : "connection");
            logger.error( errMsg, jmse );
            throw new JMSRuntimeException( errMsg, jmse );
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
//        String msgId = null;
//        boolean redelivered = false;
//        try {
//            msgId = message.getJMSMessageID();
//            redelivered = message.getJMSRedelivered();
//        } catch ( JMSException jmse ) {
//            String errMsg = "Unable to retrieve JMS " + (msgId == null ? "redelivered flag" : "message id")
//                            + " from JMS message. Message will not be returned to queue.";
//            logger.warn( errMsg, jmse );
//        }
//
//        if ( redelivered ) {
//            if ( retryTracker.maxRetriesReached( msgId ) ) {
//                logger.warn( "Maximum number of retries (" + retryTracker.getMaximumLimitRetries() + ") reached for message " + msgId );
//                logger.warn( "Acknowledging message but NOT processing it." );
//                return;
//            } else {
//                logger.warn( "Retry number " + retryTracker.incrementRetries( msgId ) + " of message " + msgId );
//            }
//        }
//
        // 0. Get msg correlation id (for response)
        String msgCorrId = null;
        try {
            msgCorrId = message.getJMSCorrelationID();
        } catch ( JMSException jmse ) {
            String errMsg = "Unable to retrieve JMS correlation id from message! " + ID_NECESSARY;
            throw new JMSRuntimeException( errMsg, jmse );
        }

        // 1. get marshalling info
        MarshallingFormat format = null;
        try {
            if ( !message.propertyExists( SERIALIZATION_FORMAT_PROPERTY_NAME ) ) {
                format = MarshallingFormat.JAXB;
            } else {
                int intFormat = message.getIntProperty( SERIALIZATION_FORMAT_PROPERTY_NAME );
                format = MarshallingFormat.fromId( intFormat );
                if( format == null ) {
                    String errMsg = "Unsupported marshalling format '"+ intFormat +"' from message " + msgCorrId + ".";
                    throw new JMSRuntimeException( errMsg );
                }
            }
        } catch ( JMSException jmse ) {
            String errMsg = "Unable to retrieve property '"+ SERIALIZATION_FORMAT_PROPERTY_NAME +"' from message " + msgCorrId + ".";
            throw new JMSRuntimeException( errMsg, jmse );
        }

        // 2. get marshaller
        Marshaller marshaller = marshallers.get( format );

        // 3. deserialize request
        CommandScript script = unmarshallRequest( message, msgCorrId, marshaller, format );

        // 4. process request
        ServiceResponsesList response = kieServer.executeScript( script );

        // 5. serialize response
        Message msg = marshallResponse( session, msgCorrId, format, marshaller, response );

        // 6. send response
        sendResponse( msgCorrId, format, msg );

//        if ( redelivered ) {
//            retryTracker.clearRetries( msgId );
//        }

    }

    private static CommandScript unmarshallRequest(Message message, String msgId, Marshaller serializationProvider, MarshallingFormat format) {
        CommandScript cmdMsg = null;
        try {
            String msgStrContent = ((TextMessage) message).getText();
            cmdMsg = serializationProvider.unmarshall( msgStrContent, CommandScript.class );
        } catch (JMSException jmse) {
            String errMsg = "Unable to read information from message " + msgId + ".";
            throw new JMSRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to unmarshall request to " + CommandScript.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new JMSRuntimeException(errMsg, e);
        }
        return cmdMsg;
    }

    private static Message marshallResponse(Session session, String msgId, MarshallingFormat format, Marshaller marshaller, ServiceResponsesList response ) {
        TextMessage textMsg = null;
        try {
            String msgStr = marshaller.marshall( response );
            textMsg = session.createTextMessage(msgStr);
            textMsg.setIntProperty( SERIALIZATION_FORMAT_PROPERTY_NAME, format.getId());
        } catch (JMSException jmse) {
            String errMsg = "Unable to create response message or write to it [msg id: " + msgId + "].";
            throw new JMSRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to serialize " + response.getClass().getSimpleName() + " to a String.";
            throw new JMSRuntimeException(errMsg, e);
        }
        return textMsg;
    }

    private void sendResponse(String msgCorrId, MarshallingFormat format, Message msg) {
        // set correlation id in response messgae
        try {
            msg.setJMSCorrelationID(msgCorrId);
        } catch (JMSException jmse) {
            // Without correlation id, receiver won't know what the response relates to
            String errMsg = "Unable to set correlation id of response to msg id " + msgCorrId;
            logger.error(errMsg, jmse);
            return;
        }

        // send response message
        MessageProducer producer = null;
        try {
            Queue responseQueue = (Queue) (new InitialContext()).lookup(RESPONSE_QUEUE_NAME);
            producer = session.createProducer(responseQueue);
            producer.send(msg);
            session.commit();
        } catch (NamingException ne) {
            String errMsg = "Unable to lookup response queue " + RESPONSE_QUEUE_NAME + " to send msg " + msgCorrId
                            + " (Is " + RESPONSE_QUEUE_NAME_PROPERTY + " incorrect?).";
            logger.error(errMsg, ne);
        } catch (JMSException jmse) {
            String errMsg = "Unable to send msg " + msgCorrId + " to " + RESPONSE_QUEUE_NAME;
            logger.error(errMsg, jmse);
        } finally {
            if( producer != null ) {
                try {
                    producer.close();
                } catch( JMSException e ) {
                    logger.debug("Closing the producer resulted in an exception: "  + e.getMessage(), e);
                }
            }
        }
    }

}

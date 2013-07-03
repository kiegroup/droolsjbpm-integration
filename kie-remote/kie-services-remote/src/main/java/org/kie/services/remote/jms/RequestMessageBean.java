package org.kie.services.remote.jms;

import static org.kie.services.remote.util.CommandsRequestUtil.processJaxbCommandsRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.remote.KieRemoteServicesInternalError;
import org.kie.services.remote.cdi.ProcessRequestBean;

/**
 * This class is the link between incoming request (whether via REST or JMS or .. whatever)
 * and the bean that processes the requests, the {@link ProcessRequestBean}.
 * </p>
 * Responses to requests are <b>not</b> placed on the reply-to queue, but on the answer queue.
 * </p> Because there are multiple queues to which an instance of this class could listen to, the (JMS queue) configuration is
 * done in the ejb-jar.xml file, which allows us to configure instances of one class to listen to more than one queue.
 */
public class RequestMessageBean implements MessageListener {

    @Inject
    private Logger logger;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private ProcessRequestBean processRequestBean;

    @Resource
    // TODO: set tx to rollback in some cases?
    private MessageDrivenContext msgContext;
    
    private String RESPONSE_QUEUE_NAME = null;
    private static String RESPONSE_QUEUE_NAME_PROPERTY = "kie.services.jms.queues.response";

    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty(RESPONSE_QUEUE_NAME_PROPERTY, "queue/KIE.RESPONSE.ALL");
    }

    public void onMessage(Message message) {
        boolean failure = false;
        String msgCorrId = null;
        try {
            msgCorrId = message.getJMSCorrelationID();
        } catch (JMSException jmse) {
            logger.log(Level.WARNING, 
                    "Unable to retrieve JMS correlation id from message! This id is needed to be able to match a request to a response message.",
                    jmse);
        }
        if( msgCorrId == null ) { 
            logger.log(Level.WARNING, 
                    "JMS correlation id is empty! This id is needed to be able to match a request to a response message.");
        }
        
        // 1. get request
        int[] serializationTypeHolder = new int[1];
        JaxbCommandsRequest cmdsRequest = deserializeRequest(message, msgCorrId, serializationTypeHolder);

        // 2. process request
        JaxbCommandsResponse jaxbResponse;
        if (cmdsRequest != null) {
            jaxbResponse = processJaxbCommandsRequest(cmdsRequest, processRequestBean);
        } else {
            // Failure reasons have been logged in deserializeRequest(). 
            logger.log(Level.SEVERE, "Stopping processing of request message due to errors: see above.");
            return;
        }

        // 3. create session
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException jmse) {
            logger.log(Level.SEVERE, "Unable to open new session to send response message to message " + msgCorrId, jmse);
            failure = true;
        } finally {
            if (failure) {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        session.close();
                        session = null;
                    } catch (JMSException jmse) {
                        logger.log(Level.INFO, "Unable to close connection or session after failing to create connection or session.", jmse);
                    }
                }
                // Unable to create connection/session, so no need to try send the message either
                return;
            }
        }

        // 4. create response message
        Message msg = serializeResponse(session, msgCorrId, serializationTypeHolder[0], jaxbResponse);
        try {
            msg.setJMSCorrelationID(msgCorrId);
        } catch (JMSException jmse) {
            logger.log( Level.WARNING, "Unable to set correlation id of response to msg id " + msgCorrId, jmse );
        }
        
        // 5. send response message
        if (!failure) {
            try {
                InitialContext context = new InitialContext();
                Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_NAME);
                MessageProducer producer = session.createProducer(responseQueue);
                producer.send(msg);
            } catch (NamingException ne) {
                logger.log(Level.SEVERE, 
                        "Unable to lookup response queue (" + RESPONSE_QUEUE_NAME + ") to send msg " + msgCorrId 
                        + " (Is " + RESPONSE_QUEUE_NAME_PROPERTY + " incorrect?).",
                        ne );
            } catch (JMSException jmse) {
                logger.log(Level.SEVERE, "Unable to send msg " + msgCorrId + " to " + RESPONSE_QUEUE_NAME, jmse );
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        session.close();
                        session = null;
                    } catch (JMSException jmse) {
                        logger.log(Level.INFO, "Unable to close connection or session.", jmse);
                    }
                }
            }
        }
    }

    private JaxbCommandsRequest deserializeRequest(Message message, String msgId, int[] serializationTypeHolder) {
        JaxbCommandsRequest cmdMsg = null;
        try {
            serializationTypeHolder[0] = message.getIntProperty("serialization");

            if (serializationTypeHolder[0] == 1) {
                String msgStrContent = ((BytesMessage) message).readUTF();
                cmdMsg = (JaxbCommandsRequest) JaxbSerializationProvider.convertStringToJaxbObject(msgStrContent);
            } else {
                throw new KieRemoteServicesInternalError("Unknown serialization type when deserializing message " + msgId + ":" + serializationTypeHolder[0]);
            }
        } catch (JMSException jmse) {
            logger.log(Level.SEVERE, "Unable to read information from message " + msgId + ".", jmse);
        } catch( JAXBException jaxbe) { 
            throw new KieRemoteServicesInternalError("Unable to convert String to " + JaxbCommandsRequest.class.getSimpleName() + " [msg id: " + msgId + "].", jaxbe);
        }
        return cmdMsg;
    }

    private Message serializeResponse(Session session, String msgId, int serializationType, JaxbCommandsResponse jaxbResponse) {
        BytesMessage byteMsg = null;
        try {
            byteMsg = session.createBytesMessage();
            byteMsg.setIntProperty("serialization", serializationType);

            if (serializationType == 1) {
                String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbResponse);
                byteMsg.writeUTF(xmlStr);
            } else {
                throw new KieRemoteServicesInternalError("Unknown serialization type when deserializing message " + msgId + ":" + serializationType);
            }
        } catch (JMSException jmse) {
            logger.log(Level.SEVERE, "Unable to create response message or write to it [msg id: " + msgId + "].", jmse);
        } catch( JAXBException jaxbe) { 
            throw new KieRemoteServicesInternalError("Unable to serialize " + jaxbResponse.getClass().getSimpleName() + " to a String.", jaxbe);
        }
        return byteMsg;
    }

}

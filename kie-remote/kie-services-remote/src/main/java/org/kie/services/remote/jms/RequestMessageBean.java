package org.kie.services.remote.jms;

import static org.kie.services.remote.util.CommandsRequestUtil.*;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.remote.UnfinishedError;
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

    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty("kie.services.jms.queues.response", "queue/KIE.RESPONSE.ALL");
    }

    public void onMessage(Message message) {
        // 1. get request
        int[] serializationTypeHolder = new int[1];
        JaxbCommandsRequest cmdsRequest = deserializeRequest(message, serializationTypeHolder);

        // 2. process request
        JaxbCommandsResponse jaxbResponse;
        if (cmdsRequest != null) {
            jaxbResponse = processJaxbCommandsRequest(cmdsRequest, processRequestBean);
        } else {
            jaxbResponse = null;
            // TODO
        }

        // 3. create session
        boolean failure = false;
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (Exception e) {
            // TODO: log exception
            failure = true;
        } finally {
            if (failure) {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        session.close();
                        session = null;
                    } catch (Exception e) {
                        // TODO: log exception
                    }
                }
            }
        }

        // 4. create response message
        Message msg = null;
        if (!failure) {
            msg = serializeResponse(session, serializationTypeHolder[0], jaxbResponse);
        }

        
        // 5. send response message
        if (!failure) {
            try {
                InitialContext context = new InitialContext();
                Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_NAME);
                MessageProducer producer = session.createProducer(responseQueue);
                producer.send(msg);
            } catch (Exception e) {
                // TODO: log exception
                String errorMsg = "Unable to send msg to " + RESPONSE_QUEUE_NAME;
                logger.severe(errorMsg);
                throw new RuntimeException(errorMsg, e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        session.close();
                        session = null;
                    } catch (Exception e) {
                        // TODO: log exception
                    }
                }
            }
        }
    }

    private JaxbCommandsRequest deserializeRequest(Message message, int[] serializationTypeHolder) {
        JaxbCommandsRequest cmdMsg = null;
        try {
            serializationTypeHolder[0] = message.getIntProperty("serialization");

            if (serializationTypeHolder[0] == 1) {
                String msgStrContent = ((BytesMessage) message).readUTF();
                cmdMsg = (JaxbCommandsRequest) JaxbSerializationProvider.convertStringToJaxbObject(msgStrContent);
            } else {
                // TODO: log exception
                throw new UnfinishedError("Unknown serialization type : " + serializationTypeHolder[0]);
            }
        } catch (Exception e) {
            // TODO: log exception
            throw new RuntimeException("Unable to read " + JaxbCommandsRequest.class.getSimpleName() + " from "
                    + BytesMessage.class.getSimpleName());
        }
        return cmdMsg;
    }

    private Message serializeResponse(Session session, int serializationtype, JaxbCommandsResponse jaxbResponse) {
        BytesMessage byteMsg = null;
        try {
            byteMsg = session.createBytesMessage();
            byteMsg.setIntProperty("serialization", serializationtype);

            if (serializationtype == 1) {
                String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbResponse);
                byteMsg.writeUTF(xmlStr);
            } else {
                // TODO: log exception
                throw new UnfinishedError("Unknown serialization type : " + serializationtype);
            }
        } catch (Exception e) {
            String msg = "Unable to read " + JaxbCommandsRequest.class.getSimpleName() + " from "
                    + BytesMessage.class.getSimpleName();
            logger.severe(msg);
            throw new RuntimeException(msg, e);

        }
        return byteMsg;
    }

}

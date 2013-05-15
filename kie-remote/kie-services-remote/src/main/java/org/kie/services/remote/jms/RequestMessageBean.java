package org.kie.services.remote.jms;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.kie.services.remote.UnfinishedError;
import org.kie.services.remote.ejb.ProcessRequestBean;

/**
 * This class is the link between incoming request (whether via REST or JMS or .. whatever)
 * and the Stateless EJB that processes the requests, the {@link ProcessRequestBean}.
 * </p>
 * Responses to requests are <b>not</b> placed on the reply-to queue, but on the corresponding answer queue.
 * For example:
 * <ul>
 * <li>If the request arrived on the JBPM.TASK.DOMAIN.MYCOM, then the answer would be sent to the JBPM.TASK</li>
 * </p> 
 * Because there are * multiple queues to which an instance of this class could listen to, the (JMS queue) configuration is done in the ejb-jar.xml
 * file, which allows us to configure instances of one class to listen to more than one queue.
 */
public class RequestMessageBean implements MessageListener {

    @Inject
    private Logger logger;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private ProcessRequestBean processRequestBean;

    @Inject
//    private MessageSerializationProviderFactory serializationProviderFactory;

    public void onMessage(Message message) {
        logger.info("Message received: " + message.toString());
        Connection connection = null;
        Session session = null;
        try {
//            int serializationType = message.getIntProperty("serializationType");
//            MessageSerializationProvider serializationProvider = serializationProviderFactory
//                    .getMessageSerializationProvider(serializationType);
//            ServiceMessage request = serializationProvider.convertJmsMessageToServiceMessage(message);
//            ServiceMessage response = new ServiceMessage(request.getDomainName());
//            for (OperationMessage operation : request.getOperations()) {
//                OperationMessage operResponse = processRequestBean.doOperation(request, operation);
//                response.addOperation(operResponse);
//            }
//
//            Message replyMessage = serializationProvider.convertServiceMessageToJmsMessage(response, session);
//
//            connection = connectionFactory.createConnection();
//            connection.start();
//            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            MessageProducer producer = session.createProducer(message.getJMSReplyTo());
//            producer.send(replyMessage);
        } catch (Exception e) {
            throw new UnfinishedError("Not sure what to do with error handling", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    session.close();
                } catch (Exception e) {
                }
            }
        }
    }

}

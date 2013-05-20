package org.kie.services.remote.jms;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.remote.UnfinishedError;
import org.kie.services.remote.cdi.ProcessRequestBean;

/**
 * This class is the link between incoming request (whether via REST or JMS or .. whatever)
 * and the Stateless EJB that processes the requests, the {@link ProcessRequestBean}.
 * </p>
 * Responses to requests are <b>not</b> placed on the reply-to queue, but on the corresponding answer queue.
 * For example:
 * <ul>
 * <li>If the request arrived on the JBPM.TASK.DOMAIN.MYCOM, then the answer would be sent to the JBPM.TASK</li>
 * </p> Because there are * multiple queues to which an instance of this class could listen to, the (JMS queue) configuration is
 * done in the ejb-jar.xml file, which allows us to configure instances of one class to listen to more than one queue.
 */
public class RequestMessageBean implements MessageListener {

    @Inject
    private Logger logger;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private ProcessRequestBean processRequestBean;

    public void onMessage(Message message) {
        logger.info("Message received: " + message.toString());
        JaxbCommandsRequest cmdMsg = null;
        try {
            int serializationtype = message.getIntProperty("serialization");

            if( serializationtype == 0 ) { 
                String msgStrContent = ((BytesMessage) message).readUTF();
                cmdMsg = (JaxbCommandsRequest) JaxbSerializationProvider.convertStringToJaxbObject(msgStrContent);
            } else { 
                throw new UnfinishedError("Unknown serialization type : " + serializationtype);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to read " + JaxbCommandsRequest.class.getSimpleName() + " from "
                    + BytesMessage.class.getSimpleName());
        }

        String deploymentId = cmdMsg.getDeploymentId();
        Long processInstanceId = cmdMsg.getProcessInstanceId();
        for (Command<?> cmd : cmdMsg.getCommands()) {
            try {
                Object cmdResult;
                if (cmd instanceof TaskCommand<?>) {
                    cmdResult = processRequestBean.doTaskOperation(cmd);
                } else {
                    cmdResult = processRequestBean.doKieSessionOperation(cmd, deploymentId, processInstanceId);
                }
            } catch (Exception e) {
                // TODO: fill jms message
            }
        }

        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer(message.getJMSReplyTo());
            producer.send(null);
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

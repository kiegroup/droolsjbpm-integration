package org.kie.services.remote.jms;

import static org.kie.services.client.serialization.SerializationConstants.EXTRA_JAXB_CLASSES_PROPERTY_NAME;
import static org.kie.services.client.serialization.SerializationConstants.SERIALIZATION_TYPE_PROPERTY_NAME;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

import org.drools.core.command.SingleSessionCommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.SerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.remote.cdi.RuntimeManagerManagerBean;
import org.kie.services.remote.cdi.TransactionalExecutor;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.kie.services.remote.exception.KieRemoteServicesRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responses to requests are <b>not</b> placed on the reply-to queue, but on the answer queue.
 * </p> Because there are multiple queues to which an instance of this class could listen to, the (JMS queue) configuration is
 * done in the ejb-jar.xml file, which allows us to configure instances of one class to listen to more than one queue.
 */
public class RequestMessageBean implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessageBean.class);

    // JMS resources
    
    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Resource
    private MessageDrivenContext context;
    
    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory factory;

    // Initialized in @PostConstruct
    private Session session;
    private Connection connection;
   
    @Inject
    private RetryTrackerSingleton retryTracker;
    
    // KIE resources
    
    @Inject
    private RuntimeManagerManagerBean runtimeMgrMgr;
    
    @Inject
    private TaskService taskService;

    @Inject
    private TransactionalExecutor executor;
    
    // Constants / properties
    
    private String RESPONSE_QUEUE_NAME = null;
    private static String RESPONSE_QUEUE_NAME_PROPERTY = "kie.services.jms.queues.response";
    
    private static final String ID_NECESSARY = "This id is needed to be able to match a request to a response message.";
    
    
    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty(RESPONSE_QUEUE_NAME_PROPERTY, "queue/KIE.RESPONSE.ALL");
        
        try {
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
        } catch (JMSException jmse) {
            // Unable to create connection/session, so no need to try send the message (4.) either
            String errMsg = "Unable to open new session to send response messages";
            logger.error(errMsg, jmse);
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }
    }
    
    @PreDestroy
    public void cleanup() { 
       try { 
           if( connection != null ) { 
               connection.close();
               connection = null;
           }
           if( session != null ) { 
               session.close();
               session = null;
           }
       } catch(JMSException jmse) { 
           String errMsg = "Unable to close " + (connection == null ? "session" : "connection");
           logger.error(errMsg, jmse);
           throw new KieRemoteServicesRuntimeException(errMsg, jmse);
       }
    }
    
    // See EJB 3.1 fr, 5.4.12 and 13.3.3: BMT for which the (last) ut.commit() confirms message reception
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
        
        String msgId = null;
        Boolean redelivered = null;
        try {
            msgId = message.getJMSMessageID();
            redelivered = message.getJMSRedelivered();
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve JMS " + (msgId == null ? "redelivered flag" : "message id") 
                    + " from JMS message. Failing and NOT returning message to queue.";
            logger.warn(errMsg, jmse);
        }

        if( redelivered ) { 
            if( retryTracker.maxRetriesReached(msgId) ) { 
                logger.warn("Maximum number of retries (" + retryTracker.getMaximumLimitRetries() + ") reached for message " + msgId );
                logger.warn("Acknowledging message but NOT processing it.");
                return;
            } else { 
                logger.warn("Retry number " + retryTracker.incrementRetries(msgId) + " of message " + msgId );
            }
        }
        
        // 0. Get msg correlation id (for response)
        String msgCorrId = null;
        JaxbCommandsResponse jaxbResponse = null;
        try {
            msgCorrId = message.getJMSCorrelationID();
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve JMS correlation id from message! " + ID_NECESSARY;
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        } 

        // 0. get serialization info
        int serializationType = -1;
        try { 
            if( ! message.propertyExists(SERIALIZATION_TYPE_PROPERTY_NAME) ) {
                serializationType = JaxbSerializationProvider.JMS_SERIALIZATION_TYPE;
            } else { 
                serializationType = message.getIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to get properties from message " + msgCorrId + ".";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }

        SerializationProvider serializationProvider;
        switch(serializationType ) { 
        case JaxbSerializationProvider.JMS_SERIALIZATION_TYPE:
            serializationProvider = getJaxbSerializationProvider(message);
            break;
        default:
            throw new KieRemoteServicesInternalError("Unknown serialization type: " + serializationType);
        }     

        // 1. deserialize request
        JaxbCommandsRequest cmdsRequest = deserializeRequest(message, msgCorrId, serializationProvider, serializationType);

        // 2. process request
        jaxbResponse = processJaxbCommandsRequest(cmdsRequest);
        
        // 3. serialize response 
        Message msg = serializeResponse(session, msgCorrId, serializationType, serializationProvider, jaxbResponse);

        // 4. send response
        sendResponse(msgCorrId, serializationType, msg);
        
        if( redelivered ) { 
            retryTracker.clearRetries(msgId);
        }
    }

    private void sendResponse(String msgCorrId, int serializationType, Message msg) { 
        // 3b. set correlation id in response messgae
        try {
            msg.setJMSCorrelationID(msgCorrId);
        } catch (JMSException jmse) {
            // Without correlation id, receiver won't know what the response relates to
            String errMsg = "Unable to set correlation id of response to msg id " + msgCorrId;
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }

        // 3c. send response message
        try {
            Queue responseQueue = (Queue) (new InitialContext()).lookup(RESPONSE_QUEUE_NAME);
            MessageProducer producer = session.createProducer(responseQueue);
            producer.send(msg);
        } catch (NamingException ne) {
            String errMsg = "Unable to lookup response queue " + RESPONSE_QUEUE_NAME + " to send msg " + msgCorrId 
                    + " (Is " + RESPONSE_QUEUE_NAME_PROPERTY + " incorrect?).";
            throw new KieRemoteServicesRuntimeException(errMsg, ne);       
        } catch (JMSException jmse) {
            String errMsg = "Unable to send msg " + msgCorrId + " to " + RESPONSE_QUEUE_NAME;
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }
    }
    
    // De/Serialization helper methods -------------------------------------------------------------------------------------------
    
    private static JaxbCommandsRequest deserializeRequest(Message message, String msgId, SerializationProvider serializationProvider, int serializationType) {
        
        JaxbCommandsRequest cmdMsg = null;
        try {
            String msgStrContent = null;
            
            switch(serializationType) {
            case JaxbSerializationProvider.JMS_SERIALIZATION_TYPE:
                msgStrContent = ((BytesMessage) message).readUTF();
                cmdMsg = (JaxbCommandsRequest) serializationProvider.deserialize(msgStrContent);
                break;
            default:
                throw new KieRemoteServicesRuntimeException("Unknown serialization type when deserializing message " + msgId + ":" + serializationType);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to read information from message " + msgId + ".";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        } catch( Exception e ) { 
            String errMsg = "Unable to serialize String to " + JaxbCommandsRequest.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new KieRemoteServicesInternalError(errMsg, e);
        } 
        return cmdMsg;
    }

    private static SerializationProvider getJaxbSerializationProvider(Message message) { 
        SerializationProvider serializationProvider;
        try { 
            if( message.propertyExists(EXTRA_JAXB_CLASSES_PROPERTY_NAME) ) {
                String extraClassesString = message.getStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME);
                Set<Class<?>> extraClassesList = JaxbSerializationProvider.commaSeperatedStringToClassSet(extraClassesString);
                serializationProvider = new JaxbSerializationProvider(extraClassesList);
            } else { 
                serializationProvider = new JaxbSerializationProvider();
            }
        } catch (JMSException jmse) {
            throw new KieRemoteServicesInternalError("Unable to check or read JMS message for property.", jmse);
        } catch (SerializationException se) { 
            throw new KieRemoteServicesRuntimeException("Unable to load classes needed for JAXB deserialization.", se);
        }
        return serializationProvider;
    }

    private static Message serializeResponse(Session session, String msgId, int serializationType, 
            SerializationProvider serializationProvider, JaxbCommandsResponse jaxbResponse) {
        BytesMessage byteMsg = null;
        try {
            byteMsg = session.createBytesMessage();
            byteMsg.setIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME, serializationType);
    
            String msgStr;
            switch(serializationType) { 
            case JaxbSerializationProvider.JMS_SERIALIZATION_TYPE:
                msgStr = (String) serializationProvider.serialize(jaxbResponse);
                Set<Class<?>> extraJaxbClasses =  ((JaxbSerializationProvider) serializationProvider).getExtraJaxbClasses();
                if( ! extraJaxbClasses.isEmpty() ) { 
                    String propValue;
                    try {
                        propValue = JaxbSerializationProvider.classSetToCommaSeperatedString(extraJaxbClasses);
                    } catch( SerializationException se ) { 
                        throw new KieRemoteServicesRuntimeException("Unable to get class names for extra JAXB classes.", se );
                    }
                    byteMsg.setStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME, propValue);
                }
                break;
            default:
                throw new KieRemoteServicesRuntimeException("Unknown serialization type when deserializing message " + msgId + ":" + serializationType);
            }
            byteMsg.writeUTF(msgStr);
        } catch (JMSException jmse) {
            String errMsg = "Unable to create response message or write to it [msg id: " + msgId + "].";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        } catch( Exception e) { 
            String errMsg = "Unable to serialize " + jaxbResponse.getClass().getSimpleName() + " to a String.";
            throw new KieRemoteServicesInternalError(errMsg, e);
        }
        return byteMsg;
    }
    
    // Runtime / KieSession / TaskService helper methods --------------------------------------------------------------------------
    
    private JaxbCommandsResponse processJaxbCommandsRequest(JaxbCommandsRequest request) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command<?>> commands = request.getCommands();

        if (commands != null) {
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                if( ! AcceptedCommands.getSet().contains(cmd.getClass())) {
                    UnsupportedOperationException uoe = new UnsupportedOperationException(cmd.getClass().getName()
                            + " is not a supported command.");
                    jaxbResponse.addException(uoe, i, cmd);
                    continue;
                }

                Object cmdResult = null;
                try {
                    // if the JTA transaction (in HT or the KieSession) doesn't commit, 
                    // that will cause message reception to be *NOT* acknowledged!
                    if( cmd instanceof TaskCommand<?>
                        && ! AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass())  ) {
                        cmdResult = executor.executeAndSerialize((InternalTaskService) taskService, (TaskCommand<?>) cmd);
                    } else {
                        // Synchronize around SSCS to avoid race-conditions with kie session cache clearing in afterCompletion
                        KieSession kieSession 
                            = runtimeMgrMgr.getRuntimeEngine(request.getDeploymentId(), request.getProcessInstanceId()).getKieSession();
                        SingleSessionCommandService sscs 
                            = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) kieSession).getCommandService();
                        synchronized(sscs) { 
                            if( cmd instanceof TaskCommand<?> ) {
                                cmdResult = executor.execute((InternalTaskService) taskService, (TaskCommand<?>) cmd);
                            } else { 
                                cmdResult = executor.execute(kieSession, cmd);
                            }
                        }
                    }
                } catch( Exception e ) { 
                    String errMsg =  "Unable to execute " + cmd.getClass().getSimpleName() + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage();
                    logger.warn(errMsg, e);
                    jaxbResponse.addException(new KieRemoteServicesRuntimeException(errMsg, e), i, cmd);
                }
                if (cmdResult != null) {
                    try {
                        // addResult could possibly throw an exception, which is why it's here and not above
                        jaxbResponse.addResult(cmdResult, i, cmd);
                    } catch (Exception e) {
                        String errMsg = "Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i 
                                + " because of "+ e.getClass().getSimpleName();
                        logger.error(errMsg, e);
                        jaxbResponse.addException(new KieRemoteServicesRuntimeException(errMsg, e), i, cmd);
                    }
                }
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }
    
}

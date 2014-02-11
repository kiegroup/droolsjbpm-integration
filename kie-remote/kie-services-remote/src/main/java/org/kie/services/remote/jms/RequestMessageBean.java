package org.kie.services.remote.jms;

import static org.kie.services.client.serialization.SerializationConstants.*;

import java.util.Collection;
import java.util.HashSet;
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

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.SerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.exception.DomainNotFoundBadRequestException;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.kie.services.remote.exception.KieRemoteServicesRuntimeException;
import org.kie.services.remote.jms.request.BackupIdentityProviderProducer;
import org.kie.services.remote.util.ExecuteAndSerializeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There are thus multiple queues to which an instance of this class could listen to, which is why 
 * the (JMS queue) configuration is done in the ejb-jar.xml file.
 * </p>
 * Doing the configuration in the ejb-jar.xml file which allows us to configure instances of one class 
 * to listen to more than one queue.
 * </p>
 * Also: responses to requests are <b>not</b> placed on a reply-to queue, but on the specified answer queue.
 */
public class RequestMessageBean implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessageBean.class);

    // JMS resources
    
    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory factory;

    @Resource
    private MessageDrivenContext context;
    
    // Initialized in @PostConstruct
    private Session session;
    private Connection connection;
   
    @Inject
    private RetryTrackerSingleton retryTracker;
    
    // KIE resources
    
    @Inject
    private DeploymentInfoBean runtimeMgrMgr;
    
    @Inject
    private TaskService injectedTaskService;

    @Inject
    private BackupIdentityProviderProducer backupIdentityProviderProducer;

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
                // default is JAXB
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

        // 2. Set initiator for request
        backupIdentityProviderProducer.createBackupIdentityProvider(cmdsRequest.getUser());

        // 3. process request
        jaxbResponse = processJaxbCommandsRequest(cmdsRequest);

        // 4. serialize response
        Message msg = serializeResponse(session, msgCorrId, serializationType, serializationProvider, jaxbResponse);

        // 5. send response
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

    private SerializationProvider getJaxbSerializationProvider(Message message) { 
        SerializationProvider serializationProvider;
        Set<Class<?>> serializationClasses = new HashSet<Class<?>>();
        
        try { 
            String deploymentId = null;
            ClassLoader classLoader = null;
            
            // Add classes from deployment (and get deployment classloader)
            if( message.propertyExists(DEPLOYMENT_ID_PROPERTY_NAME) ) { 
                deploymentId = message.getStringProperty(DEPLOYMENT_ID_PROPERTY_NAME);
                logger.debug( "Added classes from {} to serialization context.", deploymentId);
                Collection<Class<?>> deploymentClasses = runtimeMgrMgr.getDeploymentClasses(deploymentId);
                if( ! deploymentClasses.isEmpty() ) { 
                    serializationClasses.addAll(deploymentClasses);
                    // KieContainer (deployment) classloader
                    classLoader = deploymentClasses.iterator().next().getClassLoader(); 
                }
            }
            if( classLoader == null ) { 
                // Application classloader
                classLoader = this.getClass().getClassLoader();
            }
            
            // Add other classes that might only have been added to the war/application
            if( message.propertyExists(EXTRA_JAXB_CLASSES_PROPERTY_NAME) ) {
                String extraClassesString = message.getStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME);
                Set<Class<?>> moreExtraClasses = JaxbSerializationProvider.commaSeperatedStringToClassSet(classLoader, extraClassesString);
                for( Class<?> extraClass : moreExtraClasses ) { 
                    logger.debug("Added {} to serialization context.", extraClass.getName() );
                }
                serializationProvider = new JaxbSerializationProvider(moreExtraClasses);
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
                Collection<Class<?>> extraJaxbClasses =  ((JaxbSerializationProvider) serializationProvider).getExtraJaxbClasses();
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
        
        RuntimeEngine runtimeEngine = null;
        InternalTaskService internalTaskService = null;
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
                    // if the JTA transaction (in HT or the KieSession) doesn't commit, that will cause message reception to be *NOT* acknowledged!
                    if( cmd instanceof TaskCommand<?> ) { 
                        if( AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass()) ) {
                            runtimeEngine = runtimeMgrMgr.getRuntimeEngineForTaskCommand((TaskCommand<?>) cmd, injectedTaskService, true);
                            internalTaskService = (InternalTaskService) runtimeEngine.getTaskService();
                        }  else { 
                            internalTaskService = (InternalTaskService) injectedTaskService;
                        }
                        cmdResult = internalTaskService.execute(new ExecuteAndSerializeCommand((TaskCommand<?>) cmd));
                    } else { 
                        String deploymentId = request.getDeploymentId();
                        if( deploymentId == null ) {
                            throw new DomainNotFoundBadRequestException("A deployment id is required for the " + cmd.getClass().getSimpleName());
                        }
                        runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, request.getProcessInstanceId());
                        cmdResult = runtimeEngine.getKieSession().execute(cmd);
                    }
                } catch( Exception e ) { 
                    String errMsg =  "Unable to execute " + cmd.getClass().getSimpleName() + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage();
                    logger.warn(errMsg, e);
                    jaxbResponse.addException(new KieRemoteServicesRuntimeException(errMsg, e), i, cmd);
                } finally {
                    runtimeMgrMgr.disposeRuntimeEngine(runtimeEngine);
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

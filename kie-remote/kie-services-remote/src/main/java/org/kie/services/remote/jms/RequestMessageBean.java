package org.kie.services.remote.jms;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.remote.cdi.RuntimeManagerManager;
import org.kie.services.remote.exception.DomainNotFoundBadRequestException;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.kie.services.remote.rest.RestProcessRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the link between incoming request (whether via REST or JMS or .. whatever)
 * and the bean that processes the requests, the {@link RestProcessRequestBean}.
 * </p>
 * Responses to requests are <b>not</b> placed on the reply-to queue, but on the answer queue.
 * </p> Because there are multiple queues to which an instance of this class could listen to, the (JMS queue) configuration is
 * done in the ejb-jar.xml file, which allows us to configure instances of one class to listen to more than one queue.
 */
public class RequestMessageBean implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessageBean.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private String RESPONSE_QUEUE_NAME = null;
    private static String RESPONSE_QUEUE_NAME_PROPERTY = "kie.services.jms.queues.response";

    @Inject
    private RuntimeManagerManager runtimeMgrMgr;
    
    @Inject
    private TaskService taskService;
    
    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty(RESPONSE_QUEUE_NAME_PROPERTY, "queue/KIE.RESPONSE.ALL");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        boolean failure = false;
        String msgCorrId = null;
        try {
            msgCorrId = message.getJMSCorrelationID();
        } catch (JMSException jmse) {
            logger.warn("Unable to retrieve JMS correlation id from message! This id is needed to be able to match a request to a response message.",
                    jmse);
        }
        if( msgCorrId == null ) { 
            logger.warn("JMS correlation id is empty! This id is needed to be able to match a request to a response message.");
        }
        
        // 1. get request
        int[] serializationTypeHolder = new int[1];
        JaxbCommandsRequest cmdsRequest = deserializeRequest(message, msgCorrId, serializationTypeHolder);

        // 2. process request
        JaxbCommandsResponse jaxbResponse;
        if (cmdsRequest != null) {
            jaxbResponse = processJaxbCommandsRequest(cmdsRequest);
        } else {
            // Failure reasons have been logged in deserializeRequest(). 
            logger.error("Stopping processing of request message due to errors: see above.");
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
            logger.error("Unable to open new session to send response message to message " + msgCorrId, jmse);
            failure = true;
        } finally {
            if (failure) {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        if( session != null ) { 
                            session.close();
                            session = null;
                        }
                    } catch (JMSException jmse) {
                        logger.warn("Unable to close connection or session after failing to create connection or session.", jmse);
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
            logger.warn( "Unable to set correlation id of response to msg id " + msgCorrId, jmse );
        }
        
        // 5. send response message
        if (!failure) {
            try {
                InitialContext context = new InitialContext();
                Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_NAME);
                MessageProducer producer = session.createProducer(responseQueue);
                producer.send(msg);
            } catch (NamingException ne) {
                logger.error("Unable to lookup response queue (" + RESPONSE_QUEUE_NAME + ") to send msg " + msgCorrId 
                        + " (Is " + RESPONSE_QUEUE_NAME_PROPERTY + " incorrect?).",
                        ne );
            } catch (JMSException jmse) {
                logger.error("Unable to send msg " + msgCorrId + " to " + RESPONSE_QUEUE_NAME, jmse );
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                        if( session != null ) {
                            session.close();
                            session = null;
                        }
                    } catch (JMSException jmse) {
                        logger.error("Unable to close connection or session.", jmse);
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
            logger.error("Unable to read information from message " + msgId + ".", jmse);
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
            logger.error("Unable to create response message or write to it [msg id: " + msgId + "].", jmse);
        } catch( JAXBException jaxbe) { 
            throw new KieRemoteServicesInternalError("Unable to serialize " + jaxbResponse.getClass().getSimpleName() + " to a String.", jaxbe);
        }
        return byteMsg;
    }

    public JaxbCommandsResponse processJaxbCommandsRequest(JaxbCommandsRequest request) {
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
                if (cmd instanceof TaskCommand<?>) {
                    cmdResult = internalDoTaskOperation(cmd, jaxbResponse, i);
                } else {
                    cmdResult = internalDoKieSessionOperation( cmd, request, jaxbResponse, i);
                }
                if (cmdResult != null) {
                    try {
                        // addResult could possibly throw an exception, which is why it's here and not above
                        jaxbResponse.addResult(cmdResult, i, cmd);
                    } catch (Exception e) {
                        logger.error("Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i + " because of "
                                + e.getClass().getSimpleName(), e);
                        jaxbResponse.addException(e, i, cmd);
                    }
                }
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Object internalDoTaskOperation(Command<?> cmd, JaxbCommandsResponse jaxbResponse, int i) { 
        Object cmdResult;
        try { 
            cmdResult = doTaskOperation(cmd);
        } catch( UnauthorizedException ue ) { 
           Throwable cause = ue.getCause(); 
           if( cause instanceof PermissionDeniedException ) { 
               PermissionDeniedException pde = (PermissionDeniedException) cause;
               logger.warn(pde.getMessage());
               jaxbResponse.addException(pde, i, cmd);
               return null;
           }
           throw ue;
        }
        return cmdResult;
    }
    
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Object internalDoKieSessionOperation(Command<?> cmd, JaxbCommandsRequest request, JaxbCommandsResponse jaxbResponse, int i) { 
        Object cmdResult;
        try { 
            cmdResult = doKieSessionOperation(cmd, request.getDeploymentId(), request.getProcessInstanceId());
        } catch( DomainNotFoundBadRequestException dnfbre ) { 
            logger.warn( dnfbre.getMessage() );
            jaxbResponse.addException(dnfbre, i, cmd);
            return null;
        }
        return cmdResult;
    }
    
    private Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId) {
        Object result = null;
        try { 
            KieSession kieSession = getRuntimeEngine(deploymentId, processInstanceId).getKieSession();
            result = kieSession.execute(cmd);
        } catch( Exception e ) { 
            JaxbExceptionResponse exceptResp = new JaxbExceptionResponse(e, cmd);
            logger.warn( "Unable to execute " + exceptResp.getCommandName() + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage());
            logger.trace("Stack trace: \n", e);
            result = exceptResp;
        }
        return result;
    }
    
    private Object doTaskOperation(Command<?> cmd) {
        Object result = null;
        try {  
            result = ((InternalTaskService) taskService).execute(cmd);
        } catch( PermissionDeniedException pde ) { 
            throw new UnauthorizedException(pde.getMessage(), pde);
        } catch( Exception e ) { 
            JaxbExceptionResponse exceptResp = new JaxbExceptionResponse(e, cmd);
            logger.warn( "Unable to execute " + exceptResp.getCommandName() + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage());
            logger.trace("Stack trace: \n", e);
            result = exceptResp;
        }
        return result;
    }

    protected RuntimeEngine getRuntimeEngine(String domainName, Long processInstanceId) {
        RuntimeManager runtimeManager = runtimeMgrMgr.getRuntimeManager(domainName);
        Context<?> runtimeContext;
        if (processInstanceId != null) {
            runtimeContext = new ProcessInstanceIdContext(processInstanceId);
        } else {
            runtimeContext = EmptyContext.get();
        }
        if( runtimeManager == null ) { 
            throw new DomainNotFoundBadRequestException("No runtime manager could be found for domain '" + domainName + "'.");
        }
        return runtimeManager.getRuntimeEngine(runtimeContext);
    }
}

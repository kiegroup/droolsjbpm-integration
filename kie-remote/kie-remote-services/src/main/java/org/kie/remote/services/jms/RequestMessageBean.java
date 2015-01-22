package org.kie.remote.services.jms;

import static org.kie.services.client.serialization.JaxbSerializationProvider.JMS_SERIALIZATION_TYPE;
import static org.kie.services.client.serialization.SerializationConstants.DEPLOYMENT_ID_PROPERTY_NAME;
import static org.kie.services.client.serialization.SerializationConstants.SERIALIZATION_TYPE_PROPERTY_NAME;
import static org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus.FORBIDDEN;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.jbpm.services.task.identity.adapter.UserGroupAdapter;
import org.kie.api.command.Command;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.exception.KieRemoteServicesRuntimeException;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.kie.remote.services.jms.request.BackupIdentityProviderProducer;
import org.kie.remote.services.jms.security.JmsUserGroupAdapter;
import org.kie.remote.services.jms.security.UserPassCallbackHandler;
import org.kie.remote.services.rest.jaxb.DynamicJaxbContext;
import org.kie.remote.services.rest.jaxb.DynamicJaxbContextFilter;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.SerializationProvider;
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

    // Initialized in @PostConstruct
    private Session session;
    private Connection connection;

    @Inject
    private RetryTrackerSingleton retryTracker;

    // KIE resources

    @Inject
    protected DeploymentInfoBean runtimeMgrMgr;

    @Inject
    protected ProcessRequestBean processRequestBean;

    @Inject
    protected BackupIdentityProviderProducer backupIdentityProviderProducer;

    @Inject
    private DynamicJaxbContext dynamicJaxbContext;
    
    // Constants / properties
    private String RESPONSE_QUEUE_NAME = null;
    private static final String RESPONSE_QUEUE_NAME_PROPERTY = "kie.services.jms.queues.response";

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
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (session != null) {
                session.close();
                session = null;
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to close " + (connection == null ? "session" : "connection");
            logger.error(errMsg, jmse);
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }
    }

    // See EJB 3.1 fr, 5.4.12 and 13.3.3: BMT for which the (last) ut.commit() confirms message reception
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {

        String msgId = null;
        boolean redelivered = false;
        try {
            msgId = message.getJMSMessageID();
            redelivered = message.getJMSRedelivered();
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve JMS " + (msgId == null ? "redelivered flag" : "message id")
                    + " from JMS message. Message will not be returned to queue.";
            logger.warn(errMsg, jmse);
        }

        if( redelivered ) { 
            if (retryTracker.maxRetriesReached(msgId)) {
                logger.warn("Maximum number of retries (" + retryTracker.getMaximumLimitRetries() + ") reached for message " + msgId );
                logger.warn("Acknowledging message but NOT processing it.");
                return;
            } else {
                logger.warn("Retry number " + retryTracker.incrementRetries(msgId) + " of message " + msgId);
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
            if (!message.propertyExists(SERIALIZATION_TYPE_PROPERTY_NAME)) {
                // default is JAXB
                serializationType = JMS_SERIALIZATION_TYPE;
            } else {
                serializationType = message.getIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to get properties from message " + msgCorrId + ".";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        }

        SerializationProvider serializationProvider;
        switch (serializationType) {
        case JMS_SERIALIZATION_TYPE:
            serializationProvider = getJaxbSerializationProvider(message);
            break;
        default:
            throw new KieRemoteServicesInternalError("Unknown serialization type: " + serializationType);
        }

        // 1. deserialize request
        JaxbCommandsRequest cmdsRequest = deserializeRequest(message, msgCorrId, serializationProvider, serializationType);

        // 2. security/identity
        cmdsRequest.setUserPass(getUserPass(message));

        // 3. process request
        jaxbResponse = jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        // 4. serialize response
        Message msg = serializeResponse(session, msgCorrId, serializationType, serializationProvider, jaxbResponse);

        serializationProvider.dispose();
        
        // 5. send response
        sendResponse(msgCorrId, serializationType, msg);

        if (redelivered) {
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
            logger.error(errMsg, jmse);
            return;
        }

        // 3c. send response message
        MessageProducer producer = null;
        try {
            Queue responseQueue = (Queue) (new InitialContext()).lookup(RESPONSE_QUEUE_NAME);
            producer = session.createProducer(responseQueue);
            producer.send(msg);
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

    // De/Serialization helper methods -------------------------------------------------------------------------------------------

    private static JaxbCommandsRequest deserializeRequest(Message message, String msgId, SerializationProvider serializationProvider, int serializationType) {

        JaxbCommandsRequest cmdMsg = null;
        try {
            String msgStrContent = null;

            switch (serializationType) {
            case JMS_SERIALIZATION_TYPE:
                msgStrContent = ((TextMessage) message).getText();
                cmdMsg = (JaxbCommandsRequest) serializationProvider.deserialize(msgStrContent);
                break;
            default:
                throw new KieRemoteServicesRuntimeException("Unknown serialization type when deserializing message " + msgId + ":" + serializationType);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to read information from message " + msgId + ".";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to serialize String to " + JaxbCommandsRequest.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new KieRemoteServicesInternalError(errMsg, e);
        }
        return cmdMsg;
    }

    private SerializationProvider getJaxbSerializationProvider(Message message) {
        SerializationProvider serializationProvider;
        try {
            // Add classes from deployment (and get deployment classloader)
            if (message.propertyExists(DEPLOYMENT_ID_PROPERTY_NAME)) {
                String deploymentId = message.getStringProperty(DEPLOYMENT_ID_PROPERTY_NAME);
                DynamicJaxbContext.setDeploymentJaxbContext(deploymentId);
            } else { 
                DynamicJaxbContext.setDeploymentJaxbContext(DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID);
            }
            serializationProvider = ServerJaxbSerializationProvider.newInstance(dynamicJaxbContext);
        } catch (JMSException jmse) {
            throw new KieRemoteServicesInternalError("Unable to check or read JMS message for property.", jmse);
        } catch (SerializationException se) {
            throw new KieRemoteServicesRuntimeException("Unable to load classes needed for JAXB deserialization.", se);
        }
        return serializationProvider;
    }

    private static Message serializeResponse(Session session, String msgId, int serializationType,
            SerializationProvider serializationProvider, JaxbCommandsResponse jaxbResponse) {
        TextMessage textMsg = null;
        try {
            String msgStr;
            switch (serializationType) {
            case JMS_SERIALIZATION_TYPE:
                msgStr = (String) serializationProvider.serialize(jaxbResponse);
                break;
            default:
                throw new KieRemoteServicesRuntimeException("Unknown serialization type when deserializing message " + msgId + ":" + serializationType);
            }
            textMsg = session.createTextMessage(msgStr);
            textMsg.setIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME, serializationType);
        } catch (JMSException jmse) {
            String errMsg = "Unable to create response message or write to it [msg id: " + msgId + "].";
            throw new KieRemoteServicesRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to serialize " + jaxbResponse.getClass().getSimpleName() + " to a String.";
            throw new KieRemoteServicesInternalError(errMsg, e);
        }
        return textMsg;
    }

    // Runtime / KieSession / TaskService helper methods --------------------------------------------------------------------------
    protected JaxbCommandsResponse jmsProcessJaxbCommandsRequest(JaxbCommandsRequest request) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command> commands = request.getCommands();
      
        if (commands != null) {
            UserGroupAdapter userGroupAdapter = null;
            try { 
                for (int i = 0; i < commands.size(); ++i) {

                    Command<?> cmd = commands.get(i);
                    if (!AcceptedServerCommands.isAcceptedCommandClass(cmd.getClass())) {
                        String cmdName = cmd.getClass().getName();
                        String errMsg = cmdName + " is not a supported command and will not be executed.";
                        logger.warn( errMsg );
                        UnsupportedOperationException uoe = new UnsupportedOperationException(errMsg);
                        jaxbResponse.addException(uoe, i, cmd, FORBIDDEN);
                        continue;
                    }

                    List<String> userRoles = new ArrayList<String>();
                    if( cmd instanceof TaskCommand && userGroupAdapter == null ) { 
                        userGroupAdapter = getUserFromMessageAndLookupAndInjectGroups(request.getUserPass(), userRoles);
                    }
                    backupIdentityProviderProducer.createBackupIdentityProvider(request.getUser(), userRoles);

                    // if the JTA transaction (in HT or the KieSession) doesn't commit, that will cause message reception to be *NOT* acknowledged!
                    processRequestBean.processCommand(cmd, request, i, jaxbResponse);
                }
            } finally { 
                clearUserGroupAdapter(userGroupAdapter);
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }

    /**
     * Retrieves the user/pass info from the message and authenticates it against the underlying JAAS module:<ul>
     * <li>Calls {@link RequestMessageBean#getUserPass(Message)} to get the user and password from the {@link Message} instance</li>
     * <li>Calls {@link RequestMessageBean#tryLogin(String[])} to create a {@link LoginContext} and login</li>
     * <li>Calls {@link RequestMessageBean#getGroupsFromSubject(Subject)} to retrieve the Roles info from the JAAS login.</li>
     * <li>Injects the groups information into the underlying framework for use by the human-task code</li>
     * </ul>
     * 
     * @param msg The JMS {@link Message} received.
     */
    private UserGroupAdapter getUserFromMessageAndLookupAndInjectGroups(String [] userPass, List<String> userRoles) {
        UserGroupAdapter jmsUserGroupAdapter = null;
        try {
            if( userPass == null ) { 
                logger.warn("Unable to retrieve user and password from message: NOT injecting group information.");
                return null;
            }
            Subject msgSubject = tryLogin(userPass);
            if( msgSubject == null ) { 
                logger.warn("Unable to login to JAAS with received user and password.");
                return null;
            }
            List<Principal> roles = getGroupsFromSubject(msgSubject);
            String [] rolesArr = new String[roles.size()];
            for( int i = 0; i < rolesArr.length; ++i ) { 
                rolesArr[i] = roles.get(i).getName();
                userRoles.add(rolesArr[i]);
            }
            UserGroupAdapter newUserGroupAdapter = new JmsUserGroupAdapter(userPass[0], rolesArr);
            jmsUserGroupAdapter = newUserGroupAdapter;
            JAASUserGroupCallbackImpl.addExternalUserGroupAdapter(newUserGroupAdapter);
        } catch (Exception e) {
            logger.warn("Unable to retrieve group information for user in message: " + e.getMessage(), e);
        } 
        return jmsUserGroupAdapter;
    }

    private void clearUserGroupAdapter(UserGroupAdapter userGroupAdapter) {
        if( userGroupAdapter != null ) { 
            JAASUserGroupCallbackImpl.clearExternalUserGroupAdapter();
        }
    }

    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
   
    /**
     * Get the user and password information.
     * 
     * @param msg The JMS {@link Message} received.
     * @return A String array, with the user and password in that order. In the case that something goes wrong, null is returned.
     */
    private String[] getUserPass(Message msg) {
        String prop = USERNAME_PROPERTY;
        try { 
            String user = null;
            String pass = null;
            if( msg.propertyExists(prop) ) { 
               user = msg.getStringProperty(prop);
            } 
            prop = PASSWORD_PROPERTY;
            if( msg.propertyExists(prop) ) { 
               pass = msg.getStringProperty(prop);
            } 
            if( user != null && pass != null ) { 
                String [] userPass = { user, pass };
                return userPass;
            }
        } catch(Exception e) { 
           logger.error( "Unable to retrieve '" + prop + "' from JMS message.", e);
        }
        return null;
    }

    /**
     * Try to login to the underlying JAAS module via a {@link LoginException}.
     * 
     * @param userPass A String array containing the user and password information.
     * @return The logged-in {@link Subject}
     * @throws LoginException If something goes wrong when trying to login. 
     */
    private Subject tryLogin(String[] userPass) throws LoginException {
        try { 
            CallbackHandler handler = new UserPassCallbackHandler(userPass);
            LoginContext lc = new LoginContext("kie-jms-login-context", handler);
            lc.login();
            return lc.getSubject();
        } catch( Exception e ) { 
            logger.error( "Unable to login via JAAS with message supplied user and password", e);
            return null; 
        }
    }

    /**
     * Extracts the list of roles from the subject.
     * 
     * @param subject The JAAS login subject
     * @return A list of {@link Principal} objects, that are the role/groups.
     */
    private List<Principal> getGroupsFromSubject(Subject subject) {
        List<Principal> userGroups = new ArrayList<Principal>();
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof Group && "Roles".equalsIgnoreCase(principal.getName())) {
                Enumeration<? extends Principal> groups = ((Group) principal).members();
                while (groups.hasMoreElements()) {
                    Principal groupPrincipal = (Principal) groups.nextElement();
                    userGroups.add(groupPrincipal);
                }
            }
        }
        return userGroups;
    }

}

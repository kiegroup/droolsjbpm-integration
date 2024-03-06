/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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

import org.kie.server.api.ConversationId;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.services.impl.security.adapters.BrokerSecurityAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.jms.JMSConstants.*;

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(name = "KieServerMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "queue/KIE.SERVER.REQUEST"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/KIE.SERVER.REQUEST"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class KieServerMDB
        implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger( KieServerMDB.class );

    // Constants / properties
    private              String RESPONSE_QUEUE_NAME          = null;
    private static final String DEFAULT_RESPONSE_QUEUE_NAME  = "queue/KIE.SERVER.RESPONSE";

    private static final String ID_NECESSARY = "This id is needed to be able to match a request to a response message.";

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory factory;

    private boolean sessionTransacted;
    private int sessionAck;


    private KieServerImpl kieServer;
    private Map<MarshallingFormat, Marshaller> marshallers;

    @PostConstruct
    public void init() {
        RESPONSE_QUEUE_NAME = System.getProperty( KieServerConstants.CFG_KIE_SERVER_RESPONSE_QUEUE, DEFAULT_RESPONSE_QUEUE_NAME );

        sessionTransacted = Boolean.parseBoolean(System.getProperty(KieServerConstants.CFG_KIE_SERVER_JMS_SESSION_TX, "false"));
        sessionAck = Integer.parseInt(System.getProperty(KieServerConstants.CFG_KIE_SERVER_JMS_SESSION_ACK, String.valueOf(Session.AUTO_ACKNOWLEDGE)));
        kieServer = KieServerLocator.getInstance();

        marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>(  );
        // the commands classloader -- not sure if there is a better way to get a reference to it
        ClassLoader classLoader = CommandScript.class.getClassLoader();
        marshallers.put( MarshallingFormat.XSTREAM, MarshallerFactory.getMarshaller( MarshallingFormat.XSTREAM, classLoader ) );
        marshallers.put( MarshallingFormat.JAXB, MarshallerFactory.getMarshaller( MarshallingFormat.JAXB, classLoader ) );
        marshallers.put( MarshallingFormat.JSON, MarshallerFactory.getMarshaller( MarshallingFormat.JSON, classLoader ) );
    }

    /**
     * This method is used to initialize the JMS connection and
     * session. It is done in its own method so that if the
     * point at which it is done needs to be changed then
     * it can be done by just changing the invocation point.
     */
    private JMSConnection startConnectionAndSession() {
       JMSConnection result = null;
       Connection connection = null;
       Session session = null;
       try {
          connection = factory.createConnection();
          if ( connection != null ) {
             session = connection.createSession( sessionTransacted, sessionAck );
             result = new JMSConnection(connection,session);
             if ( logger.isDebugEnabled() ) {
                logger.debug( "KieServerMDB sessionTransacted={}, sessionAck={}",
                        sessionTransacted,
                        sessionAck);
             }
          }
       } catch (JMSException jmse) {
          String errMsg = "Unable to obtain connection/session";
          logger.error( errMsg, jmse );
          throw new JMSRuntimeException( errMsg, jmse );
       } finally {
           if (connection != null && session == null){
               logger.error("KieServerMDB: Session creation failed - closing connection");
               try {
                   connection.close();
               } catch (JMSException jmse) {
                   String errMsg = "KieServerMDB: Error closing connection after failing to open session";
                   throw new JMSRuntimeException(errMsg, jmse);
               }
           }
       }
       return result;
    }


    private void closeConnectionAndSession(JMSConnection connected) {
        Connection connection = null;
        Session session = null;
        if (connected == null) {
            logger.debug("KieServerMDB: JMSConnection is null, unable to close connection/session");
            return;
        } else {
            connection = connected.getConnection();
            session = connected.getSession();
        }
        JMSException sessionError = null;
        if (session != null) {
            try {
                session.close();
                logger.debug("KieServerMDB: Session closed");
            } catch (JMSException jmse) {
                sessionError = jmse;
            } finally {
                session = null;
            }
        } else {
            logger.debug("KieServerMDB: session was 'null', so cannot be closed");
        }
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.debug("KieServerMDB: Connection closed");
            } catch (JMSException jmse) {
                String errMsg = (sessionError != null) ?
                        "KieServerMDB: Error closing both session and connection" :
                        "KieServerMDB: Error closing connection";
                logger.error(errMsg, jmse);
                throw new JMSRuntimeException( errMsg, jmse );
            } finally {
                if (connection == null && sessionError != null) {
                    logger.warn("KieServerMDB: Error closing session",sessionError);
                    logger.warn("KieServerMDB: Assuming session was closed by connection closure");
                    session = null;
                }
            }
        } else {
            logger.debug("KieServerMDB: connection was 'null', so cannot be closed");
            if ( sessionError != null ) {
                String errMsg = "KieServerMDB: Error closing session";
                logger.error( errMsg, sessionError );
                session = null;
                throw new JMSRuntimeException(errMsg,sessionError);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
    }

    public void onMessage(Message message) {
        JMSConnection connect = null;
        try {
            String username = null;
            String password = null;
            try {
                username = message.getStringProperty(USER_PROPERTY_NAME);
                password = message.getStringProperty(PASSWRD_PROPERTY_NAME);
            } catch (JMSException jmse) {
                logger.warn("Unable to retrieve user name and/or password, from message");
            }
            if (username != null && password != null) {
                BrokerSecurityAdapter.login(username, password);
            } else {
                logger.warn("Unable to login to JMSSecurityAdapter, user name and/or password missing");
            }

            KieContainerCommandService executor = null;

            // 0. Get msg correlation id (for response)
            String msgCorrId = null;
            try {
                msgCorrId = message.getJMSCorrelationID();
            } catch (JMSException jmse) {
                String errMsg = "Unable to retrieve JMS correlation id from message! " + ID_NECESSARY;
                throw new JMSRuntimeException(errMsg, jmse);
            }

            String targetCapability = getStringProperty(message, TARGET_CAPABILITY_PROPERTY_NAME, "KieServer"); // for backward compatibility default to KieServer
            String containerId = getStringProperty(message, CONTAINER_ID_PROPERTY_NAME, null);
            String conversationId = getStringProperty(message, CONVERSATION_ID_PROPERTY_NAME, null);

            int interactionPattern = getIntProperty(message, INTERACTION_PATTERN_PROPERTY_NAME, REQUEST_REPLY_PATTERN);

            // 1. get marshalling info
            MarshallingFormat format = null;
            String classType = null;
            try {
                classType = message.getStringProperty(CLASS_TYPE_PROPERTY_NAME);

                if (!message.propertyExists(SERIALIZATION_FORMAT_PROPERTY_NAME)) {
                    format = MarshallingFormat.JAXB;
                } else {

                    int intFormat = message.getIntProperty(SERIALIZATION_FORMAT_PROPERTY_NAME);
                    logger.debug("Serialization format (int) is {}", intFormat);
                    format = MarshallingFormat.fromId(intFormat);
                    logger.debug("Serialization format is {}", format);
                    if (format == null) {
                        String errMsg = "Unsupported marshalling format '" + intFormat + "' from message " + msgCorrId + ".";
                        throw new JMSRuntimeException(errMsg);
                    }
                }
            } catch (JMSException jmse) {
                String errMsg = "Unable to retrieve property '" + SERIALIZATION_FORMAT_PROPERTY_NAME + "' from message " + msgCorrId + ".";
                throw new JMSRuntimeException(errMsg, jmse);
            }

            // 2. get marshaller
            Marshaller marshaller = getMarshaller(containerId, format);
            logger.debug("Selected marshaller is {}", marshaller);

            // 3. deserialize request
            CommandScript script = unmarshallRequest(message, msgCorrId, marshaller, format);

            logger.debug("Target capability is {}", targetCapability);
            for (KieServerExtension extension : kieServer.getServerExtensions()) {
                KieContainerCommandService tmp = extension.getAppComponents(KieContainerCommandService.class);

                if (tmp != null && extension.getImplementedCapability().equalsIgnoreCase(targetCapability)) {
                    executor = tmp;
                    logger.debug("Extension {} returned command executor {} with capability {}", extension, executor, extension.getImplementedCapability());
                    break;
                }
            }
            if (executor == null) {
                throw new IllegalStateException("No executor found for script execution");
            }

            // 4. process request
            ServiceResponsesList response = executor.executeScript(script, format, classType);

            if (interactionPattern < UPPER_LIMIT_REPLY_INTERACTION_PATTERNS) {
                connect = startConnectionAndSession();
                logger.debug("Response message is about to be sent according to selected interaction pattern {}", interactionPattern);
                // 5. serialize response
                Message msg = marshallResponse(connect.getSession(), msgCorrId, format, marshaller, response);
                // set conversation id for routing
                if (containerId != null && (conversationId == null || conversationId.trim().isEmpty())) {
                    try {
                        KieContainerInstance containerInstance = kieServer.getServerRegistry().getContainer(containerId);
                        if (containerInstance != null) {
                            ReleaseId releaseId = containerInstance.getResource().getResolvedReleaseId();
                            if (releaseId == null) {
                                releaseId = containerInstance.getResource().getReleaseId();
                            }

                            conversationId = ConversationId.from(KieServerEnvironment.getServerId(), containerId, releaseId).toString();
                        }
                    } catch (Exception e) {
                        logger.warn("Unable to build conversation id due to {}", e.getMessage(), e);
                    }
                }
                try {
                    if (conversationId != null) {
                        msg.setStringProperty(CONVERSATION_ID_PROPERTY_NAME, conversationId);
                    }
                } catch (JMSException e) {
                    logger.debug("Unable to set conversation id on response message due to {}", e.getMessage());
                }

                // 6. send response
                sendResponse(connect.getSession(),msgCorrId, format, msg);
            } else {
                logger.debug("Response message is skipped according to selected interaction pattern {}", FIRE_AND_FORGET_PATTERN);
            }

        } finally {
            if (connect != null) { // Only attempt to close the connection/session if they were actually created
                try {
                    closeConnectionAndSession(connect);
                } catch (JMSRuntimeException runtimeException) {
                    logger.error("Error while attempting to close connection/session",runtimeException);
                } finally {
                    BrokerSecurityAdapter.logout();
                }
            } else {
                BrokerSecurityAdapter.logout();
            }
        }

    }

    private static CommandScript unmarshallRequest(Message message, String msgId, Marshaller serializationProvider, MarshallingFormat format) {
        CommandScript cmdMsg = null;
        try {
            String msgStrContent = ((TextMessage) message).getText();
            logger.debug("About to unmarshal content '{}'", msgStrContent);
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

    private void sendResponse(Session session, String msgCorrId, MarshallingFormat format, Message msg) {
        // set correlation id in response message
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
        } catch (NamingException ne) {
            String errMsg = "Unable to lookup response queue " + RESPONSE_QUEUE_NAME + " to send msg " + msgCorrId
                            + " (Is " + KieServerConstants.CFG_KIE_SERVER_RESPONSE_QUEUE + " incorrect?).";
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

    protected Marshaller getMarshaller(String containerId, MarshallingFormat format) {
        if (containerId == null || containerId.isEmpty()) {
            return marshallers.get(format);
        }

        KieContainerInstance kieContainerInstance = kieServer.getServerRegistry().getContainer(containerId);
        if (kieContainerInstance != null && kieContainerInstance.getKieContainer() != null) {
            return kieContainerInstance.getMarshaller(format);
        }

        return marshallers.get(format);
    }

    protected String getStringProperty(Message message, String name, String defaultValue) {
        try {
            if (message.propertyExists(name)) {
                return message.getStringProperty(name);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve property '" + name + "' from message " + message + ".";
            logger.debug(errMsg, jmse);
        }

        return defaultValue;
    }

    protected int getIntProperty(Message message, String name, int defaultValue) {
        try {
            if (message.propertyExists(name)) {
                return message.getIntProperty(name);
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve property '" + name + "' from message " + message + ".";
            logger.debug(errMsg, jmse);
        }

        return defaultValue;
    }

}

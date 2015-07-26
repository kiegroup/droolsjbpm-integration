/*
 * Copyright 2015 JBoss Inc
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

package org.kie.services.client.api.command;

import static org.kie.services.client.api.command.AbstractRemoteCommandObject.emptyDeploymentId;
import static org.kie.services.client.api.command.AbstractRemoteCommandObject.logger;
import static org.kie.services.client.api.command.AbstractRemoteCommandObject.prepareCommandRequest;
import static org.kie.services.client.serialization.SerializationConstants.DEPLOYMENT_ID_PROPERTY_NAME;
import static org.kie.services.client.serialization.SerializationConstants.SERIALIZATION_TYPE_PROPERTY_NAME;
import static org.kie.services.shared.ServicesVersion.VERSION;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kie.api.command.Command;
import org.kie.remote.client.api.exception.MissingRequiredInfoException;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.SerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;

/**
 * This class has been created to help users find the correct logic to 
 * send {@link JaxbCommandsRequest} instances to a server instance.
 */
public class InternalJmsCommandHelper {

    public static <T> T internalExecuteJmsCommand( Command command,  // required
            ConnectionFactory factory,  // required
            Queue sendQueue, // required
            Queue responseQueue, // required
            SerializationProvider serializationProvider) { 
            
       return internalExecuteJmsCommand(command, 
               null, null, 
               null, null, 
               null, 
               null, 
               null, 
               factory, 
               sendQueue, 
               responseQueue, 
               serializationProvider, 
               null, 
               ClientJaxbSerializationProvider.JMS_SERIALIZATION_TYPE, 
               null);
    }
    
    public static <T> T internalExecuteJmsCommand( Command command,  // required
            String connUser, String connPassword,
            String userName, String password, 
            String deploymentId,  // required for all non-audit commands
            Long processInstanceId,  // required for process instance runtimes
            List<String> rorrelationKeyProperties, // required for correlated sessions
            ConnectionFactory factory,  // required
            Queue sendQueue, // required
            Queue responseQueue, // required
            SerializationProvider serializationProvider,  // required
            Set<Class<?>> extraJaxbClasses, 
            int serializationType, // required
            Long timeoutInMillisecs) {
        JaxbCommandsRequest req = prepareCommandRequest(command, userName, deploymentId, processInstanceId, rorrelationKeyProperties );

        Connection connection = null;
        Session session = null;
        JaxbCommandsResponse cmdResponse = null;
        String corrId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = '" + corrId + "'";
        try {

            // setup
            MessageProducer producer;
            MessageConsumer consumer;
            try {
                if( password != null ) {
                    connection = factory.createConnection(connUser, connPassword);
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to setup a JMS connection.", jmse);
            }

            // Create msg
            TextMessage textMsg;
            try {

                // serialize request
                String xmlStr = serializationProvider.serialize(req);
                textMsg = session.createTextMessage(xmlStr);
                
                // set properties
                // 1. corr id
                textMsg.setJMSCorrelationID(corrId);
                // 2. serialization info
                textMsg.setIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME, serializationType);
                if( extraJaxbClasses != null && !extraJaxbClasses.isEmpty() ) {
                    if( emptyDeploymentId(deploymentId) ) {
                        throw new MissingRequiredInfoException(
                                "Deserialization of parameter classes requires a deployment id, which has not been configured.");
                    }
                    textMsg.setStringProperty(DEPLOYMENT_ID_PROPERTY_NAME, deploymentId);
                }
                // 3. user/pass for task operations
                boolean isTaskCommand = (command instanceof TaskCommand);
                if( isTaskCommand ) {
                    if( userName == null ) {
                        throw new RemoteCommunicationException(
                                "A user name is required when sending task operation requests via JMS");
                    }
                    if( password == null ) {
                        throw new RemoteCommunicationException(
                                "A password is required when sending task operation requests via JMS");
                    }
                    textMsg.setStringProperty("username", userName);
                    textMsg.setStringProperty("password", password);
                }
                // 4. process instance id
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to create and fill a JMS message.", jmse);
            } catch( SerializationException se ) {
                throw new RemoteCommunicationException("Unable to deserialze JMS message.", se.getCause());
            }

            // send
            try {
                producer.send(textMsg);
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                if( timeoutInMillisecs != null ) { 
                    response = consumer.receive(timeoutInMillisecs);
                } else { 
                    response = consumer.receive();
                }
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to receive or retrieve the JMS response.", jmse);
            }

            if( response == null ) {
                logger.warn("Response is empty");
                return null;
            }
            // extract response
            assert response != null: "Response is empty.";
            try {
                String xmlStr = ((TextMessage) response).getText();
                cmdResponse = (JaxbCommandsResponse) serializationProvider.deserialize(xmlStr);
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jmse);
            } catch( SerializationException se ) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", se.getCause());
            }
            assert cmdResponse != null: "Jaxb Cmd Response was null!";
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                    if( session != null ) {
                        session.close();
                    }
                } catch( JMSException jmse ) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
        String version = cmdResponse.getVersion();
        if( version == null ) {
            version = "pre-6.0.3";
        }
        if( !version.equals(VERSION) ) {
            logger.info("Response received from server version [{}] while client is version [{}]! This may cause problems.",
                    version, VERSION);
        }
        List<JaxbCommandResponse<?>> responses = cmdResponse.getResponses();
        if( responses.size() > 0 ) {
            JaxbCommandResponse<?> response = responses.get(0);
            if( response instanceof JaxbExceptionResponse ) {
                JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) response;
                throw new RemoteApiException(exceptionResponse.getMessage());
            } else {
                return (T) response.getResult();
            }
        } else {
            assert responses.size() == 0: "There should only be 1 response, not " + responses.size() + ", returned by a command!";
            return null;
        }
    }

}

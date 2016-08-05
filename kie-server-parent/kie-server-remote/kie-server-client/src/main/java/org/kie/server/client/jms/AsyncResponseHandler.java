/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.jms;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.impl.KieServicesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async response handler that receives message from response queue using message listener.
 * It requires callback to be invoked upon message delivery otherwise will throw IllegalStateException on runtime.
 * <br/>
 * Due to nature of message listener (cannot clean up its connection and session) another thread is used to perform the cleanup
 * in finally block of message listener.
 * <br/>
 * Response is only delivered via callback thus return value of handleResponse is always single ServiceResponse of type NO_RESPONSE
 */
public class AsyncResponseHandler implements ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(AsyncResponseHandler.class);

    private ResponseCallback callback;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AsyncResponseHandler(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getInteractionPattern() {
        return JMSConstants.ASYNC_REPLY_PATTERN;
    }

    @Override
    public ServiceResponsesList handleResponse(String selector, Connection connection, Session session, Queue responseQueue, KieServicesConfiguration config, Marshaller marshaller, KieServicesClient owner) {

        if (callback == null) {
            throw new IllegalStateException("There is no callback defined, can't continue...");
        }

        MessageConsumer consumer = null;
        try {
            consumer = session.createConsumer(responseQueue, selector);
            consumer.setMessageListener(new AsyncMessageListener(connection, session, selector, consumer, marshaller, owner));
            logger.debug("Message listener for async message retrieval successfully registered on consumer {}", consumer);

        } catch( JMSException jmse ) {
            throw new KieServicesException("Unable to retrieve JMS response from queue " + responseQueue + " with selector " + selector, jmse);
        }

        ServiceResponse messageSentResponse = new ServiceResponse(ServiceResponse.ResponseType.NO_RESPONSE, "Message sent");
        return new ServiceResponsesList(Arrays.asList(messageSentResponse));
    }

    @Override
    public void dispose(Connection connection, Session session) {
        // no op as the resources are closed from within message listener (via separate thread)
    }

    private class AsyncMessageListener implements MessageListener  {
        private String selector;
        private MessageConsumer consumer;
        private Marshaller marshaller;
        private KieServicesClient owner;

        private Connection connection;
        private Session session;

        public AsyncMessageListener(Connection connection, Session session, String selector, MessageConsumer consumer, Marshaller marshaller, KieServicesClient owner) {
            this.selector = selector;
            this.consumer = consumer;
            this.marshaller = marshaller;
            this.owner = owner;
            this.connection = connection;
            this.session = session;
        }

        @Override
        public void onMessage(Message message) {
            try {
                ((KieServicesClientImpl) owner).setConversationId(message.getStringProperty(JMSConstants.CONVERSATION_ID_PROPERTY_NAME));

                String responseStr = ((TextMessage) message).getText();
                logger.debug("Received response from server '{}'", responseStr);

                ServiceResponsesList cmdResponse = marshaller.unmarshall(responseStr, ServiceResponsesList.class);
                logger.debug("Unmarshalled response from async delivery {} calling callback {}", cmdResponse, callback);

                callback.onResponse(selector, cmdResponse);
                logger.debug("Callback {} successfully invoked with response {}", callback, cmdResponse);
            } catch (Exception e) {
                logger.error("Error while receiving message due to {}, this means response from the server won't be delivered to client", e.getMessage(), e);
            } finally {
                if (consumer != null) {
                    try {
                        consumer.close();
                    } catch (JMSException e) {
                        logger.warn("Error when closing JMS consumer due to {}", e.getMessage());
                    }
                }
                // submit work to executor service to close resources
                // as they cannot be closed from message listener
                // due to AMQ129006: It is illegal to call this method (session.close()) from within a Message Listener
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (session != null) {
                                session.close();
                                logger.debug("Session closed via separate thread.");
                            }
                            if (connection != null) {
                                connection.close();
                                logger.debug("Connection closed via separate thread.");
                            }
                        } catch (JMSException jmse) {
                            logger.warn("Unable to close connection or session!", jmse);
                        }

                    }
                });
                logger.debug("Cleanup of JMS resources requested via separate thread.");
            }
        }
    }
}

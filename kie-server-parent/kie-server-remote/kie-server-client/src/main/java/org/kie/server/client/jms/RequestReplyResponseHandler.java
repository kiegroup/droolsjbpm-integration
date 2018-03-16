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

import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.client.impl.KieServicesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestReplyResponseHandler implements ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestReplyResponseHandler.class);
    @Override
    public int getInteractionPattern() {
        return JMSConstants.REQUEST_REPLY_PATTERN;
    }

    @Override
    public ServiceResponsesList handleResponse(String selector, Connection connection, Session session, Queue responseQueue, KieServicesConfiguration config, Marshaller marshaller, KieServicesClient owner) {

        MessageConsumer consumer = null;

        try {            
            consumer = config.getResources().getConsumer();

            Message response = consumer.receive( config.getTimeout() );

            if( response == null ) {
                logger.warn("Response is empty");
                // return actual instance to avoid null points on client side
                List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();
                responses.add(new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Response is empty"));
                return new ServiceResponsesList(responses);
            }

            ((KieServicesClientImpl)owner).setConversationId(response.getStringProperty(JMSConstants.CONVERSATION_ID_PROPERTY_NAME));

            String responseStr = ((TextMessage) response).getText();
            logger.debug("Received response from server '{}'", responseStr);
            ServiceResponsesList cmdResponse = marshaller.unmarshall(responseStr, ServiceResponsesList.class);
            return cmdResponse;
        } catch( JMSException jmse ) {
            throw new KieServicesException("Unable to retrieve JMS response from queue " + responseQueue + " with selector " + selector, jmse);
        } 
    }

    @Override
    public void dispose(Connection connection, Session session) {

    }
}

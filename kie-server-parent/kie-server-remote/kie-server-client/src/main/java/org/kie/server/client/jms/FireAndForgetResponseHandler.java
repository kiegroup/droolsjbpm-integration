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

import java.util.List;
import java.util.ArrayList;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fire and forget response handler meaning it does not wait for any response as it actually
 * instructs the server to not even send any response via interaction pattern constant.
 *
 * It always returns single ServiceResponse of type NO_RESPONSE. Client cannot expect any response from integration
 * when using this handler, as the name suggest it sends the message and forgets about it directly.
 */
public class FireAndForgetResponseHandler implements ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(FireAndForgetResponseHandler.class);

    @Override
    public int getInteractionPattern() {
        return JMSConstants.FIRE_AND_FORGET_PATTERN;
    }

    @Override
    public ServiceResponsesList handleResponse(String selector, Connection connection, Session session, Queue responseQueue, KieServicesConfiguration config, Marshaller marshaller, KieServicesClient owner) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();
        ServiceResponse messageSentResponse = new ServiceResponse(ServiceResponse.ResponseType.NO_RESPONSE, "Message sent");
        responses.add(messageSentResponse);
        return new ServiceResponsesList(responses);
    }

    @Override
    public void dispose(Connection connection, Session session) {
        try {
            if ( session != null ) {
                session.close();
            }
            if ( connection != null ) {
                connection.close();
            }
        } catch( JMSException jmse ) {
            logger.warn("Unable to close connection or session!", jmse);
        }
    }
}

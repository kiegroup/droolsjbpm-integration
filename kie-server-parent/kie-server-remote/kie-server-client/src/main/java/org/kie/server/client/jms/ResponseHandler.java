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

import javax.jms.Connection;
import javax.jms.Queue;
import javax.jms.Session;

import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;

/**
 * Used to define how JMS response should be handled
 */
public interface ResponseHandler {

    /**
     * Returns int identifying supported interaction pattern for JMS
     * @see JMSConstants for interaction pattern constants
     * @return
     */
    int getInteractionPattern();

    /**
     * Deals with response if needed according to given interaction pattern it supports.
     * @param selector message selector to pick only response for given message
     * @param connection JMS connection to be used
     * @param session JMS session to be used
     * @param responseQueue queue that should have response delivered to
     * @param config kie server client configuration
     * @param marshaller marshaller to be used after message is received
     * @param owner top level kie server client that owns the service client
     * @return ServiceResponseList produced from response message
     */
    ServiceResponsesList handleResponse(String selector, Connection connection, Session session, Queue responseQueue,
            KieServicesConfiguration config, Marshaller marshaller, KieServicesClient owner);

    /**
     * Responsible for close of resources. Up to implementation if they can be closed directly
     * or after async processing, etc
     * @param connection jms connection used
     * @param session jms session used
     */
    void dispose(Connection connection, Session session);
}

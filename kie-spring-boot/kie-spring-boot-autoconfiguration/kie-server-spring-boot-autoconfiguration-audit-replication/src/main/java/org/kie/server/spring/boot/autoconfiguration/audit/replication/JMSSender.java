/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import com.thoughtworks.xstream.XStream;

public class JMSSender {

    private static Logger logger = LoggerFactory.getLogger(JMSSender.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private XStream xstream;

    private String endpointName;

    public JMSSender(String endpointName) {
        this.endpointName = endpointName;
    }

    protected void sendMessage(Object messageContent, Integer eventType) {

        String eventXml = xstream.toXML(messageContent);

        logger.debug("XML Event: \n {}", eventXml);

        jmsTemplate.send(endpointName, messageCreator -> {
            TextMessage message = messageCreator.createTextMessage(eventXml);
            message.setIntProperty("EventType", eventType);
            return message;
        });

    }

}

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

import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAuditDataReplicationJMSConsumer {

    private static Logger logger = LoggerFactory.getLogger(AbstractAuditDataReplicationJMSConsumer.class);

    @Autowired
    private XStream xstream;

    private EntityManagerFactory emf;

    private AtomicLong processedMessages = new AtomicLong();

    public Long get() {
        return processedMessages.get();
    }
    
    public void reset () {
        processedMessages.set(0);
    }

    public AbstractAuditDataReplicationJMSConsumer(EntityManagerFactory emf) {
        this.emf = emf;
    }


    protected void processMessage(Object message) {

        if (message instanceof TextMessage) {
            EntityManager em = emf.createEntityManager();
            TextMessage textMessage = (TextMessage) message;
            try {
                String messageContent = textMessage.getText();
                Integer eventType = textMessage.getIntProperty("EventType");
                logger.debug("Message type {} received:\n{}", eventType, messageContent);
                Object event = xstream.fromXML(messageContent);
                em.merge(event);
                processedMessages.incrementAndGet();
            } catch (JMSException e) {
                throw new RuntimeException("Something went wrong while consuming an event", e);
            } finally {
                em.close();
            }
        }

    }

}

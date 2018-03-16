/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourcesCache {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourcesCache.class);

    private String identifier;
    
    private Connection connection;
    private Session session;
    
    private MessageProducer producer;    
    private MessageConsumer consumer;
    
    private String selector;
    
    
    private ConnectionFactory factory; 
    private Queue requestQuue;
    private Queue responseQueue;
    private boolean isTransactional;
    private String username;
    private String password;
    
    public ResourcesCache(ConnectionFactory factory, Queue requestQuue, Queue responseQueue, boolean isTransactional, String username, String password) {
    
        this.identifier = UUID.randomUUID().toString();
        this.selector = "JMSCorrelationID = '" + identifier + "'";

        this.factory = factory;
        this.requestQuue = requestQuue;
        this.responseQueue = responseQueue;
        this.isTransactional = isTransactional;
        this.username = username;
        this.password = password;
        
        initResources();
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public Connection getConnection() {
        return connection;
    }
    
    public Session getSession() {
        return session;
    }
    
    public MessageProducer getProducer() {
        return producer;
    }
    
    public MessageConsumer getConsumer() {
        return consumer;
    }

    public String getSelector() {
        return selector;
    }
    
    public void reconnect() {
        close();
        initResources();
    }
    
    protected void initResources() {
        try {
            if( password != null ) {
                this.connection = this.factory.createConnection(this.username, this.password);
            } else {
                this.connection = this.factory.createConnection();
            }
            this.connection.start();
            this.session = this.connection.createSession(this.isTransactional, Session.AUTO_ACKNOWLEDGE);
            
            this.producer = this.session.createProducer(this.requestQuue);
            this.consumer = this.session.createConsumer(this.responseQueue, selector);
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create JMS resources for KIE Server Client", e);
        }
    }
    
    public void close() {
        try {
            this.connection.close();
        } catch (JMSException e) {
            logger.warn("Unable to close jms connection for resource case with id {}", identifier, e);
        }
    }
    
    public ResourcesCache copy() {
        return new ResourcesCache(factory, requestQuue, responseQueue, isTransactional, username, password);
    }
    
}

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
package org.kie.server.integrationtests.jbpm.jms;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;

import org.kie.server.integrationtests.config.TestConfig;

/**
 * XAConnectionFactory wrapper for connection factories.
 * <p>
 * It invokes createXAConnection(String, String) method using reflection since there is no common interface with this
 * method in different JMS client implementations but they all provide this method.
 */
public class KieServerXAConnectionFactory implements XAConnectionFactory {

    public static XAConnectionFactory connectionFactory;

    @Override
    public XAConnection createXAConnection() throws JMSException {
        return createXAConnection(TestConfig.getUsername(), TestConfig.getPassword());
    }

    @Override
    public XAConnection createXAConnection(String userName, String password) throws JMSException {
        return connectionFactory.createXAConnection(userName, password);
    }

    @Override
    public XAJMSContext createXAContext() {
        return null;
    }

    @Override
    public XAJMSContext createXAContext(String s, String s1) {
        return null;
    }

}

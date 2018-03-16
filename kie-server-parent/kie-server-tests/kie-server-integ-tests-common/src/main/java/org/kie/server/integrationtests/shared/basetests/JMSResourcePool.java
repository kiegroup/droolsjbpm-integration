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

package org.kie.server.integrationtests.shared.basetests;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.kie.server.client.jms.ResourcesCache;
import org.kie.server.integrationtests.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSResourcePool {

    private static Logger logger = LoggerFactory.getLogger(JMSResourcePool.class);

    private static JMSResourcePool INSTANCE = new JMSResourcePool();
    private ResourcesCache cached;

    public static JMSResourcePool get() {
        return INSTANCE;
    }

    public ResourcesCache resources(ConnectionFactory connectionFactory, Queue requestQueue, Queue responseQueue) {
        if (cached == null) {
            cached = new ResourcesCache(connectionFactory, requestQueue, responseQueue, false, TestConfig.getUsername(),
                                        TestConfig.getPassword());
            logger.debug("new JMS resource cache created");
        }

        return cached;
    }

    public void dispose() {
        if (cached != null) {
            cached.close();
            cached = null;

            logger.debug("JMS resource cache disposed");
        }
    }
}

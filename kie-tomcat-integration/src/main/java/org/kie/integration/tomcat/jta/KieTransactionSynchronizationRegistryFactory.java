/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.integration.tomcat.jta;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class KieTransactionSynchronizationRegistryFactory implements ObjectFactory {

    private static final Log LOGGER = LogFactory.getLog(KieTransactionSynchronizationRegistryFactory.class);

    /**
     * User internal factory method to instantiate new or reuse existing instance of
     * {@link jakarta.transaction.TransactionSynchronizationRegistry}.
     *
     * @param obj
     * @param name
     * @param nameCtx
     * @param environment
     * @return instance of {@link jakarta.transaction.TransactionSynchronizationRegistry} or {@code null} if instantiation has
     * failed.
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
        JTAEnvironmentBean jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();
        try {
            return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();
        } catch (Exception e) {
            String error = String.format("Failed to instantiate TransactionSynchronizationRegistry %s due to %s; jtaEnvironmentBean is %s", name, e.getMessage(), jtaEnvironmentBean);
            LOGGER.fatal(error, e);
            return null;
        }
    }

}   
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

package org.kie.spring.jbpm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.drools.core.impl.EnvironmentFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.kie.spring.persistence.KieSpringTransactionManagerFactory;
import org.springframework.transaction.jta.JtaTransactionManager;

@RunWith(Parameterized.class)
public class KieSpringTransactionManagerFactoryTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][]{
                {JTA_EMF_SINGLETON_PATH, ProcessInstanceIdContext.get()}
        };
        return Arrays.asList(data);
    }

    public KieSpringTransactionManagerFactoryTest(String contextPath,
                                                  Context<?> runtimeManagerContext) {
        super(contextPath,
              runtimeManagerContext);
    }

    @Test
    public void testTransactionManagerSet() throws Exception {
        Object txManager = context.getBean("jbpmTxManager");
        assertNotNull(txManager);
        assertTrue(txManager instanceof JtaTransactionManager);

        JtaTransactionManager jtaManager = (JtaTransactionManager) txManager;

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.TRANSACTION_MANAGER,
                jtaManager);
        KieSpringTransactionManagerFactory factory = new KieSpringTransactionManagerFactory();
        Object springTxManager = factory.newTransactionManager(env);
        assertNotNull(springTxManager);
        assertTrue(springTxManager instanceof KieSpringTransactionManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransactionManagerNotSet() throws Exception {
        Object txManager = context.getBean("jbpmTxManager");
        assertNotNull(txManager);
        assertTrue(txManager instanceof JtaTransactionManager);

        JtaTransactionManager jtaManager = (JtaTransactionManager) txManager;

        Environment env = EnvironmentFactory.newEnvironment();
        KieSpringTransactionManagerFactory factory = new KieSpringTransactionManagerFactory();
        Object springTxManager = factory.newTransactionManager(env);
    }
}

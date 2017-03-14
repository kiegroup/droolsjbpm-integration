/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.jbpm.process.audit.AuditLogService;
import org.junit.After;
import org.junit.Before;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

public abstract class AbstractJbpmSpringParameterizedTest extends AbstractJbpmSpringTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJbpmSpringParameterizedTest.class);

    protected String contextPath;
    protected Context<?> runtimeManagerContext;

    private EntityManager em;

    public AbstractJbpmSpringParameterizedTest(String contextPath, Context<?> runtimeManagerContext) {
        this.contextPath = contextPath;
        this.runtimeManagerContext = runtimeManagerContext;
    }

    @Before
    public void setUp() {
        LOG.info("Creating spring context - " + contextPath);
        context = new ClassPathXmlApplicationContext(contextPath);
        LOG.info("The spring context created.");
    }

    @After
    public void cleanUp() {
        if(em != null) {
            em.close();
            em = null;
        }
    }

    protected KieSession getKieSession() {
        return getEngine().getKieSession();
    }

    protected AuditLogService getLogService() {
        return context.getBean("logService",
                               AuditLogService.class);
    }

    protected RuntimeEngine getEngine() {
        return getManager().getRuntimeEngine(runtimeManagerContext);
    }

    protected RuntimeManager getManager() {
        return  context.getBean("runtimeManager",
                                RuntimeManager.class);
    }

    protected RuntimeManager getSecondManager() {
        return  context.getBean("runtimeManager2", RuntimeManager.class);
    }

    protected TaskService getTaskService() {
        return getEngine().getTaskService();
    }

    protected AbstractPlatformTransactionManager getTransactionManager() {
        return context.getBean("jbpmTxManager", AbstractPlatformTransactionManager.class);
    }

    protected EntityManager getEntityManager() {
        if(em != null) return em;
        if(context.containsBean("jbpmEM")) {
            return context.getBean("jbpmEM", EntityManager.class);
        } else {
            em = context.getBean("jbpmEMF", EntityManagerFactory.class).createEntityManager();
            return em;
        }
    }
}
